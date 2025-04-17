package com.animestudios.animeapp.ui.screen.notification.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.ui.screen.browse.page.allanime.AllAnimeFragment
import com.animestudios.animeapp.ui.screen.browse.page.genre.GenreFragment
import com.animestudios.animeapp.ui.screen.login.LoginScreen
import com.animestudios.animeapp.ui.screen.notification.page.AllPage
import com.animestudios.animeapp.ui.screen.notification.page.NotificationSelectedScreen

class NotificationTabAdapter(var arrayList: ArrayList<String>, fragmentManager: FragmentActivity) :
    FragmentStateAdapter(fragmentManager) {
    private val notificationTabsWithType = listOf(
        "Following" to "FollowingNotification",
        "Airing" to "AiringNotification",
        "Likes" to "ActivityLikeNotification",
        "Messages" to "ActivityMessageNotification",
        "Mentions" to "ActivityMentionNotification",
        "Replies" to "ActivityReplyNotification",
        "Thread Mentions" to "ThreadCommentMentionNotification",
        "Thread Replies" to "ThreadCommentReplyNotification"
    )

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllPage()
            else -> {
                val screen = NotificationSelectedScreen()
                screen.arguments = Bundle().apply {
                    putString(
                        "selected_category",
                        notificationTabsWithType.find { it.first == arrayList[position] }?.first
                    )
                }
                screen
            }
        }
    }
}