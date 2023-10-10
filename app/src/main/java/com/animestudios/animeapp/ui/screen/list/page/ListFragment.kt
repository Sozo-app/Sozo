package com.animestudios.animeapp.ui.screen.list.page

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.api.common.Anilist
import com.animestudios.animeapp.databinding.FragmentListBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.selectedPosition
import com.animestudios.animeapp.setAnimation
import com.animestudios.animeapp.ui.screen.anime.AnimeTitleWithScoreAdapter
import com.animestudios.animeapp.viewmodel.imp.ListViewModelImp
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.skydoves.powermenu.kotlin.powerMenu


class ListFragment(private val model: ListViewModelImp) : Fragment() {
    private var pos: Int? = null
    private var calendar = false
    private var list: MutableList<Media>? = null
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pos = it.getInt("list")
            calendar = it.getBoolean("calendar")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ResourceAsColor")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val screenWidth = resources.displayMetrics.run { widthPixels / density }


        fun update() {
            if (list != null) {
                val adapter = AnimeTitleWithScoreAdapter(this)
                adapter.submitLit(list!!)
                binding.listRecyclerView.layoutManager =
                    GridLayoutManager(requireContext(), (screenWidth / 124f).toInt())
                binding.listRecyclerView.adapter = adapter
            }
        }


        model.lists.observe(viewLifecycleOwner) {
            if (it != null) {
                list = it.values.toList().getOrNull(pos!!)
                println(list?.get(0))
                update()
            }
        }

    }

    companion object {
        fun newInstance(
            pos: Int, calendar: Boolean = false, model: ListViewModelImp
        ): ListFragment = ListFragment(model).apply {
            arguments = Bundle().apply {
                putInt("list", pos)

                putBoolean("calendar", calendar)
            }
        }
    }
}