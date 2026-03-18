import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun App() {
    MaterialTheme {
        Text("App comum - verifique as plataformas específicas para UI")
    }
}

expect fun getPlatformName(): String