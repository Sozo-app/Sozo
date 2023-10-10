package com.animestudios.animeapp.ui.screen.home.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.animestudios.animeapp.R
import com.animestudios.animeapp.databinding.CategoryItemBinding
import com.animestudios.animeapp.readData
import com.animestudios.animeapp.setSafeOnClickListener
import com.animestudios.animeapp.settings.UISettings

class CategoryAdapter(
    private val activity: FragmentActivity,
    private val matchParent: Boolean = false
) :

    RecyclerView.Adapter<CategoryAdapter.CategoryVh>() {
    private val list = ArrayList<String>()
    private var checkedPosition = 0
    private lateinit var itemClickListener: ((String) -> Unit)

    fun setItemClickListener(listener: (String) -> Unit) {
        itemClickListener = listener
    }

    private val uiSettings =
        readData<UISettings>("ui_settings") ?: UISettings()

    inner class CategoryVh(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: String) {
            binding.apply {
                binding.categoryTitle.text = data
                if (checkedPosition == -1) {
                        container.setBackgroundResource(R.drawable.explore)
                    categoryTitle.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    if (checkedPosition == adapterPosition) {
                        categoryTitle.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        container.setBackgroundResource(R.drawable.category_selected_bg)
                    } else {
                        categoryTitle.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        container.setBackgroundResource(R.drawable.explore)
                    }
                }
                itemView.setSafeOnClickListener {
                    itemView.apply {
                        container.setBackgroundResource(R.drawable.category_selected_bg)
                        categoryTitle.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        if (checkedPosition != adapterPosition) {
                            notifyItemChanged(checkedPosition)
                            checkedPosition = adapterPosition
                            itemClickListener.invoke(data)
                        } else {
                            itemClickListener.invoke(data)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryVh {
        return CategoryVh(
            CategoryItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun submitList(newList: List<String>) {
        checkedPosition = 0
        list.clear()
        list.addAll(newList)
    }

    override fun onBindViewHolder(holder: CategoryVh, position: Int) {
        holder.onBind(list.get(position))
    }

    override fun getItemCount(): Int {
        return list.size
    }

}