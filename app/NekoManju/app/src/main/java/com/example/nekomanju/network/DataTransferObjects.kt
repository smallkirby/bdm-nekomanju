package com.example.nekomanju.network

import com.example.nekomanju.database.DatabaseNeko
import com.example.nekomanju.domain.Data
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkDataContainer(val data: List<NetworkDataModel>)

@JsonClass(generateAdapter = true)
data class NetworkDataModel(
    val time: String,
    val altitude: Double,
    val longitude: Double,
    val co2: Double,
    val temperature: Double,
    val humidity: Double,
    )

// ネットワークモデルからドメインモデルへの変換
fun NetworkDataContainer.asDomainModel(): List<Data>{
    return data.map{
        Data(
            dataId = 0,         // XXX fix me
            time = it.time,
            latitude = it.altitude,
            longitude = it.longitude,
            co2 = it.co2,
            temperature = it.temperature,
            humidity = it.humidity
        )
    }
}

fun NetworkDataContainer.asDatabaseModel(): List<DatabaseNeko>{
    return data.map{
        DatabaseNeko(
            dataId = 0L, // fixme
            time = it.time,
            latitude = it.altitude,
            longitude = it.longitude,
            co2 = it.co2,
            temperature = it.temperature,
            humidity = it.humidity
        )
    }
}

