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
        Log.d("test", currTab.toString())
        viewpager.adapter = adapter
        tab_layout.setupWithViewPager(viewpager)
        viewpager.currentItem = currTab

        addAlarm()
        addAutoTrade()
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
        cal.set(Calendar.MINUTE,0)
        cal.set(Calendar.SECOND, 0)

        alarmManager.setInexactRepeating (AlarmManager.RTC_WAKEUP, cal.timeInMillis,1000*60*60*24, pIntent)
    }
}