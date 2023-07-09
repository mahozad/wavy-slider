package showcase

import androidx.compose.ui.window.Window
import org.jetbrains.skiko.wasm.onWasmReady

fun main() {
    console.log("Hello, Kotlin/JS!")
    onWasmReady {
        Window(title = "Wavy slider showcase") {
            MainView()
        }
    }

    // See https://github.com/JetBrains/compose-multiplatform/issues/2186
    // val body = document.getElementsByTagName("body")[0] as HTMLElement
    // renderComposable(rootElementId = "root") {
    //     MaterialTheme {
    //         WavySlider(0.67f, true, {})
    //     }
    // }
}
