package com.animestudios.animeapp.ui.screen.profile.page.theme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.animestudios.animeapp.databinding.ThemeScreenBinding
import com.animestudios.animeapp.loadThemes
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.screen.profile.adapter.ThemeAdapter
import com.google.android.material.snackbar.Snackbar

class ThemeScreen : Fragment() {
    private var _binding: ThemeScreenBinding? = null

    private val binding get() = _binding!!


    private val adapter by lazy { ThemeAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = ThemeScreenBinding.inflate(inflater, container, false)


        return binding.root
    }

    fun restartApp() {
        Snackbar.make(
            binding.root,
            "Restart App ?", Snackbar.LENGTH_SHORT
        ).apply {
            val mainIntent =
                Intent.makeRestartActivityTask(
                    context.packageManager.getLaunchIntentForPackage(
                        context.packageName
                    )!!.component
                )
            setAction("Do it!") {
                context.startActivity(mainIntent)
                Runtime.getRuntime().exit(0)
            }
            show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            adapter.submitList(loadThemes())
            binding.themeRv.adapter = adapter
            adapter.setItemClickListener { themeModel, position ->
                restartApp()
                binding.root.context.getSharedPreferences("Sozo", Context.MODE_PRIVATE)
                    .edit().putString("theme", themeModel.title).apply()
                adapter.notifyDataSetChanged()
            }
        }
    }

}