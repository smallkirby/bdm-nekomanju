package com.example.nekomanju.overview

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nekomanju.database.DatabaseNekoDao
import com.example.nekomanju.database.NekoDatabase.Companion.getDatabase
import com.example.nekomanju.database.asDomainModel
import com.example.nekomanju.domain.Data
import com.example.nekomanju.util.Utility.Companion.getLatestData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException

enum class WebApiStatus { LOADING, ERROR, DONE }

class OverviewViewModel(
    application: Application
): ViewModel() {

    private lateinit var localdb: DatabaseNekoDao
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    //val datalist = dataRepository.data

    private val _collectedData = MutableLiveData<List<Data>>()
    val  collectedData: LiveData<List<Data>>
        get() = _collectedData

    private val _latestData = MutableLiveData<Data>()
    val latestData: LiveData<Data>
        get() = _latestData

    //private val _status = MutableLiveData<WebApiStatus>()
    //val status: LiveData<WebApiStatus>
    //    get() = _status

    //private val _networkError = MutableLiveData<Boolean>(false)
    //val networkError: LiveData<Boolean>
    //    get() = _networkError

    //private val _networkErrorComplete = MutableLiveData<Boolean>(false)
    //val networkErrorComplete: LiveData<Boolean>
    //    get() = _networkErrorComplete

    init {
        Timber.d("Init is triggerd")
        localdb = getDatabase(application).databaseNekoDao
        //getRecentData()
    }

    private fun getRecentData() {
        Timber.d("getRecentData()")
        //_status.value = WebApiStatus.LOADING
        viewModelScope.launch{
            try{
                //dataRepository.getData()
                //_status.value = WebApiStatus.DONE
                //_networkError.value = false
                //_networkErrorComplete.value = true
            }catch(networkError: IOException){
                Timber.d(networkError.toString())
                if(false){//datalist.value.isNullOrEmpty()) {
                    //_networkError.value = true
                    //_networkErrorComplete.value = false
                }
            }
        }
    }

    fun collectLatLngSpecificData(latlng: LatLng){
        //viewModelScope.launch{
        uiScope.launch{
            getLatLngSpecifiedData(latlng.latitude, latlng.longitude)
        }
    }

    fun onNetworkErrorComplete(){
        //_networkErrorComplete.value = false
    }


    /** DAO wrappers **/
    suspend fun getLatLngSpecifiedData(latitude: Double, longitude: Double){
        withContext(Dispatchers.IO){
            val data = localdb.getLatlngSpecificData(latitude, longitude).asDomainModel()
            // put all collected data
            _collectedData.postValue(data)
            // choose and put latest data
            _latestData.postValue(getLatestData(data))
        }
    }
}