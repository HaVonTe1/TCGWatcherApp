package de.dkutzer.tcgwatcher

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Menu
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import de.dkutzer.tcgwatcher.models.ItemOfInterest
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import de.dkutzer.tcgwatcher.views.ItemOfInterestCardView
import de.dkutzer.tcgwatcher.views.SearchView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme ()
        }
    }
}


sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    object ItemsOfInterestScreen :
        Screen("itemsOfInterest", R.string.items, icon = Icons.TwoTone.Menu)
    object SearchScreen : Screen("search", R.string.search, icon = Icons.TwoTone.Search)
    object SettingsScreen : Screen("settings", R.string.settings, icon = Icons.TwoTone.Settings)

}


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainTheme( ) {
    val internetPermissionState = rememberPermissionState(
        android.Manifest.permission.INTERNET
    )

    TCGWatcherTheme {

        if (!internetPermissionState.status.isGranted) {
            permissionsScreen(internetPermissionState)
        }
        else {
            MainScreen()
        }

    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun permissionsScreen(internetPermissionState: PermissionState) {
    Column {
        val textToShow = if (internetPermissionState.status.shouldShowRationale) {
            // If the user has denied the permission but the rationale can be shown,
            // then gently explain why the app requires this permission
            "The Internet access is important for this app. Please grant the permission."
        } else {
            // If it's the first time the user lands on this feature, or the user
            // doesn't want to be asked again for this permission, explain that the
            // permission is required
            "Internet access permission required for this feature to be available. " +
                    "Please grant the permission"
        }
        Text(textToShow)
        Button(onClick = { internetPermissionState.launchPermissionRequest() }) {
            Text("Request permission")
        }
    }
}

@Composable
private fun MainScreen(items: List<ItemOfInterest> = emptyList()) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {

            BottomNavigation(
                backgroundColor = Color.LightGray,
                contentColor = Color.Black,
                elevation = 2.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
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
            startDestination = Screen.ItemsOfInterestScreen.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.ItemsOfInterestScreen.route) { ItemOfInterestCardView(items) }
            composable(Screen.SearchScreen.route) { SearchView() }
            composable(Screen.SettingsScreen.route) { DummyView(navController) }

        }
    }
}

@Composable
private fun RowScope.MyBottomNavigationItem(
    currentDestination: NavDestination?,
    navController: NavHostController,
    screen: Screen
) {
    BottomNavigationItem(
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

@Composable
fun DummyView(navController: NavController, modifier: Modifier = Modifier) {
    Text(text ="Hallo")
}

@Preview(showBackground = true)
@Composable
fun TestMainPreview() {
    MainScreen(Datasource().loadMockData())

}