package com.example.nekomanju.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

interface NetworkService{
    @GET("data")
    suspend fun getData(): NetworkDataContainer
}

object NekoNetwork{

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://<server IP>/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val data = retrofit.create(NetworkService::class.java)
}

