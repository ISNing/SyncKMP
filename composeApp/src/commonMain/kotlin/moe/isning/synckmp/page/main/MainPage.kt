package moe.isning.synckmp.page.main

import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import moe.isning.synckmp.lifecycle.LocalServiceController
import moe.isning.synckmp.lifecycle.SyncServiceState
import moe.isning.synckmp.page.main.devices.DevicesPage
import moe.isning.synckmp.page.main.folders.FoldersPage
import moe.isning.synckmp.page.main.home.HomePage
import moe.isning.synckmp.page.main.logs.LogsPage
import moe.isning.synckmp.page.main.settings.SettingsPage


@Composable
fun MainPage(/*preferredViewModel: PreferredViewModel*/) {
    val navController = rememberNavController()

    // 获取服务控制器并监听运行状态
    val serviceController = LocalServiceController.current
    val serviceState by serviceController.state.collectAsState()
    val isRunning = serviceState == SyncServiceState.Running

    rememberCoroutineScope()
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
//                val route: MainNavigationRoute.FolderEditConfig = it.toRoute()
                HomePage(isRunning = isRunning)
            }
            composable<MainNavigationRoute.Folders> {
                FoldersPage()
            }
            composable<MainNavigationRoute.Devices> {
                DevicesPage()
            }
            composable<MainNavigationRoute.Logs> {
                LogsPage()
            }
            composable<MainNavigationRoute.Settings> {
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
                it.toRoute()
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