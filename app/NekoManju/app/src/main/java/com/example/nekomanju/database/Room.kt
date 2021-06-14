package com.example.nekomanju.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Room.databaseBuilder

@Dao
interface DatabaseNekoDao{
    @Query("select * from DatabaseNeko")
    suspend fun getAllData(): List<DatabaseNeko>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // XXX fix me
    suspend fun insertAll(data: List<DatabaseNeko>)

    @Query("select * from DatabaseNeko where latitude = :latitude and longitude = :longitude")
    fun getLatlngSpecificData(latitude: Double, longitude: Double): List<DatabaseNeko>

    @Query("select * from Databaseneko where latitude > :lowerLatitude and latitude < :upperLatitude and longitude > :lowerLongitude and longitude < :upperLongitude")
    fun getLatLngRangedData(upperLatitude: Double, lowerLatitude: Double, upperLongitude: Double, lowerLongitude: Double): List<DatabaseNeko>

    @Query("delete from DatabaseNeko")
    suspend fun clear()
}

@Database(entities = [DatabaseNeko::class], version=1)
abstract class NekoDatabase: RoomDatabase(){
    abstract val databaseNekoDao: DatabaseNekoDao

    companion object{
        @Volatile
        private var INSTANCE: NekoDatabase? = null

        fun getDatabase(context: Context): NekoDatabase{
            synchronized(NekoDatabase::class.java){
                var instance = INSTANCE
                if(instance == null){
                    instance = databaseBuilder(
                        context.applicationContext,
                        NekoDatabase::class.java,
                        "data"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

