package com.example.nekomanju.overview

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nekomanju.R
import com.example.nekomanju.databinding.FragmentOverviewBinding
import com.example.nekomanju.domain.Data
import com.example.nekomanju.util.Utility.Companion.co2icon
import com.example.nekomanju.util.Utility.Companion.co2status
import com.example.nekomanju.util.Utility.Companion.formatPrecise
import com.example.nekomanju.util.Utility.Companion.latlng2str
import com.example.nekomanju.util.Utility.Companion.timeIso2Readable
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

class OverviewFragment: Fragment() {

    private val TAG = OverviewFragment::class.java.simpleName
    private lateinit var binding:FragmentOverviewBinding

    val application by lazy{
        requireNotNull(this.activity).application
    }
    //private val viewModel: OverviewViewModel by lazy{
    //    val viewModelFactory = OverviewViewModelFactory(application)
    //    ViewModelProvider(this, viewModelFactory).get(OverviewViewModel::class.java)
    //}
    private lateinit var viewModel: OverviewViewModel
    private lateinit var extra: LatLng

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d(TAG, "onCreateView()")
        super.onCreateView(inflater, container, savedInstanceState)

        // set title
        //val actionbar = supportActionbar
        //if(actionbar != null){
        //    actionbar.title = getString(R.string.main_title) + " : " + "ログ"
        //}

        // get model
        val viewModelFactory = OverviewViewModelFactory(application)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(OverviewViewModel::class.java)

        // get extra
        extra = (activity as OverviewActivity).getLatLng()

        // init and binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_overview, container, false)

        // bind
        //binding.overviewViewModel = viewModel
        binding.lifecycleOwner = this

        // click listener
        binding.showLogButton.setOnClickListener{
            this.findNavController().navigate(OverviewFragmentDirections.actionOverviewFragmentToChartFragment())
        }

        // observe
        viewModel.latestData.observe(viewLifecycleOwner, Observer<Data>{ latestData ->
            if(latestData != null){
                //Toast.makeText(context, collectedData[0].time, Toast.LENGTH_LONG).show()
                binding.locationDataText.text = latlng2str(LatLng(latestData.latitude, latestData.longitude))
                binding.co2DataText.text =  "${latestData.co2.formatPrecise(4)} ppm"
                binding.statusDataText.text = co2status(latestData.co2)
                binding.imageView.setImageResource(co2icon(latestData.co2))
                binding.temperatureDataText.text = "${latestData.temperature.formatPrecise(2)} 度"
                binding.humidityDataText.text = "${latestData.humidity.formatPrecise(2)} %"
                binding.lastupdatedText.text = "最終更新 ${timeIso2Readable(latestData.time)}"
            }
        })

        // set menu
        setHasOptionsMenu(true)

        // search requested data from localDB
        viewModel.collectLatLngSpecificData(extra)

        return binding.root
    }

    private fun onNetworkError(){
        //if(!viewModel.networkErrorComplete.value!!){
        //    Toast.makeText(activity, "Network Error Happens", Toast.LENGTH_LONG).show()
        //    viewModel.onNetworkErrorComplete()
        //}
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.go_map_menu -> {
            true
        }
        else -> {
            AlertDialog.Builder(context).setTitle("Hey").setMessage("Not Implemented")
                .setPositiveButton("OK"){dialog,which ->}
                .show()
            super.onOptionsItemSelected(item)
        }
    }
}