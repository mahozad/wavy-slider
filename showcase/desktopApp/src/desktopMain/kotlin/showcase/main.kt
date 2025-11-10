package showcase

import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.decodeToSvgPainter
import java.io.InputStream

fun main() = application {
    val appErrorIcon = javaClass
        .classLoader
        .getResourceAsStream("logo.svg")
        ?.use(InputStream::readAllBytes)
        ?.decodeToSvgPainter(Density(1f))
    Window(
        title = "Wavy slider showcase",
        icon = appErrorIcon,
        onCloseRequest = ::exitApplication
    ) {
        App()
    }
}
