package com.animestudios.animeapp.ui.screen.anime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.ReviewItemBinding
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.settings.UISettings

class ReviewAdapter(private val list: List<Review>, private val activity: FragmentActivity) :
    RecyclerView.Adapter<ReviewAdapter.ReviewVh>() {
    inner class ReviewVh(val binding: ReviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Review) {
            val uiSettings =
                readData<UISettings>("ui_settings") ?: UISettings()
            binding.apply {
                if (uiSettings.layoutAnimations) {
                    setAnimation(activity, binding.root, uiSettings)
                    binding.title.animation = setSlideIn(uiSettings)
                    binding.animeTxt.animation = setSlideIn(uiSettings)
                    binding.profilePhoto.animation = setSlideUp(uiSettings)
                    binding.itemCompactImage.animation = setSlideUp(uiSettings)
                }
                title.text = data.user.name
                animeTxt.text = data.aniListMedia.title.userPreferred
                bannerGradient.loadImage(data.aniListMedia.bannerImage)
                profilePhoto.loadImage(data.user.avatar.medium)
                itemCompactImage.loadImage(data.aniListMedia.coverImage.medium)
                description.text = data.summary
                ratingReview.rating = (data.score / 20).toFloat()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewVh {
        return ReviewVh(
            ReviewItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ReviewVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {

        return list.size
    }
}