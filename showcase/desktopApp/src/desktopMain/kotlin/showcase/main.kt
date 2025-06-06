package showcase

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        title = "Wavy slider showcase",
        icon = painterResource("logo.svg"),
        onCloseRequest = ::exitApplication
    ) {
        MainView()
    }
}
