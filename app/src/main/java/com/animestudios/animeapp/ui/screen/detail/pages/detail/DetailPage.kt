package com.animestudios.animeapp.ui.screen.detail.pages.detail

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.databinding.DetailPageBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.tools.getNumberFormatting
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPage : Fragment() {

    private val model by activityViewModels<DetailsViewModelImpl>()
    private var _binding: DetailPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DetailPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeData()
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        model.getMedia().observe(viewLifecycleOwner) {
            binding.apply {
                averageScore.text = it.averageScore.getNumberFormatting() + "%"
                meanScore.text = it.meanScore?.getNumberFormatting() + "%"
                popularityScore.text = it.popularity!!.getNumberFormatting()
                favoritesScore.text = it.favourites.getNumberFormatting()
                mediaTitleRomanji.text = it.nameRomaji

                mediaTitleEnglish.text=it.englishName
                mediaTitleNative.text=it.nativeName

                if (it.synonyms.isNotEmpty()&&it.synonyms.size>2){
                    synonymsTitle.text="${it.synonyms.get(0)}\n${it.synonyms.get(1)}\n${it.synonyms.get(2)}"
                }else{
                    binding.synonymsTitle.gone()
                    binding.synonymsRow.gone()
                }

            ///Description
                val desc = HtmlCompat.fromHtml(
                    (it.description ?: "null").replace("\\n", "<br>").replace("\\\"", "\""),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
                binding.mediaInfoDescription.text =
                    "\t\t\t" + if (desc.toString() != "null") desc else "No Description Available"

                binding.mediaInfoDescription.setOnClickListener {
                    if (binding.mediaInfoDescription.maxLines == 5) {
                        ObjectAnimator.ofInt(binding.mediaInfoDescription, "maxLines", 100)
                            .setDuration(950).start()
                    } else {
                        ObjectAnimator.ofInt(binding.mediaInfoDescription, "maxLines", 5)
                            .setDuration(400).start()
                    }
                }

            }
        }
    }

}