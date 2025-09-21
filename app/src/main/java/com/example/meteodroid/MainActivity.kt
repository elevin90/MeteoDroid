
// -----------------------------------------------------------------------------
// MeteoDroid: Main UI entry + screens (Jetpack Compose, Material 3, Hilt DI)
// This file contains the activity entry point and the primary Composables for
// navigation drawer, top app bar, settings sheet, and weather content.
// The comments explain why each API/state is used and common pitfalls.
// -----------------------------------------------------------------------------

package com.example.meteodroid
import androidx.compose.runtime.getValue
import Database.CityCacheEntity
import Location.LocationFetchResult
import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.meteodroid.Cards.WeatherCard
import com.example.meteodroid.Cards.WeatherDetailsCard
import com.example.meteodroid.Cards.WeatherForecastCard
import com.example.meteodroid.CitySearch.BottomCitySearchSheet
import com.example.meteodroid.CitySearch.CitySearchViewModel
import com.example.meteodroid.Settings.SettingsView
import com.example.meteodroid.Utils.PermissionMissingView
import com.example.meteodroid.Utils.rememberPermissionRequesterWithSnackbar
import com.example.meteodroid.Weather.WeatherViewModel
import com.example.meteodroid.ui.theme.MeteoDroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

// region Activity entry point

/**
 * Main activity, annotated with [@AndroidEntryPoint] so Hilt can inject
 * dependencies into Android framework classes (Activity here) and into any
 * Composables/ViewModels that are resolved via [hiltViewModel()].
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
/**
 * Standard Android entry. We enable edge‑to‑edge (status/navigation bars
 * drawn over the app) and set our Compose content tree
 * */
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeteoDroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    val viewModel: WeatherViewModel = hiltViewModel()
                    WeatherDetails(viewModel = viewModel)
                }
            }
        }
    }
}
// region WeatherDetails screen (drawer + app bar + main content + sheets)

/**
 * Root screen of the app UI. Hosts a left-side navigation drawer for favorites,
 * a top app bar with menu/settings, and the main weather content area.
 *
 * @param viewModel The [WeatherViewModel] holding UI state and operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetails(viewModel: WeatherViewModel) {
    // Navigation drawer state; use remember to keep it across recompositions.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // Remember a CoroutineScope bound to the composition to perform suspend ops
    // like opening/closing the drawer or launching one-off tasks.
    val scope = rememberCoroutineScope()
    // Snackbar host state to enqueue snackbars from child components.
    val snackbarHostState = remember { SnackbarHostState() }
    // Local UI flags controlling sheets/routes. `rememberSaveable` could be used
    // if you want them to survive process death/rotation automatically.
    val showBottomSheet = remember { mutableStateOf(false) }
    val showSettings = remember { mutableStateOf(false) }
    // Collect cities list in a lifecycle-aware manner to avoid leaks when the
    // composable goes to the background.
    val cities by viewModel.savedCities.collectAsStateWithLifecycle()
    // Build a permission requester that will show a snackbar on denial and,
    // on grant, will invoke viewModel.fetchLocation().
    //
    // IMPORTANT: There's a likely variable name typo below — the parameter is
    // `snackarHostState`, but the local variable is `snackbarHostState`.
    // Pass the correct `snackbarHostState` instance to avoid a compile error.
    val requestLocationPermission = rememberPermissionRequesterWithSnackbar(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        snackarHostState = snackbarHostState,
        onGranted = { viewModel.fetchLocation() }
    )

    val selectedCityId by viewModel.currentCityId.collectAsState()

    // Modal drawer container with left-side slide menu
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(cities = cities, selectedCityId = selectedCityId) { city ->
                // Update selection in VM and close the drawer.
                viewModel.selectCity(city)
                scope.launch {
                    drawerState.close()
                }
            }
        }) {
        // Primary scaffold for this screen — hosts the top app bar and content.
        Scaffold(
            topBar = {
                MainTopAppBar(onMenuClick = {
                    scope.launch {
                        drawerState.open()
                    }
                }, onSettingsClieck = {
                    showSettings.value = true
                })
            }, content = {
                MainContent(
                    padding = it,
                    viewModel = viewModel,
                    onRequestPermission = requestLocationPermission,
                    onEdit = {
                        showBottomSheet.value = true
                    }
                )
            }
        )
        // Bottom sheet to search and add cities. Placed outside Scaffold's
        // content lambda so it overlays the content when visible.
        if (showBottomSheet.value) {
            // Separate ViewModel for the sheet, injected by Hilt
            val searchViewModel: CitySearchViewModel = hiltViewModel()
            BottomCitySearchSheet(
                viewModel = searchViewModel,
                onDismiss = {
                    // Clear transient state within the sheet VM and hide sheet.
                    searchViewModel.clearLocation()
                    showBottomSheet.value = false
                },
                onCitySelected = { location ->
                    // Feed selection back to the main VM.
                    viewModel.updateForSelectedLocation(location = location)
                }
            )
        }
        // Settings modal
        if (showSettings.value) {
            SettingsView(onDismiss = { showSettings.value = false })
        }
    }
}
// endregion

// region Drawer content (favorites list)

/**
 * Left drawer content showing a list of favorite cities.
 *
 * @param cities All saved/favorite cities.
 * @param selectedCity Currently selected city (to render a checkmark).
 * @param onCitySelected Callback fired when the user taps a city in the list.
 */
