package com.stucs17.stockai

import android.Manifest
import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.getActivity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.commexpert.ExpertTranProc
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.kakao.sdk.newtoneapi.TextToSpeechManager
import com.stucs17.stockai.Public.BackgroundWorker
import com.stucs17.stockai.Public.BackgroundWorker_autoTrade
import com.stucs17.stockai.Public.SpeechAPI
import com.stucs17.stockai.adapter.PageAdapter
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import kotlinx.android.synthetic.main.activity_tab.*
import java.util.*
import kotlin.random.Random.Default.nextInt

class TabActivity : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 1000
    private val STORAGE_REQUEST_CODE = 1000
    private val NETWORK_STATE_CODE = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)


        var currTab = 0
        val adapter = PageAdapter(supportFragmentManager)
        adapter.addFragment(Tab1(), "홈")
        adapter.addFragment(Tab2(), "매수/매도")
        adapter.addFragment(Tab3(), "자동 투자")

        if(intent.hasExtra("tab")) {
            currTab = intent.getIntExtra("tab", 0)
        }

        viewpager.adapter = adapter
        tab_layout.setupWithViewPager(viewpager)
        viewpager.currentItem = currTab

        addAlarm()
        addAutoTrade()
        //displayNotification()
        setupPermissions()
    }

    private fun setupPermissions(){
        val permission_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val permission_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission_network = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        if(permission_audio != PackageManager.PERMISSION_GRANTED) {
            Log.d(ContentValues.TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        } else if(permission_storage != PackageManager.PERMISSION_GRANTED) {
            Log.d(ContentValues.TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else if(permission_network != PackageManager.PERMISSION_GRANTED){
            Log.d(ContentValues.TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)
        }
        else {
            //본문실행
            SpeechRecognizerManager.getInstance().initializeLibrary(this)
            TextToSpeechManager.getInstance().initializeLibrary(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
            NETWORK_STATE_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when(keyCode) {

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(this@TabActivity, SpeechAPI::class.java)
                startActivity(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun addAlarm(){
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BackgroundWorker::class.java)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val cal = Calendar.getInstance()
        cal.set(Calendar.SECOND, 0)

        alarmManager.setInexactRepeating (AlarmManager.RTC_WAKEUP, cal.timeInMillis,1000*60, pIntent)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun addAutoTrade(){
        val alarmManager = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BackgroundWorker_autoTrade::class.java)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 9)
        cal.set(Calendar.MINUTE,1)
        cal.set(Calendar.SECOND, 0)

        alarmManager.setInexactRepeating (AlarmManager.RTC_WAKEUP, cal.timeInMillis,AlarmManager.INTERVAL_DAY, pIntent)
    }
}