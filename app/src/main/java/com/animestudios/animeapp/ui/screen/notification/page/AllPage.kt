package com.animestudios.animeapp.ui.screen.notification.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.FragmentAllPageBinding
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.screen.notification.adapter.NotificationAdapter
import com.animestudios.animeapp.viewmodel.imp.NotificationViewModel
import com.apollographql.apollo3.api.not
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllPage : Fragment() {
    private var _binding: FragmentAllPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var pageInfo: NotificationsQuery.PageInfo
    private lateinit var notificationAdapter: NotificationAdapter
    private val viewModel: NotificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationAdapter = NotificationAdapter()
        binding.notificationRv.adapter = notificationAdapter

        observeViewModel()

        viewModel.fetchNotifications(1)
        setupPagination()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingView.root.isVisible = isLoading
            binding.notificationRv.isVisible = !isLoading
        }

        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            binding.loadingView.root.isVisible = false
            binding.notificationRv.isVisible = true

            if (viewModel.currentPage == 1) {
                pageInfo = notifications.first
                notificationAdapter.setItems(notifications.second)
            } else {
                notificationAdapter.setItemsForPaging(notifications.second)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let { showError(it) }
        }
    }

    private fun setupPagination() {
        binding.notificationRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (::pageInfo.isInitialized) {
                    if (!recyclerView.canScrollVertically(1) && pageInfo.hasNextPage!!) {
                        viewModel.loadNextPage()
                    }
                }
            }
        })
    }

    private fun showError(message: String) {
        snackString(message)
    }
}