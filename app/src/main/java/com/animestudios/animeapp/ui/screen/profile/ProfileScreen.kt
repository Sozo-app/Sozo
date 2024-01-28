package com.animestudios.animeapp.ui.screen.profile

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.*
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.animationTransaction
import com.animestudios.animeapp.tools.slideStart
import com.animestudios.animeapp.tools.slideTop
import com.animestudios.animeapp.tools.slideUp
import com.animestudios.animeapp.ui.screen.profile.adapter.ProfileAdapter
import com.google.android.material.appbar.AppBarLayout


class ProfileScreen : Fragment(), AppBarLayout.OnOffsetChangedListener {
    private val adapter = ProfileAdapter()
    private val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()

    private var _binding: com.animestudios.animeapp.databinding.FragmentProfileScreenBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = com.animestudios.animeapp.databinding.FragmentProfileScreenBinding.inflate(
            inflater,
            container,
            false
        )
        return _binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            adapter.submitList(loadProfileCategory())
            profileRv.adapter = adapter
            detailAppbar.addOnOffsetChangedListener(this@ProfileScreen)
            cardView4.slideUp(700, 1)
            circleImageView.slideStart(700, 1)
            profileName.slideStart(700, 1)
            linearLayout5.setOnClickListener {
                findNavController().navigate(R.id.themeScreen, null, animationTransaction().build())
            }

        }

    }

    private var isCollapsed = false
    private var isCollapsedForAppbar = false
    private val percent = 70
    private var screenWidth = 0f
    private var mMaxScrollSize = 0

    @SuppressLint("ResourceType")
    override fun onOffsetChanged(appBar: AppBarLayout, i: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange
        val percentage = Math.abs(i) * 100 / mMaxScrollSize
        val cap = MathUtils.clamp((percent - percentage) / percent.toFloat(), 0f, 1f)
        val duration = (300 * uiSettings.animationSpeed).toLong()

        binding.circleImageView.scaleX = 1f * cap
        binding.circleImageView.scaleY = 1f * cap

        binding.circleImageView.visibility =
            if (binding.circleImageView.scaleX == 0f) View.GONE else View.VISIBLE
        binding.circleImageView.visibility =
            if (binding.circleImageView.scaleX == 0f) View.GONE else View.VISIBLE


        if (percentage >= percent && !isCollapsed) {
            isCollapsed = true
            val typedValue = TypedValue()
            val theme: Resources.Theme = requireContext().theme
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnBackground,
                typedValue,
                true
            )
            @ColorInt val selectedcolor: Int = typedValue.data
            requireActivity().window.statusBarColor =selectedcolor
            ObjectAnimator.ofFloat(binding.detailAppbar, "translationX", 0f)
                .setDuration(duration).start()

        }
        if (percentage <= percent && isCollapsed) {
            isCollapsed = false
            requireActivity().window.statusBarColor =
                ContextCompat.getColor(requireActivity(), R.color.status)
            binding.cardView4.slideUp(700, 1)
            binding.circleImageView.slideUp(700, 1)
            binding.profileName.slideUp(700, 1)

        }

    }

}