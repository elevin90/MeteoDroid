package com.example.meteodroid.Utils
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

    fun Context.arePermissionsGranted(vararg permissions: String): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
}