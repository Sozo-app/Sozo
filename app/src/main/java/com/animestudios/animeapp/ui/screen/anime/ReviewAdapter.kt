package com.animestudios.animeapp.ui.screen.anime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.*
import com.animestudios.animeapp.databinding.ReviewItemBinding
import com.animestudios.animeapp.model.Review
import com.animestudios.animeapp.settings.UISettings

class ReviewAdapter(private val list: List<Review>, private val activity: Fragment) :
    RecyclerView.Adapter<ReviewAdapter.ReviewVh>() {


    inner class ReviewVh(val binding: ReviewItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Review) {
            val uiSettings =
                readData<UISettings>("ui_settings") ?: UISettings()
            binding.apply {
                if (uiSettings.layoutAnimations) {
                    setAnimation(activity.requireContext(), binding.root, uiSettings)
                    binding.title.animation = setSlideIn(uiSettings)
                    binding.animeTxt.animation = setSlideIn(uiSettings)
                    binding.profilePhoto.animation = setSlideUp(uiSettings)
                    binding.itemCompactImage.animation = setSlideUp(uiSettings)
                }
                title.text = data.user.name
                animeTxt.text = data.aniListMedia.title.userPreferred
                if (data.aniListMedia.bannerImage!=""){

                    bannerGradient.loadImage(data.aniListMedia.bannerImage)
                }else{
                    bannerGradient.loadImage("https://i.pinimg.com/736x/67/50/d8/6750d8a9e653bc738f52c814181938f1.jpg")

                }
                profilePhoto.loadImage(data.user.avatar.medium)
                itemCompactImage.loadImage(data.aniListMedia.coverImage.medium)
                description.text = data.summary
                ratingReview.rating = (data.score / 20).toFloat()

                binding.root.setOnClickListener {
                    itemClick(data)
                }
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

    fun itemClick(data: Review) {
        val bundle = Bundle()
        bundle.putSerializable("review", data)

        activity.findNavController().navigate(R.id.reviewScreen, bundle)
    }

    override fun onBindViewHolder(holder: ReviewVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {

        return list.size
    }
}