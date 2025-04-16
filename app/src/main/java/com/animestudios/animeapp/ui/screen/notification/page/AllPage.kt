package com.animestudios.animeapp.ui.screen.notification.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.FragmentAllPageBinding
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.screen.notification.adapter.NotificationAdapter
import com.animestudios.animeapp.viewmodel.imp.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllPage : Fragment() {
    private var _binding: FragmentAllPageBinding? = null
    private val binding get() = _binding!!
    private val model by viewModels<NotificationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAllPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            delay(2000)
            model.fetchNotifications(page = 1)

        }
        observeModel()
    }

    private fun observeModel() {
        model.isLoading.observe(viewLifecycleOwner) {
            binding.loadingView.root.isVisible = it
            binding.notificationRv.isVisible = !it
        }
        model.error.observe(viewLifecycleOwner) {
            snackString(it ?: requireActivity().getString(R.string.something_went_wrong))
        }
        model.notifications.observe(viewLifecycleOwner) {
            val notificationAdapter = NotificationAdapter()
            binding.notificationRv.adapter = notificationAdapter
            notificationAdapter.setItems(it)
        }
    }
}