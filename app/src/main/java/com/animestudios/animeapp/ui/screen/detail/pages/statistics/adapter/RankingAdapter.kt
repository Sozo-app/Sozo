package com.animestudios.animeapp.ui.screen.detail.pages.statistics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.GetFullDataByIdQuery
import com.animestudios.animeapp.databinding.RakingItemBinding

class RankingAdapter(val list: ArrayList<GetFullDataByIdQuery.Ranking>) :
    RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    inner class RankingViewHolder(private val binding: RakingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetFullDataByIdQuery.Ranking, index: Int) {
            with(binding) {
                val mediaRank = item
                rankingText.text =
                    "#${mediaRank.rank} ${mediaRank.context}${if (mediaRank.season != null) " " + mediaRank.season else ""}${if (mediaRank.year != 0) " " + mediaRank.year else ""}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return RankingViewHolder(
            RakingItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: ArrayList<GetFullDataByIdQuery.Ranking>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        holder.bind(list.get(position), position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}