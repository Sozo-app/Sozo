package com.animestudios.animeapp.base

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.animestudios.animeapp.swipe.model.SliderConfig
import com.animestudios.animeapp.swipe.model.SliderInterface
import com.animestudios.animeapp.swipe.model.SliderListener
import com.animestudios.animeapp.swipe.slider.SliderPanel


abstract class BaseSwipeableFragment<VB : ViewBinding>(
    private val inflate: Inflate<VB>
) : BaseFragment<VB>(inflate), SliderListener {

    private var rootFrameLayout: FrameLayout? = null
    private var contentView: View? = null

    protected var sliderInterface: SliderInterface? = null
        private set

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contentView = super.onCreateView(inflater, container, savedInstanceState)
        rootFrameLayout = FrameLayout(requireContext()).also {
            it.setBackgroundColor(Color.TRANSPARENT)
            it.removeAllViews()
            it.addView(contentView)
        }
        return rootFrameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSliderPanel()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun setUpSliderPanel() {
        contentView?.let { content ->
            if (sliderInterface == null) {
                val parent = content.parent as? ViewGroup ?: return
                val params = content.layoutParams
                parent.removeView(content)

                // Setup the slider panel and attach it
                val panel = SliderPanel(requireContext(), content, getSliderConfig())

                panel.addView(content)
                parent.addView(panel, 0, params)

                panel.setOnPanelSlideListener(
                    FragmentPanelSlideListener(findNavController(), getSliderConfig())
                )

                sliderInterface = panel.defaultInterface
            }
        }
    }

    override fun onDestroyView() {
        rootFrameLayout?.removeAllViews()
        rootFrameLayout = null
        sliderInterface = null
        super.onDestroyView()
    }

    protected open fun getSliderConfig(): SliderConfig {
        return SliderConfig.Builder()
            .listener(this)
            .edgeSize(0.5f)
            .edge(false)
            .touchDisabledViews(getTouchDisabledViews())
            .build()
    }

    protected open fun getTouchDisabledViews(): List<View> {
        return emptyList()
    }

    override fun onSlideStateChanged(state: Int) {}

    override fun onSlideChange(percent: Float) {}

    override fun onSlideOpened() {}

    override fun onSlideClosed(): Boolean {
        return false
    }
}

internal class FragmentPanelSlideListener(
    private val navController: NavController,
    private val config: SliderConfig
) : SliderPanel.OnPanelSlideListener {
    override fun onStateChanged(state: Int) {
        config.listener?.onSlideStateChanged(state)
    }

    override fun onClosed() {
        if (config.listener != null) {
            if (config.listener.onSlideClosed()) {
                return
            }
        }

        navController.navigateUp()
    }

    override fun onOpened() {
        config.listener?.onSlideOpened()
    }

    override fun onSlideChange(percent: Float) {
        config.listener?.onSlideChange(percent)
    }
}