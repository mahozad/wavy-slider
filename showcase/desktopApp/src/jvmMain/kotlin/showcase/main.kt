package showcase

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        title = "Wavy slider showcase",
        onCloseRequest = ::exitApplication
    ) {
        MainView()
    }
}