@Composable
fun DrawerContent(
    cities: List<CityCacheEntity>,
    selectedCityId: Long?,
    onCitySelected: (CityCacheEntity) -> Unit
) {
    ModalDrawerSheet {
        if (cities.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.Favourites_empty) , modifier = Modifier.padding(16.dp))
            }
        }
        Text(
            stringResource(R.string.Favourites_title),
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            modifier = Modifier.padding(16.dp)
        )
        CitiesList(
            cities,
            selectedCityId,
            onCitySelected
        )
    }
}
// endregion

// region Cities list (LazyColumn)

/**
 * Renders the favorites as a scrollable list. Uses stable keys to preserve
 * item state/animations and renders a check icon for the selected city.
 *
 * @param cities List of cached city entities from persistence.
 * @param selectedCity Currently selected city; highlighted with a check icon.
 * @param onCitySelected Invoked when user taps a row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesList(
    cities: List<CityCacheEntity>,
    selectedCityId: Long?,
    onCitySelected: (CityCacheEntity) -> Unit
    ) {
    // Use a stable key to help Compose track items across changes.
    LazyColumn {
       items(cities, key = { it.cityId }) { city ->
           Log.d("LazyColumnDemo", "City: ${city.name}")
           Row(modifier = Modifier
               .fillMaxWidth()
               .clickable { onCitySelected(city) }
               .padding(all = 12.dp),
               verticalAlignment = Alignment.CenterVertically) {
               Text(city.name.orEmpty(), Modifier.weight(1f))
               // Visual selection indicator if city was selected as a currentCity
               if (selectedCityId == city.cityId) {
                   Icon(
                       imageVector = Icons.Filled.Check,
                       contentDescription = "Checkmark"
                   )
               }
           }
       }
    }
}
// endregion

// region Top app bar
/**
 * A minimal top app bar with a navigation menu icon and a settings action.
 *
 * @param onMenuClick Called when the nav icon is tapped.
 * @param onSettingsClieck Called when the settings icon is tapped
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    onMenuClick: () -> Unit,
    onSettingsClieck: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClieck) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}
// endregion

// region Main content (weather + permission/empty/error states)

/**
 * Main content area that switches between: loaded weather, permission request,
 * location unavailable, generic error, and loading states.
 *
 * @param padding Insets provided by [Scaffold]; apply to avoid overlap.
 * @param viewModel Shared weather view model for state/data operations.
 * @param onRequestPermission Callback to trigger the location permission flow.
 * @param onEdit Invoked when the user wants to edit/select a city (opens sheet).
 */
@Composable
fun MainContent(
    padding: PaddingValues,
    viewModel: WeatherViewModel,
    onRequestPermission: () -> Unit,
    onEdit: () -> Unit,
) {
    val isFavourite by viewModel.isFavourite.collectAsState()

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when (val result = viewModel.locationResult) {
            is LocationFetchResult.Success -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WeatherCard(
                        weather = viewModel.weatherResponse,
                        isFavourite = isFavourite,
                        onEdit = onEdit,
                        onSave = {
                            viewModel.toggleFavourite()
                        }
                    )
                    WeatherDetailsCard(viewModel = viewModel)
                }
            }
            is LocationFetchResult.PermissionDenied -> {
                PermissionMissingView(
                    permissionName = "Location",
                    onRequestPermission = onRequestPermission
                )
            }
            is LocationFetchResult.LocationUnavailable -> {
                Text(text = stringResource(R.string.Location_Unavailable))
            }
            is LocationFetchResult.Error -> {
                Text("Error: ${result.throwable.message}")
            }
            null -> CircularProgressIndicator()
        }

        WeatherForecastCard(state = viewModel.dailyWeatherState)

    }
}

// endregion

//@Preview
//@Composable
//fun MainScreenPreview() {
//    val fakeViewModel = remember {
//        WeatherViewModel(StubLocationService(w))
//    }
//    WeatherDetails(viewModel = fakeViewModel)
//}