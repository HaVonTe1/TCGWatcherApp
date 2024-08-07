package de.dkutzer.tcgwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import de.dkutzer.tcgwatcher.cards.boundary.SearchActivity
import de.dkutzer.tcgwatcher.cards.entity.BaseProductModel
import de.dkutzer.tcgwatcher.settings.boundary.SettingsActivity
import de.dkutzer.tcgwatcher.ui.HomeScreenActivity
import de.dkutzer.tcgwatcher.ui.ItemOfInterestActivity
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import org.slf4j.impl.HandroidLoggerAdapter

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HandroidLoggerAdapter.APP_NAME = "TCGWatcher"
            HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
            HandroidLoggerAdapter.ANDROID_API_LEVEL = Build.VERSION.SDK_INT
            MainTheme()
        }
    }
}


sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    data object HomeScreen : Screen("home", R.string.home, icon = Icons.TwoTone.Star)

    data object ItemsOfInterestScreen :
        Screen("itemsOfInterest", R.string.items, icon = Icons.TwoTone.Menu)

    data object SearchScreen : Screen("search", R.string.search, icon = Icons.TwoTone.Search)
    data object SettingsScreen : Screen("settings", R.string.settings, icon = Icons.TwoTone.Settings)

}


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainTheme() {
    val internetPermissionState = rememberPermissionState(
        android.Manifest.permission.INTERNET // default - no permission needed
    )

    val activity = (LocalContext.current as? Activity)

    TCGWatcherTheme {

        if (!internetPermissionState.status.isGranted) {
            PermissionsDialog(
                onDismissRequest = {
                    activity?.finishAffinity()
                },
                onConfirmation = {

                    internetPermissionState.launchPermissionRequest()
                },
                currentPermissionState = internetPermissionState
            )
        }
        MainScreen()
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun PermissionsDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    currentPermissionState: PermissionState

) {

    if (currentPermissionState.status.shouldShowRationale) {

        AlertDialog(
            icon = {
                Icon(Icons.Default.Info, contentDescription = "Info Icon")
            },
            title = {
                Text(text = stringResource(id = R.string.permissionInternetAccessDialogTitle))
            },
            text = {
                Text(text = stringResource(id = R.string.permissionInternetAccessDialogText))
            },
            onDismissRequest = {
                onDismissRequest()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmation()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }


}


@Composable
private fun MainScreen(items: List<BaseProductModel> = emptyList()) {
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        bottomBar = {

            NavigationBar(
                containerColor = Color.LightGray,
                contentColor = Color.Black,
                tonalElevation = 2.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                MyBottomNavigationItem(
                    currentDestination = currentDestination,
                    navController = navController,
                    screen = Screen.HomeScreen
                )
                MyBottomNavigationItem(
                    currentDestination,
                    navController,
                    Screen.ItemsOfInterestScreen
                )
                MyBottomNavigationItem(currentDestination, navController, Screen.SearchScreen)
                MyBottomNavigationItem(currentDestination, navController, Screen.SettingsScreen)

            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.HomeScreen.route,
            Modifier.padding(innerPadding)
        ) {

            composable(Screen.HomeScreen.route) { HomeScreenActivity(snackbarHostState) }
            composable(Screen.ItemsOfInterestScreen.route) { ItemOfInterestActivity(items) }
            composable(Screen.SearchScreen.route) { SearchActivity(snackbarHostState) }
            composable(Screen.SettingsScreen.route) { SettingsActivity() }

        }
    }
}

@Composable
private fun RowScope.MyBottomNavigationItem(
    currentDestination: NavDestination?,
    navController: NavHostController,
    screen: Screen
) {
    NavigationBarItem(
        icon = { Icon(screen.icon, contentDescription = null) },
        label = { Text(stringResource(screen.resourceId)) },
        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

//
//@Preview(showBackground = true)
//@Composable
//fun TestMainPreview() {
//    MainScreen(Datasource().loadMockData())
//
//}