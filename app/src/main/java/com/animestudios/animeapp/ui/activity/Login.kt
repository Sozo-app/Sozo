package com.animestudios.animeapp.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.tools.logError
import com.animestudios.animeapp.logger
import com.animestudios.animeapp.startMainActivity

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent?.data
        logger(data.toString())
        try {
            Anilist.token =
                Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value
            val filename = "anilistToken"
            this.openFileOutput(filename, Context.MODE_PRIVATE).use {
                it.write(Anilist.token!!.toByteArray())
            }
        } catch (e: Exception) {
            logError(e)
        }
        startMainActivity(this)
    }


}