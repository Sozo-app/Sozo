package com.animestudios.animeapp.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.animestudios.animeapp.databinding.BottomsheetMediaListBinding
import com.animestudios.animeapp.media.Media
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.Serializable

class MediaListSmallDialog:BottomSheetDialogFragment() {
    private var _binding: BottomsheetMediaListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomsheetMediaListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
    companion object {
        fun newInstance(m: Media): MediaListSmallDialog =
            MediaListSmallDialog().apply {
                arguments = Bundle().apply {
                    putSerializable("media", m as Serializable)
                }
            }
    }

}