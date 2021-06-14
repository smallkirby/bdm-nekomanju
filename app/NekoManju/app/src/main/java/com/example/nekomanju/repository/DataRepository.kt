package com.example.nekomanju.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.nekomanju.database.NekoDatabase
import com.example.nekomanju.database.asDomainModel
import com.example.nekomanju.domain.Data
import com.example.nekomanju.network.NekoNetwork
import com.example.nekomanju.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class DataRepository(private val database: NekoDatabase) {

    //val data: LiveData<List<Data>> =  Transformations.map(database.databaseNekoDao.getData()){
    //    it.asDomainModel()
    //}

    suspend fun getData(){
        withContext(Dispatchers.IO){
            Timber.d("getData() @ DataRepository")
            val datalist = NekoNetwork.data.getData()
            Timber.d("Saving Data to DB")
            database.databaseNekoDao.insertAll(datalist.asDatabaseModel())
        }
    }
}