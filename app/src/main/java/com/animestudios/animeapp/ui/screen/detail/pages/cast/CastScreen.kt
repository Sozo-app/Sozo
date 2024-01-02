package com.animestudios.animeapp.ui.screen.detail.pages.cast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.databinding.CastScreenBinding
import com.animestudios.animeapp.media.Character
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.screen.detail.adapter.CastPageAdapter
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl

class CastScreen : Fragment() {

    private var _binding: CastScreenBinding? = null
    private val binding get() = _binding!!
    private val model by activityViewModels<DetailsViewModelImpl>()
    lateinit var uiSettings: UISettings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CastScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiSettings = readData("ui_settings", toast = false) ?: UISettings().apply {
            saveData(
                "ui_settings",
                this
            )
        }
        model.getMedia().observe(viewLifecycleOwner) {
            if (it != null) {
                val supportList = ArrayList<com.animestudios.animeapp.media.Character>()
                val mainList = ArrayList<Character>()
                it.characters!!.onEach {
                    if (it.role.equals("SUPPORTING")) {
                        supportList.add(it)
                    } else {
                        mainList.add(it)
                    }
                }

                val adapter = CastPageAdapter(this, mainList, supportList)
                binding.castPageRv.adapter = adapter

            }
        }

    }

}