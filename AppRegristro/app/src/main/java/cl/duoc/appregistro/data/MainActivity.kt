package cl.duoc.appregistro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.appregistro.ui.AppNavigation
import cl.duoc.appregistro.ui.theme.AppRegistroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppRegistroTheme {
                AppNavigation()
            }
        }
    }
}
