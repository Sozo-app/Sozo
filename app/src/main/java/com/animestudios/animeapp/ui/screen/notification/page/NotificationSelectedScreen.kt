package com.animestudios.animeapp.ui.screen.notification.page

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.NotificationsQuery
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.response.ActivityLikeNotification
import com.animestudios.animeapp.anilist.response.ActivityMentionNotification
import com.animestudios.animeapp.anilist.response.ActivityMessageNotification
import com.animestudios.animeapp.anilist.response.ActivityReplyNotification
import com.animestudios.animeapp.anilist.response.AiringNotification
import com.animestudios.animeapp.anilist.response.AniNotification
import com.animestudios.animeapp.anilist.response.FollowingNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentMentionNotification
import com.animestudios.animeapp.anilist.response.ThreadCommentReplyNotification
import com.animestudios.animeapp.databinding.NotificationSelectedScreenBinding
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.ui.screen.notification.adapter.NotificationAdapter
import com.animestudios.animeapp.viewmodel.imp.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationSelectedScreen : Fragment() {

    private var _binding: NotificationSelectedScreenBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private var selectedCategory: String? = null
    private lateinit var pageInfo: NotificationsQuery.PageInfo

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NotificationSelectedScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedCategory = requireArguments().getString("selected_category")

        notificationAdapter = NotificationAdapter()
        binding.notificationRv.adapter = notificationAdapter

        observeNotifications()
        viewModel.fetchNotifications(page = 1)
        setupPagination()
    }

    private fun observeNotifications() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingView.root.isVisible = isLoading
            binding.notificationRv.isVisible = !isLoading
        }

        viewModel.notifications.observe(viewLifecycleOwner) { (pageInnfo, list) ->
            binding.loadingView.root.isVisible = false
            binding.notificationRv.isVisible = true
            if (viewModel.currentPage == 1) {
                pageInfo = pageInnfo
                val filtered = filterByCategory(list, selectedCategory)
                notificationAdapter.setItems(filtered)
            } else {
                val filtered = filterByCategory(list, selectedCategory)
                notificationAdapter.setItemsForPaging(filtered)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                snackString(it)
            }
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

    private fun filterByCategory(
        items: List<AniNotification>,
        category: String?
    ): List<AniNotification> = when (category) {
        "Following" -> items.filterIsInstance<FollowingNotification>()
        "Airing" -> items.filterIsInstance<AiringNotification>()
        "Likes" -> items.filterIsInstance<ActivityLikeNotification>()
        "Messages" -> items.filterIsInstance<ActivityMessageNotification>()
        "Mentions" -> items.filterIsInstance<ActivityMentionNotification>()
        "Replies" -> items.filterIsInstance<ActivityReplyNotification>()
        "Thread Mentions" -> items.filterIsInstance<ThreadCommentMentionNotification>()
        "Thread Replies" -> items.filterIsInstance<ThreadCommentReplyNotification>()
        else -> items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}