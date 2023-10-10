package com.animestudios.animeapp.others

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.animestudios.animeapp.GesturesListener
import com.animestudios.animeapp.databinding.ItemProgressBinding
import com.animestudios.animeapp.snackString

class ProgressAdapter(private val horizontal: Boolean = true, searched: Boolean) :
    RecyclerView.Adapter<ProgressAdapter.ProgressViewHolder>() {
    val ready = MutableLiveData(searched)
    var bar: LottieAnimationView? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val binding = ItemProgressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgressViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        val progressBar = holder.binding.root
        bar = progressBar
            val doubleClickDetector = GestureDetector(progressBar.context, object : GesturesListener() {
            override fun onDoubleClick(event: MotionEvent) {
                snackString("Can't Wait, huh? fine X(")
                ObjectAnimator.ofFloat(progressBar, "translationX", progressBar.translationX, progressBar.translationX + 100f)
                    .setDuration(300).start()
            }

            override fun onScrollYClick(y: Float) {}
            override fun onSingleClick(event: MotionEvent) {}
        })
        progressBar.setOnTouchListener { v, event ->
            doubleClickDetector.onTouchEvent(event)
            v.performClick()
            true
        }
        if (ready.value == false) {
            ready.postValue(true)
        }
    }

    override fun getItemCount(): Int = 1
    inner class ProgressViewHolder(val binding: ItemProgressBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.updateLayoutParams { if (horizontal) width = -1 else height = -1 }
        }
    }
}