package com.useinsider.kotlindemo.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.useinsider.kotlindemo.screen.AppCardsScreen
import com.useinsider.kotlindemo.screen.AppFramesScreen
import com.useinsider.kotlindemo.screen.CustomEventScreen
import com.useinsider.kotlindemo.screen.CustomUserAttributesScreen
import com.useinsider.kotlindemo.screen.MainScreen
import com.useinsider.kotlindemo.viewmodel.AppCardsViewModel
import com.useinsider.kotlindemo.viewmodel.AppFramesViewModel
import com.useinsider.kotlindemo.viewmodel.CustomAttributesViewModel
import com.useinsider.kotlindemo.viewmodel.CustomEventViewModel
import com.useinsider.kotlindemo.viewmodel.MainViewModel

public object Routes {
    public const val MAIN: String = "main"
    public const val CUSTOM_EVENT: String = "custom_event"
    public const val CUSTOM_USER_ATTRIBUTES: String = "custom_user_attributes"
    public const val APP_CARDS: String = "app_cards"
    public const val APP_FRAMES: String = "app_frames"
}

@Composable
public fun AppNavGraph(navController: NavHostController): Unit {
    val mainViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToCustomEvent = { navController.navigate(Routes.CUSTOM_EVENT) },
                onNavigateToCustomAttributes = { navController.navigate(Routes.CUSTOM_USER_ATTRIBUTES) },
                onNavigateToAppCards = { navController.navigate(Routes.APP_CARDS) },
                onNavigateToAppFrames = { navController.navigate(Routes.APP_FRAMES) }
            )
        }
        composable(Routes.CUSTOM_EVENT) {
            val eventViewModel: CustomEventViewModel = viewModel()
            CustomEventScreen(
                viewModel = eventViewModel,
                onBack = { navController.popBackStack() },
                onEventSent = { result ->
                    mainViewModel.updatePrintLabel(result)
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.APP_CARDS) {
            val appCardsViewModel: AppCardsViewModel = viewModel()
            AppCardsScreen(
                viewModel = appCardsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.APP_FRAMES) {
            val appFramesViewModel: AppFramesViewModel = viewModel()
            AppFramesScreen(
                viewModel = appFramesViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.CUSTOM_USER_ATTRIBUTES) {
            val attrsViewModel: CustomAttributesViewModel = viewModel()
            CustomUserAttributesScreen(
                viewModel = attrsViewModel,
                onBack = { navController.popBackStack() },
                onAttributesSet = { result ->
                    mainViewModel.updatePrintLabel(result)
                    navController.popBackStack()
                }
            )
        }
    }
}
