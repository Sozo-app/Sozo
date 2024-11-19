package com.animestudios.animeapp.ui.screen.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.LoginScreenBinding
import com.animestudios.animeapp.tools.logError
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginScreen : Fragment() {
    private var _binding: LoginScreenBinding? = null
    private val binding get() = _binding!!


    private val model by viewModels<MainViewModelImp>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.login.setOnClickListener {
            val clientID = 14066
            val data = arguments?.getInt("selected", 1)
            println(data)

            if (!isCustomTabsAvailable(requireContext())) {
                if (data != null) {
                    binding.webLogin.settings.javaScriptEnabled = true
                    binding.webLogin.clearCache(true)
                    binding.webLogin.clearHistory()
                    binding.webLogin.clearView()
                    binding.webLogin.clearSslPreferences()
                    binding.webLogin.clearMatches()
                    binding.webLogin.clearFormData()
                    binding.webLogin.settings.javaScriptCanOpenWindowsAutomatically = true;
                    binding.webLogin.settings.domStorageEnabled = true;
                    binding.webLogin.webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()
                            if (url.startsWith("senzo://animeapp")) {
                                val regex = Regex("""(?<=access_token=).+(?=&token_type)""")
                                val matchResult = regex.find(url)
                                println("ListenData $url")
                                saveData("selectedAccount", data ?: 1)
                                val accessToken = matchResult?.value ?: "" // Access token value

                                try {
                                    val selectedAccountType = readData<Int>("selectedAccount")

                                    var filename = "anilistToken"

                                    when (selectedAccountType) {
                                        1 -> {
                                            saveData("countAccount", 1)

                                            filename = "anilistToken"
                                            Anilist.token = accessToken

                                        }
                                        2 -> {
                                            saveData("countAccount", 2)
                                            filename = "anilistToken2"
                                            saveData("selectedAccount", 2)
                                            lifecycleScope.launch {
                                                Anilist.token2 = accessToken
                                                model.loadProfile() {
                                                    println("DATA SAVED ${Anilist.username}")
                                                    saveData("user2Image", Anilist.avatar)
                                                    saveData("user2Id", Anilist.userid)
                                                    saveData("user2Name", Anilist.username)
                                                }
                                            }

                                        }

                                        3 -> {

                                            saveData("selectedAccount", 2)
                                            saveData("countAccount", 3)
                                            filename = "anilistToken3"
                                            lifecycleScope.launch {
                                                Anilist.token3 =
                                                    accessToken

                                                model.loadProfile() {
                                                    saveData("user3Image", Anilist.avatar)
                                                    saveData("user3Id", Anilist.userid)
                                                    saveData("user3Name", Anilist.username)
                                                }
                                            }
                                        }
                                    }

                                    requireActivity().openFileOutput(filename, Context.MODE_PRIVATE)
                                        .use {
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
                                binding.webLogin.onPause()
                                binding.webLogin.removeAllViews()
                                binding.webLogin.destroy()
                                binding.webLogin.canGoBack()
                                binding.webLogin.goBack()
                                clearCookies(binding.webLogin)
                                clearCookies(requireContext())
                                startMainActivity(requireActivity())

                                return true
                            }

                            return super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                    binding.webLogin.loadUrl("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
                    binding.webLogin.visible()
                } else {
                    binding.webLogin.settings.javaScriptEnabled = true
                    binding.webLogin.clearCache(true)
                    binding.webLogin.clearHistory()
                    binding.webLogin.clearView()
                    binding.webLogin.clearSslPreferences()
                    binding.webLogin.clearMatches()
                    binding.webLogin.clearFormData()
                    binding.webLogin.settings.javaScriptCanOpenWindowsAutomatically = true;
                    binding.webLogin.settings.domStorageEnabled = true;
                    binding.webLogin.webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()
                            if (url.startsWith("senzo://animeapp")) {
                                val regex = Regex("""(?<=access_token=).+(?=&token_type)""")
                                val matchResult = regex.find(url)
                                println("ListenData $url")
                                val accessToken = matchResult?.value ?: "" // Access token value

                                val filename = "anilistToken"
                                Anilist.token = accessToken


                                requireActivity().openFileOutput(filename, Context.MODE_PRIVATE).use {
                                    it.write(Anilist.token!!.toByteArray())
                                }

                                binding.webLogin.onPause()
                                binding.webLogin.removeAllViews()
                                binding.webLogin.destroy()
                                binding.webLogin.canGoBack()
                                binding.webLogin.goBack()
                                clearCookies(binding.webLogin)
                                clearCookies(requireContext())
                                startMainActivity(requireActivity())

                                return true
                            }

                            return super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                    binding.webLogin.loadUrl("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
                    binding.webLogin.visible()
                }
            } else {
                if (data != null) {
                    binding.webLogin.settings.javaScriptEnabled = true
                    binding.webLogin.clearCache(true)
                    binding.webLogin.clearHistory()
                    binding.webLogin.clearView()
                    binding.webLogin.clearSslPreferences()
                    binding.webLogin.clearMatches()
                    binding.webLogin.clearFormData()
                    binding.webLogin.settings.javaScriptCanOpenWindowsAutomatically = true;
                    binding.webLogin.settings.domStorageEnabled = true;
                    binding.webLogin.webViewClient = object : WebViewClient() {

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()
                            if (url.startsWith("senzo://animeapp")) {
                                val regex = Regex("""(?<=access_token=).+(?=&token_type)""")
                                val matchResult = regex.find(url)
                                println("ListenData $url")
                                saveData("selectedAccount", data ?: 1)
                                val accessToken = matchResult?.value ?: "" // Access token value

                                try {
                                    val selectedAccountType = readData<Int>("selectedAccount")

                                    var filename = "anilistToken"

                                    when (selectedAccountType) {
                                        1 -> {
                                            saveData("countAccount", 1)

                                            filename = "anilistToken"
                                            Anilist.token = accessToken

                                        }
                                        2 -> {
                                            saveData("countAccount", 2)
                                            filename = "anilistToken2"
                                            saveData("selectedAccount", 2)
                                            lifecycleScope.launch {
                                                Anilist.token2 = accessToken
                                                model.loadProfile() {
                                                    println("DATA SAVED ${Anilist.username}")
                                                    saveData("user2Image", Anilist.avatar)
                                                    saveData("user2Id", Anilist.userid)
                                                    saveData("user2Name", Anilist.username)
                                                }
                                            }

                                        }

                                        3 -> {

                                            saveData("selectedAccount", 2)
                                            saveData("countAccount", 3)
                                            filename = "anilistToken3"
                                            lifecycleScope.launch {
                                                Anilist.token3 =
                                                    accessToken

                                                model.loadProfile() {
                                                    saveData("user3Image", Anilist.avatar)
                                                    saveData("user3Id", Anilist.userid)
                                                    saveData("user3Name", Anilist.username)
                                                }
                                            }
                                        }
                                    }

                                    requireActivity().openFileOutput(filename, Context.MODE_PRIVATE)
                                        .use {
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
                                binding.webLogin.onPause()
                                binding.webLogin.removeAllViews()
                                binding.webLogin.destroy()
                                binding.webLogin.canGoBack()
                                binding.webLogin.goBack()
                                clearCookies(binding.webLogin)
                                clearCookies(requireContext())
                                startMainActivity(requireActivity())

                                return true
                            }

                            return super.shouldOverrideUrlLoading(view, request)
                        }
                    }
                    binding.webLogin.loadUrl("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
                    binding.webLogin.visible()
                } else {
                    Anilist.loginIntent(requireContext())
                }
            }

        }
    }

    private fun isCustomTabsAvailable(context: Context): Boolean {
        val packageManager = context.packageManager
        val activityIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://anilist.co/api/v2/oauth/authorize?client_id=14066&response_type=token")
        )
        val resolvedActivities =
            packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in resolvedActivities) {
            val serviceIntent = Intent().apply {
                action = "android.support.customtabs.action.CustomTabsService"
                `package` = info.activityInfo.packageName
            }
            if (packageManager.resolveService(serviceIntent, 0) != null) {
                return true
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webLogin.onPause()
        binding.webLogin.removeAllViews()
        binding.webLogin.destroy()

    }

}