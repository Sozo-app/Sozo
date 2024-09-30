package com.animestudios.animeapp.ui.screen.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.SourceItemBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.model.Source
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.saveData
import com.animestudios.animeapp.visible

class SourcePageAdapter : RecyclerView.Adapter<SourcePageAdapter.SourcePageVh>() {

    private val list = ArrayList<Source>()
    private lateinit var isNotify: (String) -> Unit

    private var selectedSource: String? = null

    init {
        // Initial source o'qiladi
        selectedSource = readData("selectedSource") ?: "GOGO"
    }

    fun setNotifyListener(listener: (String) -> Unit) {
        isNotify = listener
    }

    inner class SourcePageVh(var itemBinding: SourceItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(data: Source) {
            itemBinding.apply {
                if (data.link == selectedSource) {
                    isSelectedSource.visible()
                } else {
                    isSelectedSource.gone()
                }
                sourceTitle.text = data.title
            }
            itemBinding.root.setOnClickListener {
                selectedSource = data.link
                isNotify.invoke(data.link)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourcePageVh {
        return SourcePageVh(
            SourceItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: ArrayList<Source>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SourcePageVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

}