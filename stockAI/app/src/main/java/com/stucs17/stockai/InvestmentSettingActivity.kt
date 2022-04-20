package com.stucs17.stockai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView

class InvestmentSettingActivity : AppCompatActivity() {

    private lateinit var seekBar_for_risk : SeekBar
    private lateinit var textView_for_risk : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_investment_setting)

        seekBar_for_risk = findViewById(R.id.seekBar_for_risk)
        textView_for_risk = findViewById(R.id.textView_for_risk)
        seekBar_for_risk.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            val strArr = arrayOf<String>("매우 낮음","낮음","중간","높음","매우 높음")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView_for_risk.text = strArr[progress]
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
        })
    }
}