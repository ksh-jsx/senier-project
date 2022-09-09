package com.stucs17.stockai

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.stucs17.stockai.Public.Listen
import com.stucs17.stockai.adapter.PageAdapter
import kotlinx.android.synthetic.main.activity_tab.*

class TabActivity : AppCompatActivity() {

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)

        val adapter = PageAdapter(supportFragmentManager)
        adapter.addFragment(Tab1(), "홈")
        adapter.addFragment(Tab2(), "매수/매도")
        adapter.addFragment(Tab3(), "자동 투자")

        viewpager.adapter = adapter
        tab_layout.setupWithViewPager(viewpager)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when(keyCode) {

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val intent = Intent(this@TabActivity, Listen::class.java)
                startActivity(intent)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}