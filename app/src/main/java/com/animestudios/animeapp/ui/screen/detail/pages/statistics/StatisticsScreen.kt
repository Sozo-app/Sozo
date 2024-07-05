package com.animestudios.animeapp.ui.screen.detail.pages.statistics

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.animestudios.animeapp.GetFullDataByIdQuery
import com.animestudios.animeapp.R
import com.animestudios.animeapp.anilist.response.data
import com.animestudios.animeapp.app.App
import com.animestudios.animeapp.databinding.LayoutMediaStatusDistributionBinding
import com.animestudios.animeapp.databinding.ListStatsChartBarBinding
import com.animestudios.animeapp.databinding.StatisticsScreenBinding
import com.animestudios.animeapp.media.Media
import com.animestudios.animeapp.tools.*
import com.animestudios.animeapp.type.MediaType
import com.animestudios.animeapp.ui.screen.detail.pages.statistics.adapter.RankingAdapter
import com.animestudios.animeapp.viewmodel.imp.DetailsViewModelImpl
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsScreen : Fragment() {
    private var _binding: StatisticsScreenBinding? = null
    private val binding get() = _binding!!
    private var isInitialized =false
    private val model by activityViewModels<DetailsViewModelImpl>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        model.getMedia().observe(this) {
            if (it != null) {
                val media = it
                binding.apply {
                    val parent = binding.parentStatistics

                        model.getFulDataById(it)
                        model.getFullData.observe(this@StatisticsScreen) {
                            when (it) {
                                is Resource.Error -> {

                                }
                                Resource.Loading -> {
                                }
                                is Resource.Success -> {
                                    if (!isInitialized){
                                        val fullData = it.data

                                    //Ranking
                                    val rankingAdapter = RankingAdapter(
                                        fullData.Media?.rankings as ArrayList<GetFullDataByIdQuery.Ranking>
                                            ?: arrayListOf()
                                    )
                                    rankingRv.adapter = rankingAdapter

                                    loadPieChart(
                                        fullData,
                                        binding = binding,
                                        parent = parent,
                                        media = media
                                    )
//                                    loadChart(
//                                        fullData,
//                                        binding = binding,
//                                        parent = parent,
//                                        media = media
//                                    )
                                        isInitialized=true

                                }


                            }

                        }

                    }

                }
            }
        }

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = StatisticsScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun loadPieChart(
        mediaFull: GetFullDataByIdQuery.Data,
        binding: StatisticsScreenBinding,
        parent: ViewGroup,
        media: Media
    ) {
        val scoreDistributionBinding = LayoutMediaStatusDistributionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        scoreDistributionBinding.apply {
            val chart = ArrayList<Chart>()

            mediaFull.Media?.stats?.statusDistribution?.forEach {
                val hexColor = it!!.status?.getColor()
                val color = ColorStateList.valueOf(Color.parseColor(hexColor))
                val label = getStatusLabel(it.status, MediaType.ANIME)
                val amount = it.amount!!.getNumberFormatting()

                chart.add(Chart(hexColor, label, it.amount.toDouble()))

                when (it.status) {
                    com.animestudios.animeapp.type.MediaListStatus.CURRENT -> {
                        mediaStatsCurrentIcon.imageTintList = color
                        mediaStatsCurrentLabel.text = label
                        mediaStatsCurrentText.text = amount
                    }
                    com.animestudios.animeapp.type.MediaListStatus.PLANNING -> {
                        mediaStatsPlanningIcon.imageTintList = color
                        mediaStatsPlanningLabel.text = label
                        mediaStatsPlanningText.text = amount
                    }
                    com.animestudios.animeapp.type.MediaListStatus.COMPLETED -> {
                        mediaStatsCompletedIcon.imageTintList = color
                        mediaStatsCompletedLabel.text = label
                        mediaStatsCompletedText.text = amount
                    }
                    com.animestudios.animeapp.type.MediaListStatus.DROPPED -> {
                        mediaStatsDroppedIcon.imageTintList = color
                        mediaStatsDroppedLabel.text = label
                        mediaStatsDroppedText.text = amount
                    }
                    com.animestudios.animeapp.type.MediaListStatus.PAUSED -> {
                        mediaStatsPausedIcon.imageTintList = color
                        mediaStatsPausedLabel.text = label
                        mediaStatsPausedText.text = amount
                    }
                    else -> {
                        // do nothing
                    }
                }
            }

            val pieEntries =
                chart.mapIndexed { _, chart -> PieEntry(chart.value.toFloat(), chart.label) }
            val pieDataSet = PieDataSet(pieEntries, "")
            pieDataSet.colors = chart.map {
                if (it.color.isNullOrBlank()) binding.root.context!!.getAttrValue(com.google.android.material.R.attr.colorOnPrimary) else Color.parseColor(
                    it.color
                )
            }

            val pieData = PieData(pieDataSet)
            pieData.setDrawValues(false)

            mediaStatsChart.statsPieChart.apply {
                setHoleColor(ContextCompat.getColor(context, android.R.color.transparent))
                setDrawEntryLabels(false)
                setTouchEnabled(false)
                description.isEnabled = false
                legend.isEnabled = false
                data = pieData
                invalidate()
            }
            parent.addView(scoreDistributionBinding.root)

        }


    }


    private fun loadChart(
        mediaFull: GetFullDataByIdQuery.Data,
        binding: StatisticsScreenBinding,
        parent: ViewGroup,
        media: Media
    ) {
        val scoreDistributionBinding = ListStatsChartBarBinding.inflate(
            LayoutInflater.from(App.instance),
            parent,
            false
        )

        val chart = ArrayList<Chart>()
        mediaFull.Media?.stats?.scoreDistribution?.forEach {
            val hexColor = when (it?.score) {
                10 -> "#d2492d"
                20 -> "#d2642c"
                30 -> "#d2802e"
                40 -> "#d29d2f"
                50 -> "#d2b72e"
                60 -> "#d3d22e"
                70 -> "#b8d22c"
                80 -> "#9cd42e"
                90 -> "#81d12d"
                100 -> "#63d42e"
                else -> null
            }
            val label = it!!.score!!.getNumberFormatting()
            val amount = it!!.amount!!.toDouble()

            chart.add(Chart(hexColor, label, amount))
        }

        val useStringLabel = chart.any { it.label.toDoubleOrNull() == null } ?: false

        val newValueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val barEntries = chart.mapIndexed { index, chart ->
            BarEntry(
                if (useStringLabel) index * 10F + 10F else (chart.label.toFloatOrNull()
                    ?: index * 10F + 10F), chart.value.toFloat()
            )
        }
        val barDataSet = BarDataSet(barEntries, "")
        barDataSet.colors = chart.map {
            if (it.color.isNullOrBlank()) requireContext().getAttrValue(com.google.android.material.R.attr.colorPrimary) else Color.parseColor(
                it.color
            )
        }

        val barData = BarData(barDataSet)
        barData.setValueTextColor(requireContext().getAttrValue(com.google.android.material.R.attr.colorOnPrimary))
        barData.barWidth = 3F
        barData.setValueFormatter(newValueFormatter)

        scoreDistributionBinding.statsBarChart.axisLeft.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        scoreDistributionBinding.statsBarChart.axisRight.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
            setDrawLabels(false)
        }

        scoreDistributionBinding.statsBarChart.xAxis.apply {
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
            setLabelCount(barDataSet.entryCount, true)
            textColor = requireContext().getAttrValue(R.attr.themeContentColor)

            if (chart.isNotEmpty() && useStringLabel) {
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val labelIndex = round(value / 10.0) - 1
                        if (labelIndex >= 0 && labelIndex < chart.size) {
                            return chart[labelIndex.toInt()].label
                        }
                        return ""
                    }
                }
            }
        }

        scoreDistributionBinding.statsBarChart.apply {
            setTouchEnabled(false)
            description.isEnabled = false
            legend.isEnabled = false
            data = barData
            invalidate()
        }
        parent.addView(scoreDistributionBinding.root)
    }

    private fun getStatusLabel(
        status: com.animestudios.animeapp.type.MediaListStatus?,
        mediaType: MediaType?
    ): String {
        return status?.getString(if (mediaType == MediaType.MANGA) MediaType.MANGA else MediaType.ANIME)
            ?: ""
    }


}