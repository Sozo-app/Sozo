package com.animestudios.animeapp.ui.screen.home.banner

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.databinding.BannerItemBinding
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.loadImage
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.settings.UISettings
import com.animestudios.animeapp.viewmodel.imp.AniListViewModelImp
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class BannerScreen : Fragment
    () {
    private var _binding: BannerItemBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AniListViewModelImp by activityViewModels()
    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BannerItemBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setAnimation(context = binding.root.context, binding.root, uiSettings)
            loadBannerContainer()

        }
    }

    private fun loadBannerContainer() {
        binding.apply {
//            binding.itemImg.slideStart(700,0)
//            val data = arguments?.getSerializable("data") as Media
//            Glide.with(requireContext()).load(data.banner ?: data.cover)
//                .transition(DrawableTransitionOptions.withCrossFade()).override(0)
//                .into(itemCompactBanner)
//            val banner = itemCompactBanner
//            banner.loadImage(data.cover?:data.banner)
////            val context = itemCompactBanner.context
////            if (!(context as Activity).isDestroyed)
////                Glide.with(context as Context)
////                    .load(GlideUrl(data.banner ?: data.cover))
////                    .diskCacheStrategy(DiskCacheStrategy.ALL).override(490)
////                    .apply(RequestOptions.bitmapTransform(BlurTransformation(2, 3)))
////                    .into(banner)
////            binding.itemImg.loadImage(data.cover)
////            itemCompactScore.text =
////                ((if (data.userScore == 0) (data.meanScore
////                    ?: 0) else data.userScore) / 10.0).toString()
//
////            binding.itemCompactBanner.setTransitionGenerator(
////                RandomTransitionGenerator(
////                    (10000 + 15000 * (uiSettings.animationSpeed)).toLong(),
////                    AccelerateDecelerateInterpolator()
////                )
////            )
//            binding.title.text = data.name
//            var genresL = ""
//            data.genres.apply {
//                var count = 0
//                if (isNotEmpty()) {
//                    forEach {
//                        if (count <= 2) {
//
//
//                            count++
//                            genresL += "$it • "
//                        }
//                    }
//                    genresL = genresL.removeSuffix(" • ")
//                }
//            }
//            val genres =
//                "${data.anime?.totalEpisodes} Episodes\n${genresL}"
//            itemDescription.text = genres
//

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}