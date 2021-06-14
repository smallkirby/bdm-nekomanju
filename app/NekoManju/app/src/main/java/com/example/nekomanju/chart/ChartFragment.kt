package com.example.nekomanju.chart

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.nekomanju.R
import com.example.nekomanju.databinding.FragmentChartBinding
import com.example.nekomanju.domain.Data
import com.example.nekomanju.overview.OverviewViewModel
import com.example.nekomanju.overview.OverviewViewModelFactory
import com.example.nekomanju.util.Utility.Companion.compareTimeIsoString
import com.example.nekomanju.util.Utility.Companion.timeIso2Readable
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import timber.log.Timber


class ChartFragment : Fragment() {

    private val TAG = ChartFragment::class.java.simpleName
    private lateinit var binding: FragmentChartBinding

    val application by lazy{
        requireNotNull(this.activity).application
    }
    // reuse OverviewViewModel here
    //private val viewModel: OverviewViewModel by lazy{
    //    val viewModelFactory = OverviewViewModelFactory(application)
    //    ViewModelProvider(this, viewModelFactory).get(OverviewViewModel::class.java)
    //}
    private lateinit var viewModel: OverviewViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d(TAG, "onCreateView()")
        super.onCreateView(inflater, container, savedInstanceState)

        // get model
        val viewModelFactory = OverviewViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(OverviewViewModel::class.java)

        // init and binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false)

        // bind
        binding.lifecycleOwner = this

        // set chart
        val entryListCo2 = mutableListOf<Entry>()  // co2
        val entryListTemp = mutableListOf<Entry>()  // 温度
        val comparatorCo2 = Comparator{ o1: Data, o2: Data ->
            return@Comparator compareTimeIsoString(o1.time, o2.time)
        }
        val datas: List<Data> = viewModel.collectedData.value!! // TODO
            .sortedWith(comparatorCo2)
        // データ格納
        for (i in datas.indices){
            entryListCo2.add(Entry(i.toFloat(), datas[i].co2.toFloat()))
            entryListTemp.add(Entry(i.toFloat(), datas[i].temperature.toFloat()))
        }
        val lineDataSets = mutableListOf<ILineDataSet>()
        val lineDataSetCo2 = LineDataSet(entryListCo2, "CO2濃度[ppm]")
        //val lineDataSetTemp = LineDataSet(entryListTemp, "温度[℃]")
        // format
        lineDataSets.apply{
            add(lineDataSetCo2)
            //add(lineDataSetTemp)
        }
        lineDataSetCo2.apply{
            setDrawValues(false)
            color = Color.DKGRAY
            mode = LineDataSet.Mode.LINEAR
            setDrawCircles(true)
            setCircleColor(Color.BLACK)
            lineWidth = 2f
        }
        //lineDataSetTemp.apply{
        //    setDrawValues(false)
        //    color = Color.BLUE
        //    mode = LineDataSet.Mode.LINEAR
        //    setDrawCircles(true)
        //    setCircleColor(Color.BLUE)
        //    lineWidth = 2f
        //}
        //
        val lineData = LineData(lineDataSets)
        val mv: SimpleMarkerView = SimpleMarkerView(requireContext(), R.layout.simple_marker_view)
        mv.chartView = binding.lineChart1
        // format chart
        binding.lineChart1.apply{
            data = lineData
            xAxis.apply{
                isEnabled = true
                textColor = Color.BLACK
                textSize = 3f
                valueFormatter = object: ValueFormatter(){
                    override fun getFormattedValue(value: Float): String {
                        return timeIso2Readable(datas[value.toInt()].time, needDetail = false)
                    }
                }
            }
            //axisRight.apply{
            //    isEnabled = true
            //}
            axisLeft.axisMinimum = 200f
            axisLeft.axisMaximum = 3600f
            legend.apply{
                textSize = 15f
                form = Legend.LegendForm.CIRCLE
            }
            description.apply{
                //textSize = 20f
                //text = "CO2の遷移"
                //yOffset = 5f
                isEnabled = false
            }
            setBackgroundColor(Color.WHITE)
            setScaleEnabled(true)

            marker = mv
            setVisibleXRangeMaximum(20f)
        }
        // update
        binding.lineChart1.invalidate()

        return binding.root
    }
}