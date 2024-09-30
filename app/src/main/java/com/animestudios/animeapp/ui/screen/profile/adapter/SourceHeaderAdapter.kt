package com.animestudios.animeapp.ui.screen.profile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.databinding.SourcePageHeaderItemBinding
import com.animestudios.animeapp.model.SourceDt
import com.animestudios.animeapp.saveData

class SourceHeaderAdapter() : RecyclerView.Adapter<SourceHeaderAdapter.SourceHeaderVh>() {
    var list = ArrayList<SourceDt>()
    lateinit var itemClickListenerFr: (String) -> Unit
    fun setItemClickListener(listener: (String) -> Unit) {
        itemClickListenerFr = listener
    }

    inner class SourceHeaderVh(var itemBinding: SourcePageHeaderItemBinding) : RecyclerView.
    ViewHolder(itemBinding.root) {

        fun onBind(data: SourceDt) {
            itemBinding.apply {
                val sourcePageAdapter = SourcePageAdapter()
                textView6.text = data.sourceType.toString()
                sourcePageAdapter.submitList(data.list)
                sourceRv.adapter = sourcePageAdapter
                sourcePageAdapter.setNotifyListener {
                    itemClickListenerFr.invoke(it)
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SourceHeaderVh {
        return SourceHeaderVh(
            SourcePageHeaderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    fun submitList(newList: ArrayList<SourceDt>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SourceHeaderVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }
}