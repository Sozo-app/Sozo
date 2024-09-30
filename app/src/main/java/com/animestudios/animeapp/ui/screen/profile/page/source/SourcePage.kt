package com.animestudios.animeapp.ui.screen.profile.page.source

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.databinding.SourcePageBinding
import com.animestudios.animeapp.others.SourceList
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.ui.screen.profile.adapter.SourceHeaderAdapter
import com.google.android.material.snackbar.Snackbar

class SourcePage : Fragment() {

    private var _binding: SourcePageBinding? = null
    private val binding get() = _binding!!


    //

    private val adapter by lazy { SourceHeaderAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SourcePageBinding.inflate(inflater, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sourceHeaderRv.adapter = adapter
        binding.sourceToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        adapter.submitList(SourceList.sourceList)
        adapter.setItemClickListener {
            saveData("selectedSource", it)
            adapter.notifyDataSetChanged()
            restartApp()

        }
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

}