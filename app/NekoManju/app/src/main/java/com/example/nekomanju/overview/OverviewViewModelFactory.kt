package com.example.nekomanju.overview

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class OverviewViewModelFactory(
   private val application: Application
): ViewModelProvider.Factory {

   override fun <T : ViewModel?> create(modelClass: Class<T>): T {
       if(modelClass.isAssignableFrom(OverviewViewModel::class.java)){
           return OverviewViewModel(application) as T
       }
       throw IllegalArgumentException("@OverviewViewModelFactory: Un-assignable ViewModel class")
   }

}