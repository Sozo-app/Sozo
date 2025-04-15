package com.animestudios.animeapp.ui.screen.main.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.BottomSheetAccountBinding
import com.animestudios.animeapp.ui.screen.main.MainScreen
import com.animestudios.animeapp.viewmodel.imp.MainViewModelImp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountBottomSheetDialog(private val activity: MainScreen) :
    com.animestudios.animeapp.BottomSheetDialogFragment() {
    private var _binding: BottomSheetAccountBinding? = null
    private val binding get() = _binding!!

    private val model by viewModels<MainViewModelImp>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedAccount = readData<Int>("selectedAccount") ?: 1//1
        val selectedAccountCount = readData<Int>("countAccount") ?: 1//2
        binding.apply {
            when (selectedAccountCount) {
                1 -> {
                    binding.addAccountContainer.visible()
                    model.getGenresAndTags(requireActivity())
                    lifecycleScope.launch {
                        model.loadProfile() {
                            devName1.text = Anilist.username
                            devProfile1.loadImage(Anilist.avatar)
                            selected1.isChecked = true
                        }
                    }
                }

                2 -> {
                    binding.addAccountContainer.visible()
                    binding.accountContainer.visible()
                    devName1.text = readData("userName") ?: "Account Name"
                    devProfile1.loadImage(readData("userImage") ?: "")
                    //Account 2
                    binding.accountContainer2.visible()
                    binding.accountContainer3.gone()
                    devName2.text = readData("user2Name") ?: "Account 2 Name"
                    devProfile2.loadImage(readData("user2Image") ?: "")
                    model.getGenresAndTags(requireActivity())
                    when (selectedAccount) {
                        1 -> {
                            selected1.visible()
                            selected2.gone()
                            selected1.isChecked = true
                        }
                        2 -> {
                            selected1.gone()
                            selected2.visible()
                            selected2.isChecked = true
                        }
                    }


                }
                3 -> {
                    binding.addAccountContainer.gone()

                    devName1.text = readData("userName") ?: "Account Name"
                    devProfile1.loadImage(readData("userImage") ?: "")
                    //Account 2
                    binding.accountContainer2.visible()
                    binding.accountContainer3.gone()
                    devName2.text = readData("user2Name") ?: "Account 2 Name"
                    devProfile2.loadImage(readData("user2Image") ?: "")

                    binding.accountContainer3.visible()
                    devName3.text = readData("user3Name") ?: "Account 3 Name"
                    devProfile3.loadImage(readData("user3Image") ?: "")

                    when (selectedAccount) {
                        1 -> {
                            selected1.visible()
                            selected1.isChecked = true
                            selected2.gone()
                            selected3.gone()
                        }
                        2 -> {

                            selected2.visible()
                            selected2.isChecked = true
                            selected3.gone()
                            selected1.gone()
                        }
                        3 -> {
                            selected3.visible()
                            selected3.isChecked = true
                            selected2.gone()
                            selected1.gone()
                        }
                    }
                }
            }

            addAccountBtn.setOnClickListener {
                if (selectedAccountCount == 1) {
                    val bundle = Bundle()
                    bundle.putInt("selected", 2)
                    dismiss()
                    activity.findNavController().navigate(
                        R.id.loginScreen,
                        bundle
                    )

                } else {
                    val bundle = Bundle()
                    bundle.putInt("selected", 3)
                    dismiss()
                    activity.findNavController().navigate(
                        R.id.loginScreen,
                        bundle
                    )

                }
            }

        }

        binding.accountContainer.setOnClickListener {
            binding.selected3.gone()
            binding.selected1.visible()
            binding.selected1.isChecked = true
            binding.selected2.gone()
            saveData("selectedAccount", 1)
            model.getGenresAndTags(requireActivity())
            dismiss()
            startMainActivity(
                requireActivity()
            )
        }
        binding.accountContainer2.setOnClickListener {
            binding.selected3.gone()
            binding.selected2.visible()
            binding.selected2.isChecked = true
            binding.selected1.gone()
            saveData("selectedAccount", 2)
            model.getGenresAndTags(requireActivity())
            dismiss()
          startMainActivity(
              requireActivity()
          )
        }
        binding.accountContainer3.setOnClickListener {
            binding.selected2.gone()
            binding.selected3.visible()
            binding.selected3.isChecked = true
            binding.selected1.gone()
            saveData("selectedAccount", 3)
            model.getGenresAndTags(requireActivity())
            dismiss()
            startMainActivity(
                requireActivity()
            )
        }
    }

}
