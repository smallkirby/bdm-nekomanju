package com.example.nekomanju.util

import android.location.Location
import com.example.nekomanju.R
import com.example.nekomanju.domain.Data
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utility {
    companion object {
        fun latlng2str(latlng: LatLng): String {
            val lat = latlng.latitude.toString()
            val lng = latlng.longitude.toString()
            return "$lat° N, $lng° E"
        }

        fun data2description(data: Data, requireShort: Boolean = false): String {
            var desc = "最終更新: "
            val fhumidity = "%.2f".format(data.humidity)
            val ftemperature = "%.2f".format(data.temperature)
            val fco2 = "%.2f".format(data.co2)
            desc += timeIso2Readable(data.time) + "\n"
            desc += "CO2: ${fco2} ppm\n"
            desc += "温度: ${ftemperature} 度\n"
            desc += "湿度: ${fhumidity} %\n"
            return desc
        }

        fun timeIso2Readable(timestr: String, needDetail: Boolean = true): String {
            val replacedTimestr = timestr.replace("T", ":")
            if (replacedTimestr == timestr) { // 形式に沿っていない
                Timber.e("Error while parsing timestr: $timestr")
                return "ERROR"
            }
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd:HHmmss")
            //val formatter = DateTimeFormatter.ISO_DATE
            val date = LocalDateTime.parse(replacedTimestr, formatter)
            return if(!needDetail){
                date.format(DateTimeFormatter.ofPattern("MM/dd HH:mm"))
            }else{
                date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
            }
        }

        fun asDouble(arg: Any?): Double = when (arg) {
                is Double -> arg as Double
                is Long -> arg.toDouble()
                is String -> arg.toDouble()
                else -> {
                    Timber.e("Uncaught Type @ asDouble()")
                    0.0
            }
        }

        // CO2濃度から現在のステータスレベルをIntで返す。Util内部で使うようの関数
        // この資料(https://www.mhlw.go.jp/seisakunitsuite/bunya/kenkou_iryou/kenkou/seikatsu-eisei/gijutukensyuukai/dl/h23_2.pdf)によると1000ppm以下だといい感じらしい
        // 具体的な根拠はないが、900<=co2<1000ppmをnormal, co2<900をgood, 1000<=co2をbadとする
        private fun co2level(co2: Double): Int{
            return if(co2 < 900.0){
                1
            }else if(900.0 <= co2 && co2 < 1000.0){
                2
            }else{
                3
            }
        }

        // CO2濃度から適切なアイコンのリソース番号への変換
        fun co2icon(co2: Double): Int{
            return when(co2level(co2)){
                1 -> R.drawable.egao
                2 -> R.drawable.komarigao
                3 -> R.drawable.cry
                else -> {
                    Timber.e("Error @ co2icon()")
                    -1
                }
            }
        }

        // CO2濃度から換気状況のアドバイス文字列変換
        fun co2status(co2: Double): String{
            return when(co2level(co2)){
                1 -> "よく換気されています :)"
                2 -> "少し空気が滞っているかも :<"
                3 -> "換気してください! :("
                else ->{
                    Timber.e("Error @ co2status()")
                    "ERROR"
                }
            }
        }

        fun compareLatLng(latlng1: LatLng, latlng2: LatLng, base: LatLng): Int{
            val distarray1: FloatArray = floatArrayOf(0F, 0F, 0F)
            val distarray2: FloatArray = floatArrayOf(0F, 0F, 0F)
            Location.distanceBetween(latlng1.latitude, latlng1.longitude, base.latitude, base.longitude, distarray1)
            Location.distanceBetween(latlng2.latitude, latlng2.longitude, base.latitude, base.longitude, distarray2)
            val dist1 = distarray1[0]
            val dist2 = distarray2[0]
            return if(dist1 > dist2){
                1
            }else if(dist1 == dist2){
                0
            }else{
                -1
            }
        }

        // ISO表示された時刻を2つとり、前者のほうが小さければ-1、等しければ0、前者のほうが大きければ1を返す
        fun compareTimeIsoString(time1: String, time2: String): Int{
            val rtime1 = time1.replace("T", ":")
            val rtime2 = time2.replace("T", ":")
            val formatter = DateTimeFormatter.ofPattern("yyyyMMdd:HHmmss")
            val tt1 = LocalDateTime.parse(rtime1, formatter)
            val tt2 = LocalDateTime.parse(rtime2, formatter)
            return when {
                tt1.isAfter(tt2) -> {
                    1
                }
                tt1.isBefore(tt2) -> {
                    -1
                }
                else -> {
                    0
                }
            }
        }

        fun getLatestData(datas: List<Data>): Data{
            if(datas.isEmpty()){
                Timber.e("Error @ getLatestData()")
                return Data(dataId = 0L, temperature = 0.0, humidity = 0.0, latitude = 0.0, longitude = 0.0, co2 = 0.0, time = "0")
            }
            var latest = datas[0]
            for(data in datas){
                if(compareTimeIsoString(latest.time, data.time) == -1){
                    latest = data
                }
            }
            return latest
        }

        fun findLatLngLatestMatch(datas: List<Data>, latlng: LatLng): Data{
            var matchedData = arrayListOf<Data>()
            for(data in datas){
                if(LatLng(data.latitude, data.longitude) == latlng){
                    matchedData.add(data)
                }
            }
            if(matchedData.isEmpty()){ // not found
                Timber.e("Error @ findLatlngFirstMatch()")
                return Data(dataId = 0L, temperature = 0.0, humidity = 0.0, latitude = 0.0, longitude = 0.0, co2 = 0.0, time = "0")
            }
            return getLatestData(matchedData)
        }

        fun findLatlngFirstMatch(datas: List<Data>, latlng: LatLng): Data{
            for(data in datas){
                if(LatLng(data.latitude, data.longitude) == latlng){
                    return data
                }
            }
            Timber.e("Error @ findLatlngFirstMatch()")
            return Data(dataId = 0L, temperature = 0.0, humidity = 0.0, latitude = 0.0, longitude = 0.0, co2 = 0.0, time = "0")
        }

        // 小数点以下の桁数を指定して文字列にする拡張関数
        fun Double.formatPrecise(precise: Int): String{
            return "%.${precise}f".format(this)
        }

        fun getNotificationMessage(data: Data): String{
            return "${data.latitude.formatPrecise(2)}° N${data.longitude.formatPrecise(2)}° E  CO2: ${data.co2.formatPrecise(1)} ppm"
        }
    }
}