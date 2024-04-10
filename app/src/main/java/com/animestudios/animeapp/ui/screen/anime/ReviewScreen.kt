package com.animestudios.animeapp.ui.screen.anime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.animestudios.animeapp.*
import com.animestudios.animeapp.anilist.api.common.Anilist.username
import com.animestudios.animeapp.databinding.ReviewScreenBinding
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.tools.TimeUtil
import com.animestudios.animeapp.widget.MarkdownUtil
import com.animestudios.animeapp.widget.applyBottomPaddingInsets
import com.animestudios.animeapp.widget.getScoreColor
import com.animestudios.animeapp.widget.getThemeCardColor
import java.lang.Math.round

class ReviewScreen : Fragment() {

    private var _binding: ReviewScreenBinding? = null
    private val binding get() = _binding!!
    private var menuItemViewOnAniList: MenuItem? = null
    private var menuItemCopyLink: MenuItem? = null
    private lateinit var reviewData: Review


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ReviewScreenBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reviewData = arguments?.getSerializable("review") as Review
        setUpLayout()
        setUpInsets()
    }

    fun setUpToolbar(
        toolbar: Toolbar,
        title: String,
        icon: Int = R.drawable.ic_back,
        action: () -> Unit = { findNavController().popBackStack() }
    ) {
        toolbar.apply {
            setTitle(title)
            setNavigationIcon(icon)
            setNavigationOnClickListener { action() }
        }
    }

    fun setUpLayout() {
        with(binding) {
            readerToolbar.let {
                setUpToolbar(it, "")
                menuItemViewOnAniList = it.menu.findItem(R.id.itemViewOnAniList)
                menuItemCopyLink = it.menu.findItem(R.id.itemCopyLink)
            }

            menuItemViewOnAniList?.setOnMenuItemClickListener {
                openLinkInBrowser(reviewData.siteUrl)
                true
            }
//
            menuItemCopyLink?.setOnMenuItemClickListener {
                try {
                    val clipboard =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(BuildConfig.APPLICATION_ID, reviewData.siteUrl)
                    clipboard.setPrimaryClip(clip)
                } catch (e: Exception) {
                    snackString(e.message)
                }
                true
            }
            binding.readerBannerImage.loadImage(reviewData.aniListMedia.bannerImage)
            binding.readerTitle.text= getString(R.string.review_of_x_by_y, reviewData.aniListMedia.title.english, reviewData.user.name)
            binding.readerMediaType.text="Anime Review"
            binding.readerSummary.text=reviewData.summary
            binding.readerUserAvatar.loadImage(reviewData.user.avatar.large)
            binding.readerDate.text = TimeUtil.displayInDateFormat(reviewData.createdAt)
            MarkdownUtil.applyMarkdown(requireContext(), binding.readerText, reviewData.body)
            binding.readerScore.text = "${reviewData.score}/100"
            val nearestTen = (round(reviewData.score / 10.0) * 10).toInt()
            binding.readerScoreCard.setCardBackgroundColor(nearestTen.getScoreColor() ?: requireContext().getThemeCardColor())


//
//            readerTitle.clicks {
//                navigation.navigateToMedia(review.mediaId)
//            }
//
//            readerUserAvatar.clicks {
//                navigation.navigateToUser(review.userId)
//            }
//
//            readerUserName.clicks {
//                navigation.navigateToUser(review.userId)
//            }
//
//            readerLikeCard.clicks {
//                viewModel.like()
//            }
//
//            readerDislikeCard.clicks {
//                viewModel.dislike()
//            }
//        }
        }
    }
     fun setUpInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.readerCollapsingToolbar, null)
        binding.readerScrollingLayout.applyBottomPaddingInsets()
    }
    override fun onDestroy() {
        super.onDestroy()
        menuItemCopyLink = null
        menuItemViewOnAniList = null

    }

}