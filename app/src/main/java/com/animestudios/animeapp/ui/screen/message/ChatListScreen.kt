package com.animestudios.animeapp.ui.screen.message

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.ChatListScreenBinding
import com.animestudios.animeapp.viewmodel.imp.ChatListViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatListScreen : Fragment() {
    private var _binding: ChatListScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatListViewModelImp by viewModels()
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ChatListScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val typedValueBg = TypedValue()
        val themeBg: Resources.Theme = requireContext().theme
        themeBg.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurface, typedValueBg, true
        )
        @ColorInt val bgColor: Int = typedValueBg.data
        requireActivity().window.statusBarColor = bgColor
        adapter = ChatListAdapter { item ->
            Log.d("GGG", "onViewCreated:${item.chatId} ")
            findNavController().navigate(R.id.messageScreen, Bundle().apply {
                putInt("otherId", item.otherUserId)
            })
        }

        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatListScreen.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.chats.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}