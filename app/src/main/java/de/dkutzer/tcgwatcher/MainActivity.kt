package de.dkutzer.tcgwatcher

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
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
import de.dkutzer.tcgwatcher.collectables.search.presentation.SearchScreen
import de.dkutzer.tcgwatcher.help.presentation.HelpScreen
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.impl.HandroidLoggerAdapter

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
    data object HelpScreen : Screen("help", R.string.help, icon = Icons.TwoTone.Info)

}


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainTheme() {
    val internetPermissionState = rememberPermissionState(
        android.Manifest.permission.INTERNET // default - no permission needed
    )

    val activity = (LocalActivity.current)

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
private val logger = KotlinLogging.logger {}


@Composable
private fun MainScreen() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(Unit) {
        EventBus.events.collect { event ->
            // Dismiss current Snackbar if shown and display new one
            logger.debug { "Received Event: $event" }

            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = event,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)},
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(56.dp), // Adjust height (default is 80.dp)
                containerColor = Color.Transparent,
                tonalElevation = 2.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                MyBottomNavigationItem(currentDestination, navController, Screen.HomeScreen)
                //MyBottomNavigationItem(currentDestination, navController, Screen.ItemsOfInterestScreen)
                MyBottomNavigationItem(currentDestination, navController, Screen.SearchScreen)
                MyBottomNavigationItem(currentDestination, navController, Screen.HelpScreen)
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.SearchScreen.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.HomeScreen.route) { HomeScreen(snackbarHostState) }
            //composable(Screen.ItemsOfInterestScreen.route) { ItemsOfInterestScreen() }
            composable(Screen.SearchScreen.route) { SearchScreen(snackbarHostState) }
//            composable(Screen.SettingsScreen.route) { SettingsScreen() }
            composable(Screen.HelpScreen.route) { HelpScreen() }
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
        modifier = Modifier.padding(vertical = 4.dp), // Reduced vertical padding
        icon = {
            Icon(
                screen.icon, contentDescription = null)
               },
        //label = { Text(fontSize = 12.sp, text= stringResource(screen.resourceId)) },
        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
        onClick = {
            navController.navigate(screen.route) {
                launchSingleTop = true
            }
        }
    )
}