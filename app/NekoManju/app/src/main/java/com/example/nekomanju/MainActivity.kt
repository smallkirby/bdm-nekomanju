package com.example.nekomanju

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.nekomanju.chart.cancelNotifications
import com.example.nekomanju.chart.sendNotification
import com.example.nekomanju.database.DatabaseNeko
import com.example.nekomanju.database.DatabaseNekoDao
import com.example.nekomanju.database.NekoDatabase.Companion.getDatabase
import com.example.nekomanju.database.asDatabaseModel
import com.example.nekomanju.domain.Data
import com.example.nekomanju.overview.OverviewActivity
import com.example.nekomanju.receiver.Co2Receiver
import com.example.nekomanju.util.Utility.Companion.asDouble
import com.example.nekomanju.util.Utility.Companion.compareLatLng
import com.example.nekomanju.util.Utility.Companion.data2description
import com.example.nekomanju.util.Utility.Companion.findLatLngLatestMatch
import com.example.nekomanju.util.Utility.Companion.findLatlngFirstMatch
import com.example.nekomanju.util.Utility.Companion.getNotificationMessage
import com.example.nekomanju.util.Utility.Companion.latlng2str
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.field.MillisDurationField
import timber.log.Timber
import java.time.temporal.TemporalQueries.localTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var database: DatabaseReference
    private lateinit var localdb: DatabaseNekoDao

    // location service client
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: Location
    private lateinit var currentLatLng: LatLng
    private var lastNearestLatLng: LatLng? = null       // 取得したデータの中で最も近くの位置情報
    private var cameraZoomed = false

    // timer
    private val REQUEST_CODE = 0
    private val second: Long = 1_000L
    private lateinit var notifyIntent: Intent
    private lateinit var alarmManager: AlarmManager
    private val notifyPendingIntent: PendingIntent by lazy {
        PendingIntent.getBroadcast(
            application,
            REQUEST_CODE,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    private val _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long>
        get() = _elapsedTime
    private lateinit var timer: CountDownTimer
    private val TRIGGER_TIME = "TRIGGER_AT"
    private lateinit var prefs: SharedPreferences
    private lateinit var notificationMessageBody: String

    // fetched data from Realtime Database
    var fetchedData = ArrayList<Data>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        // init
        notifyIntent = Intent(application, Co2Receiver::class.java)
        alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        prefs = application.getSharedPreferences("com.example.android.nekomanju", Context.MODE_PRIVATE)
        _elapsedTime.value = 0

        // change title
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.title = getString(R.string.main_title) + " : " + "地図検索"
        }

        // create notification channel
        createChannel(getString(R.string.notification_channel_id), getString(R.string.notification_channel_name))

        // location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // set map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // init database
        localdb = getDatabase(this).databaseNekoDao
        clearAllData()


        // init get user's location information
        getCurrentLocation()

        // init firebase
        database = Firebase.database.reference

    }

    private fun startTimer() {
        Timber.d("startTimer() is called")
        val selectedInterval = second * 2
        Timber.d("StartTime: ${SystemClock.elapsedRealtime()}")
        val triggerTime = SystemClock.elapsedRealtime() + selectedInterval
        Timber.d("EndExpectedTime: ${triggerTime}")
        val notificationManager = ContextCompat.getSystemService(application, NotificationManager::class.java) as NotificationManager
        notificationManager.cancelNotifications()
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime,
            notifyPendingIntent
        )
        saveTime(triggerTime)
        createTimer()
    }

    private fun createTimer() {
        Timber.d("createTimer() is called")
        val triggerTime = loadTime()
        timer = object : CountDownTimer(triggerTime, second) {
            override fun onTick(millisUntilFinished: Long) {
                Timber.d("onTick() is called")
                Timber.d("triggerTime: ${triggerTime}")
                Timber.d("elapsedRealtime(): ${SystemClock.elapsedRealtime()}")
                _elapsedTime.value = triggerTime - SystemClock.elapsedRealtime()
                Timber.d("_elapsedTime: ${_elapsedTime.value}")
                if (_elapsedTime.value!! <= 0) {
                    Timber.d("Time is end")
                    val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
                    notificationManager.sendNotification(notificationMessageBody, applicationContext)
                    resetTimer()
                }
            }

            override fun onFinish() {
                Timber.d("onFinish() is called")
                resetTimer()
            }
        }
        timer.start()
    }

    private fun loadTime(): Long {
        val tmp = prefs.getLong(TRIGGER_TIME, 0)
        Timber.d("loadTime(): ${tmp}")
        return tmp
    }

    private fun resetTimer() {
        timer.cancel()
        _elapsedTime.value = 0
    }

    private fun saveTime(triggerTime: Long) {
        Timber.d("saveTime() is called")
        prefs.edit().putLong(TRIGGER_TIME, triggerTime).apply()
    }

    private fun startFetchData(){
        val dataReference = database.child("data")
        val infoListtener = object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var nearestLatLng:  LatLng? = null
                for(location in snapshot.children){
                    val locationParsed = parseLocation(location.key!!)
                    // get nearest location
                    if(nearestLatLng == null){
                        nearestLatLng = LatLng(locationParsed.first, locationParsed.second)
                    }else{
                        if(compareLatLng(nearestLatLng, LatLng(locationParsed.first, locationParsed.second), currentLatLng) == 1){
                            nearestLatLng = LatLng(locationParsed.first, locationParsed.second)
                        }
                    }
                    // [end get nearest location]
                    for(specificData in location.children){
                        val oneTimeData = specificData.value as HashMap<String, Any>
                        val humidity  = oneTimeData["humidity"] as Double
                        val co2 = oneTimeData["co2"] as Double
                        val oneData = Data(
                            dataId = 0L, // fixme
                            time = specificData.key as String,
                            latitude = locationParsed.first,
                            longitude = locationParsed.second,
                            temperature = asDouble(oneTimeData["temperature"]),
                            humidity =  asDouble(oneTimeData["humidity"]),
                            co2 = asDouble(oneTimeData["co2"]),
                        )
                        // add data
                        fetchedData.add(oneData)
                    }
                    // set start point per one location children
                    addMarkerOnMap(findLatLngLatestMatch(fetchedData, LatLng(locationParsed.first, locationParsed.second)))
                }
                Timber.d(TAG, "Parsed Fetched Data")
                // データの内現在地に最も近い位置にズーム
                if(!cameraZoomed && nearestLatLng!=null) {
                    val target = findLatLngLatestMatch(fetchedData, nearestLatLng)
                    val latitude = target.latitude
                    val longitude = target.longitude
                    val startLatLng = LatLng(latitude, longitude)
                    val zoomLevel = 15f
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, zoomLevel))
                    cameraZoomed = true     // 起動時の一度のみカメラをズームさせる
                }

                // set last nearest location
                if(nearestLatLng != null) {
                    lastNearestLatLng = when {
                        lastNearestLatLng == null -> {
                            nearestLatLng
                        }
                        compareLatLng(nearestLatLng, lastNearestLatLng!!, currentLatLng) == -1 -> {
                            nearestLatLng
                        }
                        else -> {
                            lastNearestLatLng
                        }
                    }
                    Timber.d("Nearest location: ${lastNearestLatLng!!.latitude},${lastNearestLatLng!!.longitude}")
                }

                // alert if nearestLatLng's co2 exceeds 1000ppm
                if(lastNearestLatLng != null) {
                    val nearestData = findLatLngLatestMatch(fetchedData, lastNearestLatLng!!)
                    if(nearestData.co2 >= 1000){
                        notificationMessageBody = getNotificationMessage(nearestData)
                        startTimer()
                    }
                }

                // set local DB
                insertAllData(fetchedData.asDatabaseModel())
            } // [END onDataChange]

            override fun onCancelled(error: DatabaseError) {
                Timber.e(TAG, "Error Fetching data from DB")
            }
        }
        dataReference.addValueEventListener(infoListtener)
    }

    // create notification channel
    private fun createChannel(channelId: String, channelName: String){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(
                channelId,          // unique ID
                channelName,        // name users can see on the settings
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "猫のチャンネルねこちゃんねる"

            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        fusedLocationClient.lastLocation.addOnSuccessListener {  location: Location? ->
            if(location == null){
                Timber.e("Error: Couldn't get current location.")
                Toast.makeText(applicationContext, "Couldn't get current location.", Toast.LENGTH_SHORT).show()
            }else {
                Timber.d("get current location (== last known location): ${location.latitude},${location.longitude}")
                currentLocation = location
                currentLatLng = LatLng(location.latitude, location.longitude)
                startFetchData()
            }
        }
    }

    fun insertAllData(alldata: List<DatabaseNeko>){
        GlobalScope.launch {
            Timber.d(TAG, "Start insertAllData()")
            localdb.insertAll(alldata)
            Timber.d(TAG, "End insertAllData()")
        }
    }

    fun clearAllData(){
        GlobalScope.launch{
            localdb.clear()
        }
    }

    fun parseLocation(location: String): Pair<Double, Double>{
        val splited = location.replace("_",".").split("-")
        if(splited.size != 2){
            Timber.e(TAG, "Location Format is incorrect")
            return Pair<Double, Double>(0.0,0.0)
        }
        return Pair<Double, Double>(splited[0].toDouble(), splited[1].toDouble())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.overflow_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId){
        R.id.go_map_menu -> {
            //val intent = Intent(this, MapsActivity::class.java)
            //this.startActivity(intent)
            //return true
            Timber.d("Map is called")
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

            true
        }
        else -> {
            AlertDialog.Builder(this).setTitle("Hey").setMessage("Not Implemented")
                .setPositiveButton("OK"){dialog,which ->}
                .show()
            super.onOptionsItemSelected(item)
        }
    }

    fun addMarkerOnMap(data: Data){
        val latlng = LatLng(data.latitude, data.longitude)
        map.addMarker(
            MarkerOptions()
                .position(latlng)
                .title(latlng2str(latlng))
                .snippet(data2description(data))
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // set map style
        try{
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
            )
            if(!success){
                Timber.e(TAG, "Map Style parsing failed")
            }
        }catch(e: Resources.NotFoundException){
            Timber.e(TAG, "Can't find style: Error: ", e)
        }

        // 一旦東大本郷キャンパスにpanする
        val latitude = 35.7127
        val longitude = 139.7620
        val startLatLng = LatLng(latitude, longitude)
        val zoomLevel = 15f

        // set start point
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, zoomLevel))

        // set infoWindo click listener
        map.setOnInfoWindowClickListener { marker ->
            Timber.d(TAG, "InfoWindowClicked")
            //val intent = Intent(this, OverviewActivity::class.java).apply{
            //    putExtra("com.example.nekomanju.EXTRA", "0")
            //}
            //startActivity(intent)
            val bundle = Bundle()
            bundle.putDouble("com.example.nekomanju.LATITUDE", marker.position.latitude)
            bundle.putDouble("com.example.nekomanju.LONGITUDE", marker.position.longitude)
            val intent = Intent(this, OverviewActivity::class.java).apply{
                //putExtra("com.example.nekomanju.LATLNG", "0")
                putExtras(bundle)
            }
            startActivity(intent)

            //val fragmentManager = supportFragmentManager
            //val fragmentTransaction = fragmentManager.beginTransaction()
            //val frag: OverviewFragment = OverviewFragment()
            //fragmentTransaction.add(R.id.fragment_container, frag).commit()
        }

        // set marker window style
        map.setInfoWindowAdapter(object: GoogleMap.InfoWindowAdapter{
            override fun getInfoWindow(p0: Marker?): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(applicationContext)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(applicationContext)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(applicationContext)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })

        /* set map style here */

        // enable location tracking
        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation(){
        if(isPermissionGranted()){
            map.isMyLocationEnabled = true
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun isPermissionGranted(): Boolean{
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

}