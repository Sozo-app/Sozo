package com.animestudios.animeapp.ui.screen.search.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.response.data
import com.animestudios.animeapp.setSafeOnClickListener
import com.google.android.material.chip.Chip

class SingleSelectAdapter(
    private var itemList: MutableList<String>
) :
    RecyclerView.Adapter<SingleSelectAdapter.ViewHolder>() {
    private lateinit var listener: ((String) -> Unit)
    fun singleItemClickListener(itemListener: ((String) -> Unit)) {
        listener = itemListener
    }


    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_single_click, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int = itemList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chipView: TextView = itemView as TextView

        fun bind(item: String, position: Int) {
            chipView.text = item
            if (selectedPosition == -1) {
                chipView.setBackgroundResource(R.drawable.explore)
            } else {
                if (selectedPosition == adapterPosition) {
                    chipView.setBackgroundResource(R.drawable.category_selected_bg)
                } else {
                    chipView.setBackgroundResource(R.drawable.explore)
                }
            }

//            chipView.setOnClickListener {
//                if (selectedPosition == position) {
//                    listener.invoke(itemList.get(position))
//                } else {
//                    setSelectedPosition(position)
//                    listener.invoke(itemList.get(position))
//                }
//            }
            chipView.setSafeOnClickListener {
                itemView.apply {
                    chipView.setBackgroundResource(R.drawable.category_selected_bg)
                    if (selectedPosition != adapterPosition) {
                        setSelectedPosition(position)
                        listener.invoke(itemList.get(position))
                    } else {
                        listener.invoke(itemList.get(position))
                    }
                }
            }
        }
    }

    // Method to update the selected position and trigger the UI update
    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(position)
    }
}
