package com.stucs17.stockai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class HomeActivity : AppCompatActivity() {

    private lateinit var Btn_goto_CSActivity : Button
    private lateinit var Btn_goto_ : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        Btn_goto_CSActivity = findViewById(R.id.Btn_goto_CSActivity)

        Btn_goto_CSActivity.setOnClickListener {
            val intent = Intent(this@HomeActivity, CurrentStockPriceActivity::class.java)
            startActivity(intent)
            finish()
        }



    }
}