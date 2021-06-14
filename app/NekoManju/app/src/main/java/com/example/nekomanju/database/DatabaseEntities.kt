package com.example.nekomanju.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nekomanju.domain.Data

@Entity
data class DatabaseNeko constructor(
    @PrimaryKey(autoGenerate = true)
    val dataId: Long,
    val time: String,
    val latitude: Double,
    val longitude: Double,
    val co2: Double,
    val temperature: Double,
    val humidity: Double
)

fun List<DatabaseNeko>.asDomainModel(): List<Data>{
    return map{
        Data(
            time = it.time,
            dataId = it.dataId,
            latitude = it.latitude,
            longitude = it.longitude,
            co2 = it.co2,
            temperature = it.temperature,
            humidity = it.humidity
        )
    }
}

fun List<Data>.asDatabaseModel(): List<DatabaseNeko>{
    return map{
        DatabaseNeko(
            dataId = it.dataId,
            time = it.time,
            longitude = it.longitude,
            latitude = it.latitude,
            co2 = it.co2,
            temperature = it.temperature,
            humidity = it.humidity
        )
    }
}

