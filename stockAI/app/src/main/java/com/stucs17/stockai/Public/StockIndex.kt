package com.stucs17.stockai.Public

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.GlobalBackground
import com.stucs17.stockai.R
import com.stucs17.stockai.StockDetailActivity
import com.stucs17.stockai.TabActivity
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.lang.Math.abs

class StockIndex : AppCompatActivity(), ITranDataListener {

    private val weburl = "https://finance.naver.com/"
    private val speechAPI = SpeechAPI()
    private var type : String? = ""
    private var target : String = ""
    private var code : String = ""

    private lateinit var expertTranProc : ExpertTranProc
    private var currentPriceRqId = 0

    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록


    private val TAG = "****** SI ******"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
        }
        when(type){
            "kospi"->{ //코스피
                MyAsyncTask().execute(weburl)
            }
            "kosdaq"->{ //코스닥
                MyAsyncTask().execute(weburl)
            }
            "stockPrice"->{ //주가
                expertTranProc = ExpertTranProc(this)
                expertTranProc.InitInstance(this)
                expertTranProc.SetShowTrLog(true)
                if(intent.hasExtra("target")) {
                    target = intent.getStringExtra("target")
                    code = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(target) }[0].code //입력한 텍스트와 주식 목록 비교->필터링
                    currentPriceRqId = getStockInfo(expertTranProc,code)
                }

            }
        }


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

    private fun gotoStockDetail() {
        val intent = Intent(this@StockIndex, StockDetailActivity::class.java)
        Log.d(TAG, "code: $code / name: $target")
        intent.putExtra("stockCode",code)
        intent.putExtra("stockName",target)
        startActivity(intent)
        finish()
    }

    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        Log.d(TAG, "this is: $sTranID")

        val info11 = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
        val info12 = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비
        val info13 = expertTranProc.GetSingleData(0, 13).toInt() // 13 :전일 대비 부호
        val tmp = if (info13 == 1) "상승" else "하락"

        speechAPI.startUsingSpeechSDK2("현재 $target 주가는 $info11 로 전일대비 ${kotlin.math.abs(info12)} 원 $tmp 하였습니다.")
        Thread.sleep(3000)
        gotoStockDetail()

    }

    override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun onTranTimeout(p0: Int) {
        TODO("Not yet implemented")
    }

}