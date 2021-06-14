package com.example.nekomanju.domain


data class Data(
    val dataId: Long,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val co2: Double,
    val temperature: Double,
    val humidity: Double,
){
}

data class TmpDataLocation(
    val location: String,
    val timesData: Array<TmpDataTime>
){}

data class TmpDataTime(
    val time: String,
    val data: TmpDataData
)

data class TmpDataData(
    val co2: Double,
    val temperature: Double,
    val humidity: Double,
)