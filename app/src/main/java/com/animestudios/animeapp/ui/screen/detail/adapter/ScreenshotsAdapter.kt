/*
 * Copyright (C) 2025 Azamov . - All Rights Reserved
 *
 * Unauthorized copying or redistribution of this file in source and binary forms via any medium
 * is strictly prohibited.
 *
 */

package com.animestudios.animeapp.ui.screen.detail.adapter

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.ItemScreenshotBinding
import com.animestudios.animeapp.model.shikimori.Screenshot
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class ScreenshotsAdapter(
    private val context: Context,
) : ListAdapter<Screenshot, ScreenshotsViewHolder>(ScreenshotsDiffCallback) {
    var onScreenshotClick: ((ImageView, Screenshot, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScreenshotsViewHolder {
        val binding = ItemScreenshotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScreenshotsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScreenshotsViewHolder, position: Int) {
        val screenshot = getItem(position)
        with(holder.binding) {
            Glide.with(context)
                .load("https://shikimori.one/${screenshot.original}")
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(sivScreenshot)
            root.setOnClickListener {
                onScreenshotClick?.invoke(
                    sivScreenshot,
                    screenshot,
                    position
                )
            }
        }
    }
}

class ScreenshotsViewHolder(
    val binding: ItemScreenshotBinding,
) : RecyclerView.ViewHolder(binding.root)

object ScreenshotsDiffCallback : DiffUtil.ItemCallback<Screenshot>() {
    override fun areItemsTheSame(oldItem: Screenshot, newItem: Screenshot): Boolean {
        return oldItem.original == newItem.original
    }

    override fun areContentsTheSame(oldItem: Screenshot, newItem: Screenshot): Boolean {
        return oldItem == newItem
    }
}

class HorizontalItemDecoration(
    private val spaceSize: Int,
    private val optionalSpaceSize: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                left = spaceSize + optionalSpaceSize
                right = spaceSize
            } else if (itemCount > 0 && itemPosition == itemCount - 1) {
                left = spaceSize
                right = spaceSize + optionalSpaceSize
            } else {
                left = spaceSize
                right = spaceSize
            }
            top = spaceSize
            bottom = spaceSize
        }
    }
}