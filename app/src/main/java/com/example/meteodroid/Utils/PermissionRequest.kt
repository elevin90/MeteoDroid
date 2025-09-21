package com.example.meteodroid.Utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun rememberPermissionRequesterWithSnackbar(
    vararg permissions: String,
    snackarHostState: SnackbarHostState,
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null
): ()-> Unit {
    // Get access to the current Android Context
    val context = LocalContext.current

    if (context.arePermissionsGranted(*permissions)) {
        onGranted()
    }

    // Launcher to request multiple permissions at once
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { resultMap ->
            val granted =  resultMap.all { it.value }
            // If all requested permissions are granted
            if (granted) {
                onGranted()
            } else {
                // Show Snackbar and call onDenied if provided
                onDenied?.invoke()
                CoroutineScope(Dispatchers.Main).launch {
                    snackarHostState.showSnackbar("Permission Denied")
                }
            }
        }
    )
    // First check starts
    // Return a lambda that checks if permission is already granted.
    // If not, it launches the permission request.
    return {
        // If all requested permissions are granted, calls onGranted
        if (context.arePermissionsGranted(*permissions)) {
            onGranted()
        } else {
            // Launches launcher
            launcher.launch(arrayOf(*permissions))
        }
    }
}