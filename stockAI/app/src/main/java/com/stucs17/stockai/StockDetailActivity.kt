package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.Public.Database
import com.stucs17.stockai.Public.StockIndex
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

class StockDetailActivity : AppCompatActivity(), ITranDataListener {

    private val gb = GlobalBackground()
    private val db = Database()
    private val stockInfo = StockIndex()
    private lateinit var expertTranProc : ExpertTranProc
    private var currentPriceRqId = 0

    private var currentPrice = 0
    private var stockMarket = ""
    private var stockCode = ""
    private var stockName = ""

    private lateinit var tv19 : TextView
    private lateinit var tv20 : TextView
    private lateinit var tv18 : TextView
    private lateinit var tv43 : TextView
    private lateinit var btn_star : ImageButton
    private lateinit var btn_heart : ImageButton
    private lateinit var tv_stock_name : TextView
    private lateinit var priceView : TextView
    private lateinit var priceView2 : TextView
    private lateinit var priceBox : LinearLayout
    private lateinit var buttonForBuy : Button
    private lateinit var buttonForSell : Button
    val TAG = "****** SD ******"

    //sql 관련
    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase


    @SuppressLint("Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_detail)

        if(intent.hasExtra("stockCode")) {
            stockCode = intent.getStringExtra("stockCode")!!
            stockName = intent.getStringExtra("stockName")!!
            Log.d(TAG, "code: $stockCode / name: $stockName")
        }
        tv19 = findViewById(R.id.tv19) // 상한가
        tv20 = findViewById(R.id.tv20) // 하한가
        tv18 = findViewById(R.id.tv18) // PER
        tv43 = findViewById(R.id.tv43) // PBR
        btn_star = findViewById(R.id.btn_star)
        btn_heart = findViewById(R.id.btn_heart)
        tv_stock_name = findViewById(R.id.tv_stock_name)
        priceView = findViewById(R.id.priceView) // 현재가
        priceView2 = findViewById(R.id.priceView2) // 변동치
        priceBox = findViewById(R.id.priceBox)
        buttonForBuy = findViewById(R.id.buttonForBuy)
        buttonForSell = findViewById(R.id.buttonForSell)

        expertTranProc = ExpertTranProc(this)
        expertTranProc.InitInstance(this)
        expertTranProc.SetShowTrLog(true)

        currentPriceRqId = stockInfo.getStockInfo(expertTranProc,stockCode)

        tv_stock_name.text = stockName

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        val c = db.isExist_like(database,stockCode)!!
        val c2 = db.isExist_autoTradeTarget(database,stockCode)!!

        if(c.count> 0)
            btn_heart.setImageResource(R.drawable.heart_on)
        else
            btn_heart.setImageResource(R.drawable.heart_off)
        btn_heart.setOnClickListener {
            if(c.count> 0){
                db.delete_like(database,stockCode)
                btn_heart.setImageResource(R.drawable.heart_off)
            }
            else {
                val contentValues = ContentValues()
                contentValues.put("code", stockCode)
                contentValues.put("name", stockName)

                db.insert_like(contentValues, database)
                btn_heart.setImageResource(R.drawable.heart_on)
            }
        }

        val c_select = db.select(database)!!
        if(c_select.moveToNext()) {
            val state = c_select.getString(c_select.getColumnIndex("autoTrade")).toInt()
            if(state == 0){
                btn_star.visibility = View.GONE
            }
            else
                btn_star.visibility = View.VISIBLE
        }

        if(c2.count> 0)
            btn_star.setImageResource(R.drawable.star_on)
        else
            btn_star.setImageResource(R.drawable.star_off)

        btn_star.setOnClickListener {
            if(c2.count> 0){
                db.delete_autoTradeTarget(database,stockCode)
                btn_star.setImageResource(R.drawable.star_off)
            }
            else {
                val contentValues = ContentValues()
                contentValues.put("code", stockCode)
                contentValues.put("name", stockName)

                db.insert_autoTradeTarget(contentValues, database)
                btn_star.setImageResource(R.drawable.star_on)
            }
        }

        buttonForBuy.setOnClickListener {
            val intent = Intent(this@StockDetailActivity, BuyActivity::class.java)
            intent.putExtra("Name",tv_stock_name.text.toString())
            intent.putExtra("Price",currentPrice)
            intent.putExtra("Code",stockCode)
            intent.putExtra("Market",stockMarket)
            startActivity(intent)
            //finish()
        }

        buttonForSell.setOnClickListener {
            val intent = Intent(this@StockDetailActivity, SellActivity::class.java)
            intent.putExtra("Name",tv_stock_name.text.toString())
            intent.putExtra("Price",currentPrice)
            intent.putExtra("Code",stockCode)
            intent.putExtra("Market",stockMarket)
            startActivity(intent)
            //finish()
        }

        Thread {
            while (!Thread.interrupted()) try {
                Thread.sleep(1000)
                runOnUiThread { currentPriceRqId = stockInfo.getStockInfo(expertTranProc,stockCode) }
            } catch (e: InterruptedException) {
                // ooops
            }
        }.start()
    }

    @SuppressLint("SetTextI18n")
    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        if (sTranID?.contains("scp") == true && currentPriceRqId == nRqId) {
            currentPrice = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
            val dayChange = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비
            stockMarket = expertTranProc.GetSingleData(0, 2) // 2 : 대표 시장 한글명

            tv19.text = gb.dec(expertTranProc.GetSingleData(0, 19).toInt()) // 19 : 최고가
            tv20.text = gb.dec(expertTranProc.GetSingleData(0, 20).toInt()) // 20 : 최저가
            tv18.text = gb.dec(expertTranProc.GetSingleData(0, 18).toInt()) // 18 : 주식 시가

            val tmp = expertTranProc.GetSingleData(0, 43) // 43 : PER
            var cnt = 0
            for(i in tmp){
                if(i.toString() == "0")
                    cnt++
            }
            val rng = IntRange(cnt,tmp.length-1)
            tv43.text = tmp.slice(rng)


            for(i in 0..50) {
                val info = expertTranProc.GetSingleData(0, i)
                //Log.d(TAG, i.toString()+":"+info)
            }
            var plus="";
            val variancePercent = (abs(dayChange.toDouble()) /(currentPrice+dayChange*(-1)).toDouble())*100
            val setDecimal = (variancePercent * 100).roundToInt() /100f

            if(dayChange>0) {
                priceBox.setBackgroundResource(R.drawable.radius_red)
                plus = "+"
            }
            else {
                priceBox.setBackgroundResource(R.drawable.radius_blue)
            }

            priceView.text = gb.dec(currentPrice)+"원"
            priceView2.text = plus+""+gb.dec(dayChange)+", "+"$setDecimal%"
            //Toast.makeText(this, "현재가 : $currentPrice, 전일 대비 : $dayChange", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun onTranTimeout(p0: Int) {
        TODO("Not yet implemented")
    }
}