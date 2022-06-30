package com.stucs17.stockai

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
}