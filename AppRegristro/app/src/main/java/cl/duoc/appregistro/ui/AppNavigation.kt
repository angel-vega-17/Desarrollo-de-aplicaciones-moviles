package cl.duoc.appregistro.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val REGISTER = "register"
    const val LIST = "list"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Un solo ViewModel compartido por ambas pantallas
    val viewModel: UserViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            UserListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
