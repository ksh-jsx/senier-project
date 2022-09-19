package com.stucs17.stockai.Public

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class StockIndex : AppCompatActivity() {

    private val weburl = "https://finance.naver.com/"
    private val speechAPI = SpeechAPI()
    private var type : String? = ""



    private val TAG = "****** SI ******"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        MyAsyncTask().execute(weburl)

    }

    private fun speak(listKospi:ArrayList<String>,listKosdaq:ArrayList<String>) {
        var change = ""
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
        }
        when(type){
            "kospi"->{ //손익
                speechAPI.startUsingSpeechSDK2("현재 코스피 지수는 ${listKospi[0]} 포인트 입니다. 어제보다 ${listKospi[1]} 포인트 $change ${listKospi[5]} 했습니다.")
            }
            "kosdaq"->{ //총자산
                speechAPI.startUsingSpeechSDK2("현재 코스닥 지수는 ${listKosdaq[0]} 포인트 입니다. 어제보다 ${listKosdaq[1]} 포인트 $change ${listKospi[5]} 했습니다.")
            }
        }
        Thread.sleep(5000)
        gotoTab2()
    }

    private fun gotoTab2() {
        val intent = Intent(this@StockIndex, TabActivity::class.java)
        intent.putExtra("tab", 0)
        startActivity(intent)
        finish()
    }

    @SuppressLint("StaticFieldLeak")
    inner class MyAsyncTask: AsyncTask<String, String, String>() { //input, progress update type, result type

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        override fun doInBackground(vararg params: String?): String {

            val doc: Document = Jsoup.connect(weburl).get()
            val kospi: Elements = doc.select("#content > div.article > div.section2 > div.section_stock_market > div.section_stock > div.kospi_area.group_quot.quot_opn > div.heading_area > a > span span")
            val kosdaq: Elements = doc.select("#content > div.article > div.section2 > div.section_stock_market > div.section_stock > div.kosdaq_area.group_quot > div.heading_area > a > span span")

            val listKospi = ArrayList<String>()
            val listKosdaq = ArrayList<String>()

            kospi.forEachIndexed { index, elem ->
                val kospiText = elem.select("span").text()
                listKospi.add(kospiText)
            }
            kosdaq.forEachIndexed { index, elem ->
                val kosdaqText = elem.select("span").text()
                listKosdaq.add(kosdaqText)
            }
            speak(listKospi,listKosdaq)
            return ""
        }
    }

}