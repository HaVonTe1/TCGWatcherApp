package de.dkutzer.tcgwatcher

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.TCGWatcherTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import de.dkutzer.tcgwatcher.collectables.search.presentation.SearchScreen
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
private fun MainScreen() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = {
//            NavigationBar(
//                containerColor = MaterialTheme.colorScheme.surfaceContainer,
//                contentColor = MaterialTheme.colorScheme.secondary,
//                tonalElevation = 2.dp
//            ) {
//                val navBackStackEntry by navController.currentBackStackEntryAsState()
//                val currentDestination = navBackStackEntry?.destination
//
//               // MyBottomNavigationItem(currentDestination, navController, Screen.HomeScreen)
//                //MyBottomNavigationItem(currentDestination, navController, Screen.ItemsOfInterestScreen)
//                MyBottomNavigationItem(currentDestination, navController, Screen.SearchScreen)
//                //MyBottomNavigationItem(currentDestination, navController, Screen.SettingsScreen)
//            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.SearchScreen.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.HomeScreen.route) { HomeScreen(snackbarHostState) }
            //composable(Screen.ItemsOfInterestScreen.route) { ItemsOfInterestScreen() }
            composable(Screen.SearchScreen.route) { SearchScreen(snackbarHostState) }
            //composable(Screen.SettingsScreen.route) { SettingsScreen() }
        }
    }
}
