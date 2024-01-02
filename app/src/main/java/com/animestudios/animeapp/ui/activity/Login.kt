package com.animestudios.animeapp.ui.activity

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.logger
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.startMainActivity
import com.animestudios.animeapp.tools.logError

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data: Uri? = intent?.data
        logger(data.toString())
        try {
            val selectedAccountType = Anilist.selected

            var filename = "anilistToken"

            when (selectedAccountType) {
                1 -> {
                    saveData("countAccount", 1)

                    filename ="anilistToken"
                    Anilist.token =
                        Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value

                }
                2 -> {
                    saveData("countAccount", 2)
                    filename ="anilistToken2"
                    saveData("selectedAccount",2)

                    Anilist.token2 =
                        Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value

                }

                3 -> {

                    saveData("selectedAccount",3)
                    saveData("countAccount", 3)
                    filename ="anilistToken3"
                    Anilist.token3 =
                        Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value

                }
            }




            this.openFileOutput(filename, Context.MODE_PRIVATE).use {
                when (selectedAccountType) {
                    1 -> {
                        it.write(Anilist.token!!.toByteArray())

                    }
                    2 -> {
                        it.write(Anilist.token2!!.toByteArray())

                    }

                    else -> {
                        it.write(Anilist.token3!!.toByteArray())
                    }
                }
            }
        } catch (e: Exception) {
            logError(e)
        }
        startMainActivity(this)
    }


}