package com.animestudios.animeapp.ui.screen.profile

import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.tools.Resource
import com.animestudios.animeapp.tools.animationTransaction
import com.animestudios.animeapp.tools.slideStart
import com.animestudios.animeapp.tools.slideTop
import com.animestudios.animeapp.tools.slideUp
import com.animestudios.animeapp.ui.screen.profile.adapter.ProfileAdapter
import com.animestudios.animeapp.viewmodel.imp.ProfileViewModelImpl
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class ProfileScreen : Fragment(), AppBarLayout.OnOffsetChangedListener {
    private val adapter = ProfileAdapter()
    private val uiSettings = readData<UISettings>("ui_settings") ?: UISettings()
    private val model by viewModels<ProfileViewModelImpl>()

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
        screenWidth = resources.displayMetrics.widthPixels.toFloat()
        return _binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {


        }
        model.error.onEach {
            snackString(it)
        }.launchIn(lifecycleScope)
        model.userData.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Error -> {
                    binding.nestedScrollView.visible()
                }

                Resource.Loading -> {
                    binding.nestedScrollView.gone()

                }

                is Resource.Success -> {

                    binding.profileName.visible()
                    binding.profileBg2.visible()
                    binding.circleImageView.visible()
                    binding.cardView4.visible()
                    binding.nestedScrollView.visible()
                    adapter.submitList(loadProfileCategory())
                    binding.profileRv.adapter = adapter
                    binding.detailAppbar.addOnOffsetChangedListener(this@ProfileScreen)
                    binding.cardView4.slideUp(700, 1)
                    binding.circleImageView.slideStart(700, 1)
                    binding.profileName.slideStart(700, 1)
                    adapter.setItemClickListenerGetPosition {

                        when (it) {
                            0 -> {
                                if (Anilist.userid == 6136028) {
                                    findNavController().navigate(
                                        R.id.chatListScreen,
                                        null,
                                        animationTransaction().build()
                                    )
                                } else {
                                    findNavController().navigate(
                                        R.id.messageScreen,
                                        null,
                                        animationTransaction().build()
                                    )
                                }
                            }

                            1 -> {
                                findNavController().navigate(
                                    R.id.themeScreen,
                                    null,
                                    animationTransaction().build()
                                )

                            }

                            2 -> {
                                findNavController().navigate(
                                    R.id.sourcePage,
                                    null,
                                    animationTransaction().build()
                                )

                            }

                            else -> {

                            }
                            //
                        }
                    }

                    val userResponse = it.data.user
                    lifecycleScope.launch {
                        binding.profileName.text = userResponse!!.name
                        Glide.with(this@ProfileScreen).load(userResponse.avatar?.medium)
                            .into(binding.circleImageView)
                        binding.profileBg.loadImage(userResponse.bannerImage)
                    }
                }
            }
        }


        model.loadProfile(Anilist.userid!!.toInt())

    }

    private var isCollapsed = false

    private var screenWidth = 0f
    private var mMaxScrollSize = 0

    private val collapseThreshold = 70             // percent
    private val uiSpeed get() = uiSettings.animationSpeed
    private val baseDuration get() = (800 * uiSpeed).toLong()
    override fun onOffsetChanged(appBar: AppBarLayout, offset: Int) {
        if (mMaxScrollSize == 0) mMaxScrollSize = appBar.totalScrollRange

        binding.profileBg2.translationY = offset / 2f
        binding.profileBg.translationY = offset / 2f

        val percentage = abs(offset) * 100 / mMaxScrollSize
        val scale = clamp((collapseThreshold - percentage) / collapseThreshold.toFloat(), 0f, 1f)
        binding.circleImageView.apply {
            scaleX = scale
            scaleY = scale
            visibility = if (scale == 0f) View.GONE else View.VISIBLE
        }

        if (percentage >= collapseThreshold && !isCollapsed) {
            isCollapsed = true
            animateCollapse()
        }
        if (percentage < collapseThreshold && isCollapsed) {
            isCollapsed = false
            animateExpand()
        }
    }

    private fun animateCollapse() {

        val fromColor = getThemeColor(com.google.android.material.R.attr.colorOnBackground)
        val toColor = getThemeColor(com.google.android.material.R.attr.colorOnBackground)
        ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
            duration = baseDuration
            addUpdateListener { animator ->
                requireActivity().window.statusBarColor = animator.animatedValue as Int
            }
        }.start()

        val avatarAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.circleImageView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f)
        )

        val cardAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.cardView4,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -20f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
        )

        val nameFade = ObjectAnimator.ofFloat(binding.profileName, View.ALPHA, 1f, 0f)

        AnimatorSet().apply {
            playTogether(avatarAnim, cardAnim, nameFade)
            duration = baseDuration
            interpolator = DecelerateInterpolator()
        }.start()
    }

    private fun animateExpand() {
        val fromColor = requireActivity().window.statusBarColor
        val toColor = getThemeColor(com.google.android.material.R.attr.colorOnBackground)
        ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
            duration = baseDuration
            addUpdateListener { animator ->
                requireActivity().window.statusBarColor = animator.animatedValue as Int
            }
        }.start()

        val avatarAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.circleImageView,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)
        ).apply {
            interpolator = OvershootInterpolator(1.3f)
        }

        val cardAnim = ObjectAnimator.ofPropertyValuesHolder(
            binding.cardView4,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f),
            PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        ).apply {
            interpolator = OvershootInterpolator(1.3f)
        }

        AnimatorSet().apply {
            playTogether(avatarAnim, cardAnim)
            duration = baseDuration
        }.start()

        binding.profileName.animate()
            .alpha(1f)
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .setDuration(baseDuration)
            .start()
    }

    override fun onDestroyView() {
        binding.detailAppbar.removeOnOffsetChangedListener(this)
        _binding = null
        super.onDestroyView()
    }

    private fun clamp(value: Float, min: Float, max: Float) =
        when {
            value < min -> min
            value > max -> max
            else -> value
        }
}