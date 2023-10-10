package com.animestudios.animeapp.ui.screen.list

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.MediaPageTransformer
import com.animestudios.animeapp.R
import com.animestudios.animeapp.Refresh
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ListScreenBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.loaded
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.selectedPosition
import com.animestudios.animeapp.setSlideIn
import com.animestudios.animeapp.setSlideUp
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.screen.anime.AnimeTitleWithScoreAdapter
import com.animestudios.animeapp.ui.screen.list.adapter.ListViewPagerAdapter
import com.animestudios.animeapp.viewmodel.imp.ListViewModelImp
import com.animestudios.animeapp.visible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.skydoves.powermenu.CircularEffect
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListScreen() : Fragment() {
    private var _binding: ListScreenBinding? = null
    private val binding get() = _binding!!
    private val scope = lifecycleScope
    private var selectedPositionF = 0
    private var screenWidth = 0f
    val model by viewModels<ListViewModelImp>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ListScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.lists.observe(this) {
            binding.listProgressBar.visibility = View.GONE
            binding.sort.visible()
            binding.listRefresh.visible()
//            binding.container.visible()
            binding.homeUserName.text = Anilist.username
            binding.homeUserEpisodesWatched.text = Anilist.episodesWatched.toString()
            binding.profilePhoto.loadImage(Anilist.avatar.toString())
            val keys = it.keys.toList()
            val values = it.values.toList()
            binding.listTypeName.text = keys[selectedPositionF]
            binding.listTypeSize.text = values[selectedPositionF].size.toString()


            binding.listTypeName.animation = setSlideIn(uiSettings)
            binding.listTypeSize.animation = setSlideIn(uiSettings)
            binding.sort.animation = setSlideUp(uiSettings)
            binding.listTabLayout.animation= setSlideUp(uiSettings)
            binding.horizontalSpace.animation= setSlideUp(uiSettings)

            val powerList = ArrayList<PowerMenuItem>()
            keys.onEach {
                powerList.add(PowerMenuItem(it))
            }
            initClicks(powerList, keys, values)
            update(values.toList().getOrNull(selectedPositionF)!!)
        }

    }


    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        screenWidth = resources.displayMetrics.run { widthPixels / density }
        scope.launch(Dispatchers.IO) {
            model.loadLists(
                true, Anilist.userid!!
            )
        }

    }


    private fun update(list: ArrayList<Media>) {
        val adapter = AnimeTitleWithScoreAdapter(this)
        adapter.submitLit(list)
        binding.listViewPager.layoutManager =
            GridLayoutManager(requireContext(), (screenWidth / 124f).toInt())
        binding.listViewPager.adapter = adapter
    }


    private fun initClicks(powerList:ArrayList<PowerMenuItem>,keys:List<String>,values:List<ArrayList<Media>>){
        binding.imageView3.setOnClickListener {
            val powerMenu = PowerMenu.Builder(requireContext()!!).addItemList(
                mutableListOf(
                    PowerMenuItem(requireContext().getString(R.string.sort_by_score)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_title)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_release_date)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_last_updated))
                )
            ).setAnimation(MenuAnimation.SHOW_UP_CENTER).setIsClipping(true)
                .setAutoDismiss(true).setMenuRadius(16f).setMenuShadow(16f)
                .setTextColor(ContextCompat.getColor(requireContext()!!, R.color.light_grey))
                .setTextGravity(Gravity.CENTER)
                .setCircularEffect(CircularEffect.INNER) // Shows circular revealed effects for the content view of the popup menu.
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)

                .setSelectedMenuColorResource(R.color.basic_color_500)
                .setMenuColor(requireActivity().getColor(R.color.banner_bg)).build()
            powerMenu.selectedPosition = selectedPosition
             // showing the popup menu as left-bottom aligns to the anchor.

                powerMenu.showAsAnchorLeftBottom(binding.imageView3)


            powerMenu.setOnMenuItemClickListener { position, item ->
                selectedPosition = position
                powerMenu.selectedPosition = position
                val sort = when (item.title) {
                    requireContext().getString(R.string.sort_by_score) -> "score"
                    requireContext().getString(R.string.sort_by_title) -> "title"
                    requireContext().getString(R.string.sort_by_release_date) -> "updatedAt"
                    requireContext().getString(R.string.sort_by_last_updated) -> "release"
                    else -> null
                }
                binding.listProgressBar.visibility = View.VISIBLE
                binding.listViewPager.adapter = null
                binding.sort.gone()
                scope.launch {
                    withContext(Dispatchers.IO) {
                        model.loadLists(
                            true, Anilist.userid!!, sort
                        )
                    }
                }
            }

        }
        binding.sort.setOnClickListener {
            val powerMenu = PowerMenu.Builder(requireContext()!!).addItemList(
                powerList
            ).setAnimation(MenuAnimation.SHOWUP_BOTTOM_RIGHT).setIsClipping(true)
                .setAutoDismiss(true).setMenuRadius(16f).setMenuShadow(16f)
                .setTextColor(ContextCompat.getColor(requireContext()!!, R.color.light_grey))
                .setTextGravity(Gravity.CENTER)
                .setCircularEffect(CircularEffect.INNER) // Shows circular revealed effects for the content view of the popup menu.
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setSelectedMenuColorResource(R.color.basic_color_500)
                .setMenuColor(requireActivity().getColor(R.color.banner_bg)).build()
            powerMenu.selectedPosition = selectedPositionF

            powerMenu.showAsAnchorLeftBottom(
                binding.sort,
                binding.sort.getMeasuredWidth() / 2 - powerMenu.getContentViewWidth(),
                -binding.sort.getMeasuredHeight() / 2 - powerMenu.getContentViewHeight()
            )




            powerMenu.setOnMenuItemClickListener { position, item ->
                selectedPositionF = position
                powerMenu.selectedPosition = position
                binding.listProgressBar.visible()
                binding.listViewPager.adapter = null
                scope.launch {
                    delay(900)
                    binding.listTypeName.text = keys[position]
                    binding.listTypeSize.text = values[position].size.toString()
                    binding.listProgressBar.gone()
                    update(values.toList().getOrNull(position)!!)

                }

            }
        }
    }

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()



    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

