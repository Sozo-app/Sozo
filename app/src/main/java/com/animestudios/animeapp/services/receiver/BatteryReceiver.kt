package com.animestudios.animeapp.services.receiver

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.BatteryManager
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.animestudios.animeapp.R
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.script.RenderScriptBlur
import com.animestudios.animeapp.settings.UISettings
import com.google.android.material.snackbar.Snackbar

class BatteryReceiver : BroadcastReceiver() {

    private var isNotificationShown = false
    private var isActionClicked = false

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val isCharging = intent.getIntExtra(
                BatteryManager.EXTRA_STATUS,
                -1
            ) == BatteryManager.BATTERY_STATUS_CHARGING

            if (level <= 32 && !isCharging && !isActionClicked) {
                showNotification(context, level)
                val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = false))
            } else if (level > 32 || isCharging) {
                val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = true))
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showNotification(context: Context, level: Int) {
        if (level <= 32 && !isNotificationShown) {
            isNotificationShown = true
            val customSnackbarView = LayoutInflater.from(context)
                .inflate(R.layout.snack_bar, null, false)
            val actionTextView: TextView = customSnackbarView.findViewById(R.id.snackbar_action)
            actionTextView.setOnClickListener {
                isActionClicked = true  // Disable functionality after action click
                val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = true))
                isNotificationShown = false
            }
            val rootView = (context as? Activity)
                ?.window?.decorView?.findViewById<View>(android.R.id.content)
                ?: return

            rootView.post {
                // Create the snackbar first so that it measures its dimensions
                val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
                val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
                snackbarLayout.setPadding(0, 0, 0, 0)
                // Use a transparent background for the snackbar container
                snackbarLayout.setBackgroundColor(Color.TRANSPARENT)

                // Add your custom view
                snackbarLayout.addView(customSnackbarView, 0)

                // Measure the snackbar to get its height (if not already measured)
                snackbarLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val snackbarHeight = snackbarLayout.measuredHeight

                // Determine the y-coordinate where the snackbar appears (usually bottom)
                // For example, assuming the snackbar is at the bottom:
                val yPos = rootView.height - snackbarHeight

                // Capture only the area behind the snackbar:
                val bitmap = Bitmap.createBitmap(
                    rootView.width,
                    snackbarHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                // Translate the canvas so that you capture the area at the bottom of the screen
                canvas.translate(0f, -yPos.toFloat())
                rootView.draw(canvas)

                // Blur only the snackbar area
                val blurredBitmap = RenderScriptBlur.blur(context, bitmap, 25f)
                // Apply the blurred bitmap as the background of the custom snackbar view:
                customSnackbarView.background = BitmapDrawable(context.resources, blurredBitmap)

                // Start animations
                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                customSnackbarView.startAnimation(fadeIn)
                customSnackbarView.startAnimation(slideUp)
                snackbar.setDuration(4000)
                snackbar.show()
            }
        } else if (level > 32) {
            isNotificationShown = false
        }
    }



}
