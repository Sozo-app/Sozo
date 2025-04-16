package com.animestudios.animeapp.utils

import android.content.Context
import android.content.pm.PackageManager

fun getAppVersionCode(context: Context): String {
    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionCode = packageInfo.versionCode  // e.g., 1
        return versionCode.toString()
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return "-1"
}

fun getAppVersionName(context: Context): String {
    try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = packageInfo.versionName  // e.g., 1.0.0
        return versionName  // e.g., 1.0.0
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return "-1"
}
