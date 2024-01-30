package com.animestudios.animeapp.ui.screen.detail.pages.detail

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.GetFullDataByIdQuery
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.databinding.DetailPageBinding
import com.animestudios.animeapp.databinding.LayoutMediaStatusDistributionBinding
import com.animestudios.animeapp.gone
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tools.*
import com.animestudios.animeapp.type.MediaListStatus
import com.animestudios.animeapp.type.MediaType
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import com.animestudios.animeapp.viewmodel.imp.GenresViewModelImp
import com.animestudios.animeapp.visible
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailPage : Fragment() {

    private val model by activityViewModels<DetailsViewModelImpl>()
    private var _binding: DetailPageBinding? = null
    private val binding get() = _binding!!
    private val genreModel: GenresViewModelImp by activityViewModels()

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
            val media = it
            model.getFulDataById(it)


            model.getFullData.observe(viewLifecycleOwner) {
                when (it) {
                    is Resource.Error -> TODO()
                    Resource.Loading -> {
                        binding.apply {
                            detailContainer.gone()
                            detailProgress.visible()
                        }
                    }
                    is Resource.Success -> {
                        val mediaFull = it.data
                        binding.apply {
                            val parent = binding.parentView

                            detailProgress.gone()
                            detailContainer.visible()

//                            //Score Distribution
//
                            if (mediaFull.Media?.studios?.nodes?.isNotEmpty() == true) {

                                binding.studiosTitle.text =
                                    mediaFull.Media.studios.nodes.get(0)?.name ?: "XXX"

                            }
                            binding.statusTitle.text = mediaFull.Media?.status?.rawValue ?: "XXX"

                            binding.producerTitle.text =
                                mediaFull.Media?.staff?.nodes?.get(0)?.name?.first ?: "XXX"

                            startDateTitle.text = media.startDate?.toISOString()
                            endDateTitle.text = media.endDate?.toISOString()


                            averageScore.text = media.averageScore.getNumberFormatting() + "%"
                            meanScore.text = media.meanScore?.getNumberFormatting() + "%"
                            popularityScore.text = media.popularity!!.getNumberFormatting()
                            favoritesScore.text = media.favourites.getNumberFormatting()
                            mediaTitleRomanji.text = media.nameRomaji

                            mediaTitleEnglish.text = media.englishName
                            mediaTitleNative.text = media.nativeName

                            if (media.synonyms.isNotEmpty() && media.synonyms.size > 2) {
                                binding.synonymsTitle1.text = removeSpaces(media.synonyms.get(0))
                                binding.synonymsTitle2.text = removeSpaces(media.synonyms.get(1))
                                binding.synonymsTitle3.text = removeSpaces(media.synonyms.get(2))
                            } else {
                                binding.synonymsTitle1.gone()
                                binding.synonymsTitle2.gone()
                                binding.synonymsTitle3.gone()
                                binding.synonymsRow.gone()
                            }

                            ///Description
                            val desc = HtmlCompat.fromHtml(
                                (media.description ?: "null").replace("\\n", "<br>")
                                    .replace("\\\"", "\""),
                                HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            binding.mediaInfoDescription.text =
                                "\t\t\t" + if (desc.toString() != "null") desc else "No Description Available"

                            binding.mediaInfoDescription.setOnClickListener {
                                if (binding.mediaInfoDescription.maxLines == 5) {
                                    ObjectAnimator.ofInt(
                                        binding.mediaInfoDescription,
                                        "maxLines",
                                        100
                                    )
                                        .setDuration(950).start()
                                } else {
                                    ObjectAnimator.ofInt(
                                        binding.mediaInfoDescription,
                                        "maxLines",
                                        5
                                    )
                                        .setDuration(400).start()
                                }
                            }

                        }
                    }
                }
            }
        }
    }




    fun removeSpaces(inputString: String): String {
        return inputString.replace(" ", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}




