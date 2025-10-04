package moe.isning.syncthing.page.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import androidx.savedstate.read
import kotlinx.coroutines.launch
import moe.isning.syncthing.lifecycle.LocalServiceController
import moe.isning.syncthing.page.main.devices.DevicesPage
import moe.isning.syncthing.page.main.folders.FoldersPage
import moe.isning.syncthing.page.main.home.HomePage
import moe.isning.syncthing.page.main.logs.LogsPage
import moe.isning.syncthing.page.main.settings.SettingsPage


@Composable
fun MainPage(/*preferredViewModel: PreferredViewModel*/) {
    val navController = rememberNavController()
    
    // 获取服务控制器并监听运行状态
    val serviceController = LocalServiceController.current
    val isRunning = serviceController.isRunning

    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationSuiteScaffold(
//        layoutType = NavigationSuiteType.WideNavigationRailExpanded,
        navigationSuiteItems = {
            MainNavigationData.entries.forEach { navData ->
                val selected = currentDestination?.hierarchy?.any { it.hasRoute(navData.route::class) } == true
                item(
                    icon = {
                        Icon(
                            imageVector = if (selected) navData.iconActive else navData.icon,
                            contentDescription = navData.contentDescription()
                        )
                    },
                    alwaysShowLabel = false,
                    label = {
                        Text(navData.label())
                    },
                    selected = selected,
                    onClick = { navController.navigate(navData.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    } }
                )
            }
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = MainNavigationRoute.Home,
        ) {
            composable<MainNavigationRoute.Home> {
                val route: MainNavigationRoute.Home = it.toRoute()
                HomePage(isRunning = isRunning)
            }
            composable<MainNavigationRoute.Folders> {
                val route: MainNavigationRoute.Folders = it.toRoute()
                FoldersPage()
            }
            composable<MainNavigationRoute.Devices> {
                val route: MainNavigationRoute.Devices = it.toRoute()
                DevicesPage()
            }
            composable<MainNavigationRoute.Logs> {
                val route: MainNavigationRoute.Logs = it.toRoute()
                LogsPage()
            }
            composable<MainNavigationRoute.Settings> {
                val route: MainNavigationRoute.Settings = it.toRoute()
                SettingsPage()
            }

            composable<MainNavigationRoute.DeviceStatusDetail>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth })
                },
                // --- EditPage 的 popExitTransition ---
                popExitTransition = {
                    // 当从 EditPage 返回时，EditPage 的退出动画
                    // 它会随着手势逐渐缩小和淡出
                    scaleOut(targetScale = 0.9f) + fadeOut()
                }
            ) {
                val route: MainNavigationRoute.DeviceStatusDetail = it.toRoute()
//            if (preferredViewModel.state.showExpressiveUI)
//                NewEditPage(
//                    navController = navController,
//                    id = if (id != -1L) id
//                    else null
//                ) else
//                EditPage(
//                    navController = navController,
//                    id = if (id != -1L) id
//                    else null
//                )
            }

            composable<MainNavigationRoute.DeviceEditConfig>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth })
                },
                popExitTransition = {
                    scaleOut(targetScale = 0.9f) + fadeOut()
                }
            ) {
                val route: MainNavigationRoute.DeviceEditConfig = it.toRoute()
//            ApplyPage(
//                navController = navController,
//                id = id
//            )
            }
            composable<MainNavigationRoute.FolderStatusDetail>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth })
                },
                popExitTransition = {
                    scaleOut(targetScale = 0.9f) + fadeOut()
                }
            ) {
                val route: MainNavigationRoute.FolderStatusDetail = it.toRoute()
//            ApplyPage(
//                navController = navController,
//                id = id
//            )
            }
            composable<MainNavigationRoute.FolderEditConfig>(
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> fullWidth })
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth })
                },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth })
                },
                popExitTransition = {
                    scaleOut(targetScale = 0.9f) + fadeOut()
                }
            ) {
                val route: MainNavigationRoute.FolderEditConfig = it.toRoute()
                HomePage(false)
            }
//        composable(
//            route = MainScreen.Theme.route,
//            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
//            popExitTransition = { scaleOut(targetScale = 0.9f) + fadeOut() } // Your predictive back animation
//        ) {
//            if (preferredViewModel.state.showExpressiveUI) {
//                NewThemeSettingsPage(navController = navController, viewModel = preferredViewModel)
//            } else {
//                LegacyThemeSettingsPage(navController = navController, viewModel = preferredViewModel)
//            }
//        }
//        composable(
//            route = MainScreen.InstallerGlobal.route,
//            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
//            popExitTransition = { scaleOut(targetScale = 0.9f) + fadeOut() } // Your predictive back animation
//        ) {
//            if (preferredViewModel.state.showExpressiveUI) {
//                NewInstallerGlobalSettingsPage(navController = navController, viewModel = preferredViewModel)
//            } else {
//                LegacyInstallerGlobalSettingsPage(navController = navController, viewModel = preferredViewModel)
//            }
//        }
        }
    }
}