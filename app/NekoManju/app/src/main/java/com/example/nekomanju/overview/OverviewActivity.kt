package com.example.nekomanju.overview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.example.nekomanju.R
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

class OverviewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overview)
    }

    //fun getEx(): String?{
    //    return intent.getStringExtra("com.example.nekomanju.EXTRA")
    //}

    fun getLatLng(): LatLng {
        val bundle =  intent.extras
        if(bundle == null){
            Timber.e("Error getLatLng")
            return LatLng(0.0,0.0)
        }
        val latitude: Double = bundle.getDouble("com.example.nekomanju.LATITUDE")
        val longitude: Double = bundle.getDouble("com.example.nekomanju.LONGITUDE")
        return LatLng(latitude, longitude)
    }
}