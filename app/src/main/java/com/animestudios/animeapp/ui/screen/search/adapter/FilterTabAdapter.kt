package com.animestudios.animeapp.ui.screen.search.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.animestudios.animeapp.ui.screen.search.dialog.tab.model.FilterTabModel
import com.animestudios.animeapp.ui.screen.search.dialog.tab.page.FilterSingleClickScreen
import com.animestudios.animeapp.ui.screen.search.dialog.tab.page.FilterSingleClickScreenForGenre
import com.animestudios.animeapp.ui.screen.search.dialog.tab.page.FilterSingleClickScreenForSort

class FilterTabAdapter(
    var arrayList: ArrayList<FilterTabModel>,
    fragmentManager: FragmentActivity
) :
    FragmentStateAdapter(fragmentManager) {
    override fun getItemCount(): Int {
        return arrayList.size
    }

    private lateinit var listener: ((ArrayList<String>) -> Unit)
    fun singleItemClickListener(itemListener: ((ArrayList<String>) -> Unit)) {
        listener = itemListener
    }

    private lateinit var listenerSort: ((String) -> Unit)
    fun singleItemSortListener(itemListener: ((String) -> Unit)) {
        listenerSort = itemListener
    }

    private lateinit var listenerYears: ((String) -> Unit)
    fun singleYearListener(itemListener: ((String) -> Unit)) {
        listenerYears = itemListener
    }


    override fun createFragment(position: Int): Fragment {
        when (position) {

            0 -> {
                val singleClickScreen = FilterSingleClickScreenForGenre(
                    arrayList.get(position).list.toMutableList() as ArrayList<String>
                )
                singleClickScreen.singleItemClickListener { listener.invoke(it) }

                return singleClickScreen
            }
            1 -> {
                val singleClickScreen = FilterSingleClickScreen(
                    arrayList.get(position).list as MutableList<String>,
                    arrayList.get(position).title,
                    arrayList[position].defaultItem
                )
                singleClickScreen.singleItemClickListener { listenerYears.invoke(it) }
                return singleClickScreen
            }
            else -> {
                val singleClickScreen = FilterSingleClickScreenForSort(
                    arrayList.get(position).list as MutableList<String>,
                    arrayList.get(position).title,
                    arrayList[position].defaultItem
                )
                singleClickScreen.singleItemClickListener { listenerSort.invoke(it) }
                return singleClickScreen
            }
        }
    }
}