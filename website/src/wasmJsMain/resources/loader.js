const app = document.getElementById("app");
const loading = document.getElementById("loading");
const loadingText = document.getElementById("loading-text");
const progressBar = document.getElementById("progress-bar");

// app.js may reference the same .wasm file under multiple names while only one of
// them is actually downloaded. Files with identical sizes therefore share one
// { size, loaded } object, so an unused alias can't keep progress below 100%.
const downloads = new Set();       // unique { size, loaded } objects
const downloadsByPath = new Map(); // URL pathname -> shared { size, loaded }

let totalSize = 0;
let failed = false;

const formatMB = bytes => (bytes / 1024 / 1024).toFixed(2);

function renderProgress() {
    if (failed || !totalSize) return;

    let received = 0;
    for (const { size, loaded } of downloads) {
        // Clamped because Fetch exposes decoded bytes (after gzip/brotli) while
        // Content-Length describes the encoded size
        received += Math.min(loaded, size);
    }

    if (received >= totalSize) {
        hideLoading();
        return;
    }

    const percent = Math.floor(received / totalSize * 100);
    progressBar.style.width = `${percent}%`;
    loadingText.textContent = `Loading the app… ${percent}% (${formatMB(received)} of ${formatMB(totalSize)} MB)`;
}

function hideLoading() {
    loading.style.opacity = 0;
    loading.style.transform = "translateX(-50%) scaleX(0.9) scaleY(0.9)";
}

function fail(error) {
    if (failed) return;
    failed = true;
    console.error("Could not load the Wavy slider application:", error);
    loadingText.textContent = "Loading failed. Please refresh the page.";
    progressBar.classList.add("loading-error");
}

/** Extracts the unique (absolute) .wasm URLs mentioned as string literals in the webpack bundle. */
function findWasmUrls(source) {
    const urls = new Set();
    for (const [, name] of source.matchAll(/["'`]([\w@./-]+\.wasm)["'`]/g)) {
        urls.add(new URL(name, document.baseURI).href);
    }
    return [...urls];
}

/** Gets the size of a remote file without downloading it. */
async function getSize(url) {
    const head = await fetch(url, { method: "HEAD", cache: "no-store" });
    const headSize = Number(head.headers.get("content-length"));
    if (head.ok && headSize > 0) return headSize;

    // Some static hosts do not return Content-Length for HEAD requests.
    // A one-byte range request still exposes the complete size.
    const range = await fetch(url, { headers: { Range: "bytes=0-0" }, cache: "no-store" });
    await range.body?.cancel();
    const rangeTotal = range.headers.get("content-range")?.match(/\/(\d+)$/)?.[1];
    const size = Number(rangeTotal ?? range.headers.get("content-length"));

    if (!(size > 0)) throw new Error(`Could not determine the size of ${url}`);
    return size;
}

/**
 * Finds the .wasm files the app needs and records their sizes.
 * Never throws: progress is only a nicety and must not prevent the app from starting,
 * so a file whose size cannot be determined (e.g. a false positive of the regex
 * that returns 404) is skipped.
 */
async function discoverDownloads(appSource) {
    const urls = findWasmUrls(appSource);
    const results = await Promise.allSettled(urls.map(getSize));
    const bySize = new Map();

    results.forEach((result, i) => {
        if (result.status === "rejected") {
            console.warn("Skipped progress tracking of", urls[i], result.reason);
            return;
        }
        const size = result.value;
        let download = bySize.get(size);
        if (!download) {
            download = { size, loaded: 0 };
            bySize.set(size, download);
            downloads.add(download);
            totalSize += size;
        }
        downloadsByPath.set(new URL(urls[i]).pathname, download);
    });

    console.debug(
        "Discovered .wasm files:",
        Object.fromEntries([...downloadsByPath].map(([path, { size }]) => [path, size]))
    );
}

/** Wraps window.fetch so that downloads of the discovered .wasm files report their progress. */
function trackWasmDownloads() {
    const nativeFetch = window.fetch.bind(window);

    window.fetch = async (input, init) => {
        const { pathname } = new URL(input.url ?? input, document.baseURI);
        const download = downloadsByPath.get(pathname);
        if (!download) return nativeFetch(input, init);

        try {
            const response = await nativeFetch(input, init);
            if (!response.ok || !response.body) {
                throw new Error(`Failed to download ${pathname}: HTTP ${response.status}`);
            }

            // The body stream is already content-decoded, so drop the headers that
            // describe the encoded body. The others (notably Content-Type, required
            // by WebAssembly.instantiateStreaming) are kept.
            const headers = new Headers(response.headers);
            headers.delete("content-length");
            headers.delete("content-encoding");
            headers.delete("content-range");

            const body = response.body.pipeThrough(new TransformStream({
                transform(chunk, controller) {
                    download.loaded += chunk.byteLength;
                    renderProgress();
                    controller.enqueue(chunk);
                },
                flush() {
                    download.loaded = download.size;
                    renderProgress();
                }
            }));

            return new Response(body, {
                status: response.status,
                statusText: response.statusText,
                headers
            });
        } catch (error) {
            fail(error);
            throw error;
        }
    };
}

try {
    loadingText.textContent = "Preparing the app…";

    // Download app.js first (it is small) to discover which .wasm files it needs
    const appResponse = await fetch("app.js");
    if (!appResponse.ok) throw new Error(`Failed to download app.js: HTTP ${appResponse.status}`);
    const appSource = await appResponse.text();

    await discoverDownloads(appSource);

    if (totalSize > 0) {
        trackWasmDownloads();
        renderProgress();
    } else {
        hideLoading();
    }

    // Safety net: hide the loader as soon as the app renders, even if the byte count
    // never reached 100% (e.g. a mentioned .wasm file that is never fetched)
    const observer = new MutationObserver(() => {
        if (app.childElementCount > 0) {
            observer.disconnect();
            hideLoading();
        }
    });
    observer.observe(app, { childList: true });

    // Execute the already downloaded bundle without fetching app.js again.
    // Webpack's public-path fallback finds an external <script src> on the page
    // (e.g. theme-switch.min.js) and derives the base directory from it.
    const script = document.createElement("script");
    script.text = appSource;
    document.body.append(script);
} catch (error) {
    fail(error);
}
