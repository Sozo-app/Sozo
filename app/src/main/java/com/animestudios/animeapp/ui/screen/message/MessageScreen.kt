package com.animestudios.animeapp.ui.screen.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.MessageScreenBinding
import com.animestudios.animeapp.model.Message
import com.animestudios.animeapp.tools.Result
import com.animestudios.animeapp.tools.collect
import com.animestudios.animeapp.tools.logError
import com.animestudios.animeapp.viewmodel.imp.MessageViewModelImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MessageScreen(
) : Fragment() {
    private var _binding: MessageScreenBinding? = null
    private val binding get() = _binding!!
    private var adapter: MessageAdapter? = null
    private val model by viewModels<MessageViewModelImpl>()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        // Inflate the layout for this fragment
//        _binding = MessageScreenBinding.inflate(inflater, container, false)
//        return binding.root
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.apply {
//            sendMessagesButtonSetUp()
//            setupMessages()
        }
    }

//    private fun sendMessagesButtonSetUp() {
//        binding?.sendMessageButton?.setOnClickListener {
//            val messageText = binding?.messageInput?.text.toString().trim()
//            if (messageText.isNotEmpty()) {
//                model.sendMessage(if (Anilist.userid == 6292651) 6136028 else 6292651, messageText)
//                binding?.messageInput?.setText("")
//                lifecycleScope.launch(Dispatchers.IO) {
//                    val lastPosition = model.messageList.count() + 1
//                    logError(Exception("$lastPosition"))
//                    withContext(Dispatchers.Main) {
//                        binding?.messageRv?.scrollToPosition(lastPosition)
//                    }
//                }
//            }
//        }
//    }

//    private fun setupMessages() {
//        collect(model.messageList) { messages ->
//            binding?.messageRv?.withModels {
//                when (messages) {
//                    is Result.Error -> {
//                        Toast.makeText(
//                            requireContext(),
//                            "Couldn't load messages",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        binding?.loadingIndicator?.text = "No messages here sorry :p"
//                    }
//
//                    Result.Loading -> {
//                        binding?.loadingIndicator?.isVisible = true
//                    }
//
//                    is Result.Success -> {
//                        binding?.loadingIndicator?.isVisible = false
//                        messages.data.forEach { message ->
//
//                            if (message.senderUserId == Anilist.userid) {
//                                senderMessage {
//                                    id(message.id)
//                                    message(message)
//                                }
//                            } else {
//                                recipientMessage {
//                                    id(message.id)
//                                    message(message)
//                                }
//                            }
//
//                            message.replies.forEach { reply ->
//                                if (reply.user.id == Anilist.userid) {
//                                    senderMessage {
//                                        id(reply.id)
//                                        message(
//                                            Message(
//                                                id = reply.id,
//                                                senderUserId = reply.user.id,
//                                                message = reply.message,
//                                                createdAt = reply.createdAt,
//                                                recipient = message.recipient,
//                                                messenger = message.messenger
//                                            )
//                                        )
//                                    }
//                                } else {
//                                    recipientMessage {
//                                        id(reply.id)
//                                        message(
//                                            Message(
//                                                id = reply.id,
//                                                senderUserId = reply.user.id,
//                                                message = reply.message,
//                                                createdAt = reply.createdAt,
//                                                recipient = message.recipient,
//                                                messenger = message.messenger
//                                            )
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//    }
