package com.animestudios.animeapp.utils

import android.content.Context
import android.content.pm.PackageManager

object AppUtils {

    // Function to get the current app version
    fun getAppVersion(context: Context): String? {
        return try {
            // Retrieve the version name from the package manager
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName // This will return the version name e.g., "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }
}
