package com.animestudios.animeapp.ui.screen.message

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.MessageScreenBinding
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.snackString
import com.animestudios.animeapp.viewmodel.imp.MessageViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MessageScreen : Fragment(R.layout.message_screen) {

    private val vm: MessageViewModelImpl by viewModels()
    private var _b: MessageScreenBinding? = null
    private val b get() = _b!!

    private val meId = Anilist.userid!!
    private val otherId  by lazy {
        if (Anilist.userid ==6136028) requireArguments().getInt("otherId") else 6136028
    }

    private lateinit var adapter: MessageAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = MessageScreenBinding.bind(view)

        // back navigation
        b.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
        requireActivity().window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // RecyclerView + Adapter
        adapter = MessageAdapter(meId)
        b.messageRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MessageScreen.adapter
        }

        // start all streams
        vm.startConversation(meId, otherId)

        // 1) Other profile → toolbar
        viewLifecycleOwner.lifecycleScope.launch {
            vm.otherProfile
                .filterNotNull()
                .collectLatest { user ->
                    b.fromChatName.text = user.name
                    b.profileImageView.loadImage(user.avatarUrl)
                }
        }

        // 2) Presence → subtitle & dot
        viewLifecycleOwner.lifecycleScope.launch {
            vm.otherStatus.collectLatest { status ->
                b.lastSeenToolbarTxt.text =
                    if (status.online) "Online"
                    else "Last seen: ${status.lastSeen.toTimeString()}"
                b.verifedBadge.isVisible = otherId == 6136028
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.chatMessages.collectLatest { list ->
                adapter.submitList(list)
                if (list.isNotEmpty())
                    b.messageRv.scrollToPosition(list.lastIndex)
                vm.markMessagesRead(meId, otherId)

            }

        }

        b.sendMessageButton.setOnClickListener {
            val txt = b.messageInput.text.toString().trim()
            if (txt.isNotEmpty()) {
                vm.sendMessage(meId, otherId, txt)
                b.messageInput.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    fun Long.toTimeString(): String {
        val nowCal = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { timeInMillis = this@toTimeString }
        return if (nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
            && nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
        } else {
            SimpleDateFormat("dd MMM HH:mm", Locale.getDefault()).format(Date(this))
        }
    }
}
