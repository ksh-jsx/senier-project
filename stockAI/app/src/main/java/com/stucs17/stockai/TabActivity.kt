package com.stucs17.stockai

import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.stucs17.stockai.Public.BackgroundWorker
import com.stucs17.stockai.Public.SpeechAPI
import com.stucs17.stockai.adapter.PageAdapter
import kotlinx.android.synthetic.main.activity_tab.*
import java.util.*
import kotlin.random.Random.Default.nextInt

class TabActivity : AppCompatActivity() {



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
        //displayNotification()
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

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis,1000*60, pIntent)
    }

}