package com.mukicloud.mukitest.SFunc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.mukicloud.mukitest.R
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.json.JSONArray
import org.json.JSONObject


class SBeaconService : LifecycleService() {
    private val mBinder: IBinder = SBinder()
    private val svc = this
    private var serviceAlive = true
    private var beaconJA = JSONArray() //最新裝置清單
    var sm: SMethods? = null

    override fun onCreate() {
        super.onCreate()
        serviceAlive = true
        sm = SMethods(svc)
        showServiceNotification()
        startScanBeacon()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return mBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY //殺不死方法之一  //return super.onStartCommand(intent, flags, startId);
    }

    override fun onDestroy() {
        serviceAlive = false
        stopScanBeacon()
        stopForeground(true) // 停止前台服务
        super.onDestroy()
    }

    inner class SBinder : Binder() {
        fun getService(): SBeaconService {
            return svc
        }
    }

    companion object {
        @JvmStatic
        fun startService(Con: Context) {
            val intent = Intent(Con, SBeaconService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Con.startForegroundService(intent)
            } else {
                Con.startService(intent)
            }
        }
    }

    //Beacon========================================================================================
    /** 重新調整格式 */
    val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"

    /** 設置興趣UUID */
//    val filterUUID = "fda50693-a4e2-4fb1-afcf-c6eb07647825"
    private val filterUUID = "288333B2-82B2-445F-A1D0-3B3BEFA92CF5"
    private val beaconManager: BeaconManager by lazy(LazyThreadSafetyMode.NONE) {
        BeaconManager.getInstanceForApplication(this)
    }
    private var region: Region = Region(filterUUID, null, null, null)
//    private val tag = "SBeacon"


    private fun startScanBeacon() {
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_FORMAT))
        beaconManager.backgroundScanPeriod = 4000L//背景時，掃描一次的時間，時間越長取樣率越平均
        beaconManager.backgroundBetweenScanPeriod = 5000L//背景時，掃描與掃描間的間隔時間
        beaconManager.foregroundScanPeriod = 4000L//掃描一次的時間，時間越長取樣率越平均
        beaconManager.foregroundBetweenScanPeriod = 5000L//掃描與掃描間的間隔時間
        beaconManager.getRegionViewModel(region).rangedBeacons.observe(this, rangingObserver)
        beaconManager.startRangingBeacons(region)
    }

    private fun stopScanBeacon() {
        beaconManager.stopRangingBeacons(region)
    }

    private val rangingObserver = Observer<Collection<Beacon>> { beacons ->
        try {
            beaconJA = JSONArray()
            if (beacons.isNotEmpty()) {
                for (beacon: Beacon in beacons) {
                    val beaconJOB = JSONObject()
                    beaconJOB.put("bluetoothName", beacon.bluetoothName)
                    beaconJOB.put("bluetoothAddress", beacon.bluetoothAddress)
                    beaconJOB.put("uuid", beacon.id1)
                    beaconJOB.put("major", beacon.id2)
                    beaconJOB.put("minor", beacon.id3)
                    beaconJOB.put("rssi", beacon.rssi)
                    beaconJOB.put("distance", String.format("%.2f", beacon.distance))//meter
                    beaconJA.put(beaconJOB)
                }
            }
        } catch (e: Exception) {
            sm?.UIToast(e.message)
        }
    }

    //BeaconJA======================================================================================
    fun getBeaconJA(): JSONArray {
        return beaconJA
    }

    //Notification Run Foreground===================================================================
    private val NOTIFICATION_CHANNEL_ID = "BeaconService"
    private val NOTIFICATION_CHANNEL_NAME = "BeaconService"
    private fun showServiceNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(false)
            notificationChannel.setSound(null, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val sdkVersion = Build.VERSION.SDK_INT
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP && sdkVersion < Build.VERSION_CODES.N) R.drawable.ic_app else R.drawable.ic_app)
            .setSound(null)
            .setVibrate(null)
            .setTicker(sm!!.IDStr(R.string.app_name))
            .setContentTitle(sm!!.IDStr(R.string.app_name))
            .setContentText(sm!!.IDStr(R.string.app_name) + " Beacon Service")
        startForeground(100, notificationBuilder.build()) // 开始前台服务
    }
}