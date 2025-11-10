package showcase

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

fun webApp() {
    @OptIn(ExperimentalComposeUiApi::class)
    ComposeViewport {
        App()
    }
}
