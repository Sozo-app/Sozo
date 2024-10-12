package com.animestudios.animeapp.ui.screen.list

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.ListScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.ui.activity.DetailActivity
import com.animestudios.animeapp.ui.screen.anime.AnimeTitleWithScoreAdapter
import com.animestudios.animeapp.viewmodel.imp.ListViewModelImp
import com.skydoves.powermenu.CircularEffect
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Wassup Guys )) Soon I will start this project Damn my time :()

class ListScreen : Fragment() {
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
            binding.listTabLayout.animation = setSlideUp(uiSettings)
            binding.horizontalSpace.animation = setSlideUp(uiSettings)

            val powerList = ArrayList<PowerMenuItem>()
            keys.onEach { its ->
                powerList.add(PowerMenuItem(its))
            }
            initClicks(powerList, keys, values)
            update(values.toList().getOrNull(selectedPositionF)!!)
        }

    }


    @SuppressLint("ResourceAsColor", "FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            Refresh.activity[1]!!.postValue(true)
            val live = Refresh.activity.getOrPut(
                1
            ) { MutableLiveData(false) }
            live.observe(this) {
                if (it) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        model.loadLists(
                            true, Anilist.userid!!
                        )
                    }
                }
            }
    }


    private fun update(list: ArrayList<Media>) {
        screenWidth = resources.displayMetrics.run { widthPixels / density }
        val adapter = AnimeTitleWithScoreAdapter(this.requireActivity(), true)
        adapter.submitLit(list)
        adapter.setItemClickListener {
            val intent = Intent(requireActivity(), DetailActivity::class.java)
            intent.putExtra("media", it)
            startActivity(intent)
        }
        binding.listViewPager.layoutManager =
            GridLayoutManager(requireContext(), (screenWidth / 124f).toInt())
        binding.listViewPager.adapter = adapter
    }


    private fun initClicks(
        powerList: ArrayList<PowerMenuItem>,
        keys: List<String>,
        values: List<ArrayList<Media>>
    ) {
        binding.imageView3.setOnClickListener {
            val typedValue = TypedValue()
            val theme: Resources.Theme = requireContext().theme
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                typedValue,
                true
            )
            @ColorInt val selectedcolor: Int = typedValue.data


            val typedValueBg = TypedValue()
            val themeBg: Resources.Theme = requireContext().theme
            themeBg.resolveAttribute(
                com.google.android.material.R.attr.colorSurface,
                typedValueBg,
                true
            )
            @ColorInt val bgColor: Int = typedValueBg.data
            val powerMenu = PowerMenu.Builder(requireContext()).addItemList(
                mutableListOf(
                    PowerMenuItem(requireContext().getString(R.string.sort_by_score)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_title)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_release_date)),
                    PowerMenuItem(requireContext().getString(R.string.sort_by_last_updated))
                )
            ).setAnimation(MenuAnimation.SHOW_UP_CENTER).setIsClipping(true)
                .setAutoDismiss(true).setMenuRadius(16f).setMenuShadow(16f)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.light_grey))
                .setTextGravity(Gravity.CENTER)
                .setCircularEffect(CircularEffect.INNER) // Shows circular revealed effects for the content view of the popup menu.
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)

                .setSelectedMenuColor(selectedcolor)
                .setMenuColor(bgColor).build()
            powerMenu.selectedPosition = selectedPosition
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
            val typedValue = TypedValue()
            val theme: Resources.Theme = requireContext().theme
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                typedValue,
                true
            )
            @ColorInt val selectedcolor: Int = typedValue.data


            val typedValueBg = TypedValue()
            val themeBg: Resources.Theme = requireContext().theme
            themeBg.resolveAttribute(
                com.google.android.material.R.attr.colorSurface,
                typedValueBg,
                true
            )
            @ColorInt val bgColor: Int = typedValueBg.data
            val powerMenu = PowerMenu.Builder(requireContext()).addItemList(
                powerList
            ).setAnimation(MenuAnimation.SHOWUP_BOTTOM_RIGHT).setIsClipping(true)
                .setAutoDismiss(true).setMenuRadius(16f).setMenuShadow(16f)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.light_grey))
                .setTextGravity(Gravity.CENTER)
                .setCircularEffect(CircularEffect.INNER) // Shows circular revealed effects for the content view of the popup menu.
                .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
                .setSelectedTextColor(Color.WHITE)
                .setSelectedMenuColor(selectedcolor)
                .setMenuColor(bgColor).build()
            powerMenu.selectedPosition = selectedPositionF

            powerMenu.showAsAnchorLeftBottom(
                binding.sort,
                binding.sort.measuredWidth / 2 - powerMenu.contentViewWidth,
                -binding.sort.measuredHeight / 2 - powerMenu.contentViewHeight
            )




            powerMenu.setOnMenuItemClickListener { position, _ ->
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
        binding.listViewPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                    binding.sort.hide()
                else
                    binding.sort.show()

            }
        })
    }

    var state: Parcelable? = null

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    override fun onPause() {
        super.onPause()
        state = binding.listViewPager.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        binding.listViewPager.layoutManager?.onRestoreInstanceState(state)
    }

}

