package com.stucs17.stockai.Public

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.GlobalBackground
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class StockIndex : AppCompatActivity(), ITranDataListener {

    private val weburl = "https://finance.naver.com/"
    private val speechAPI = SpeechAPI()
    private val gb = GlobalBackground()
    private var type : String? = ""

    private lateinit var expertTranProc : ExpertTranProc
    private var currentPriceRqId = 0

    var info11 =  0// 11 : 주식 현재가
    var info12 = 0 // 12 : 전일 대비
    var info2 = "" // 2 : 대표 시장 한글명
    var info19 = "" // 19 : 최고가
    var info20 = "" // 20 : 최저가
    var info18 = "" // 18 : 시가
    var info43 = "" // 43 : PER


    private val TAG = "****** SI ******"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        MyAsyncTask().execute(weburl)

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

    private fun speak(listKospi:ArrayList<String>,listKosdaq:ArrayList<String>) {
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
        }
        when(type){
            "kospi"->{ //손익
                speechAPI.startUsingSpeechSDK2("현재 코스피 지수는 ${listKospi[0]} 포인트 입니다. 어제보다 ${listKospi[1]} 포인트 ${listKospi[5]} 했습니다.")
            }
            "kosdaq"->{ //총자산
                speechAPI.startUsingSpeechSDK2("현재 코스닥 지수는 ${listKosdaq[0]} 포인트 입니다. 어제보다 ${listKosdaq[1]} 포인트 ${listKospi[5]} 했습니다.")
            }
        }
        Thread.sleep(5000)
        gotoTab2()
    }

    fun getStockInfo(expertTranProc:ExpertTranProc,stockCode:String): Int {

        expertTranProc.SetSingleData(0,0, "J") // J 는 주식
        expertTranProc.SetSingleData(0,1, stockCode)

        return expertTranProc.RequestData("scp")
    }

    private fun gotoTab2() {
        val intent = Intent(this@StockIndex, TabActivity::class.java)
        intent.putExtra("tab", 1)
        startActivity(intent)
        finish()
    }

    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        Log.d(TAG, "this is: $sTranID")

        info11 = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
        info12 = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비
        info2 = expertTranProc.GetSingleData(0, 2) // 2 : 대표 시장 한글명

        info19 = gb.dec(expertTranProc.GetSingleData(0, 19).toInt()) // 19 : 최고가
        info20 = gb.dec(expertTranProc.GetSingleData(0, 20).toInt()) // 20 : 최저가
        info18 = gb.dec(expertTranProc.GetSingleData(0, 18).toInt()) // 18 : PER
        info43 = expertTranProc.GetSingleData(0, 43)

    }

    override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun onTranTimeout(p0: Int) {
        TODO("Not yet implemented")
    }

}