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
            val customSnackBarView = LayoutInflater.from(context)
                .inflate(R.layout.snack_bar, null, false)
            val actionTextView: TextView = customSnackBarView.findViewById(R.id.snackbar_action)
            actionTextView.setOnClickListener {
                isActionClicked = true
                val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
                saveData("ui_settings", uiSettings.copy(layoutAnimations = true))
                isNotificationShown = false

            }
            val rootView = (context as? Activity)
                ?.window?.decorView?.findViewById<View>(android.R.id.content)
                ?: return

            rootView.post {
                val snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_LONG)
                val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
                snackbarLayout.setPadding(0, 0, 0, 0)
                snackbarLayout.setBackgroundColor(Color.TRANSPARENT)

                snackbarLayout.addView(customSnackBarView, 0)

                snackbarLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                val snackbarHeight = snackbarLayout.measuredHeight

                val yPos = rootView.height - snackbarHeight
                val bitmap = Bitmap.createBitmap(
                    rootView.width,
                    snackbarHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                canvas.translate(0f, -yPos.toFloat())
                rootView.draw(canvas)

                val blurredBitmap = RenderScriptBlur.blur(context, bitmap, 25f)
                customSnackBarView.background = BitmapDrawable(context.resources, blurredBitmap)

                val fadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in)
                val slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up)
                customSnackBarView.startAnimation(fadeIn)
                customSnackBarView.startAnimation(slideUp)
                snackbar.setDuration(4000)
                snackbar.show()
            }
        } else if (level > 32) {
            isNotificationShown = false
        }
    }



}
