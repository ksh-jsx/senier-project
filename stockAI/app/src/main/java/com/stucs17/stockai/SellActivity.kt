package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.Public.AccountInfo
import com.stucs17.stockai.Public.SpeechAPI
import com.stucs17.stockai.Public.Trade
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener


class SellActivity : AppCompatActivity(), ITranDataListener, IRealDataListener {

    var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    var m_OrderTranProc: ExpertTranProc? = null //주문
    var m_OrderListTranProc: ExpertTranProc? = null //주문내역 조회
    var m_OrderRealProc: ExpertRealProc? = null

    var m_nJangoRqId = -1 //잔고 TR ID
    var m_nOrderRqId = -1 //주문 TR ID

    private lateinit var tv_stock_name : TextView
    private lateinit var tv_stock_price : TextView
    private lateinit var tv_sell_available_qty : TextView
    private lateinit var btn_plus1 : Button
    private lateinit var btn_minus1 : Button
    private lateinit var btn_plus2 : Button
    private lateinit var btn_minus2 : Button
    private lateinit var buttonBuy : Button
    private lateinit var buttonCancel : Button
    private lateinit var tv_order_price : EditText
    private lateinit var tv_order_qty : EditText
    private lateinit var radio_group : RadioGroup

    private var currentPrice = 0
    private var currentQty = 0
    private var currentName = ""
    private var currentCode = ""
    private var marketName = ""
    private var orderType = "00"

    private val gb = GlobalBackground()
    private val info = AccountInfo()
    private val trade = Trade()
    private val speechAPI = SpeechAPI()

    //sql 관련
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

@SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        //TR 초기화
        m_OrderTranProc = ExpertTranProc(this)
        m_OrderTranProc!!.InitInstance(this)
        m_OrderTranProc!!.SetShowTrLog(true)

        m_JangoTranProc = ExpertTranProc(this)
        m_JangoTranProc!!.InitInstance(this)
        m_JangoTranProc!!.SetShowTrLog(false)

        tv_stock_name = findViewById(R.id.tv_stock_name)
        tv_stock_price = findViewById(R.id.tv_stock_price)
        tv_sell_available_qty = findViewById(R.id.tv_sell_available_qty)
        btn_plus1 = findViewById(R.id.btn_plus1)
        btn_minus1 = findViewById(R.id.btn_minus1)
        btn_plus2 = findViewById(R.id.btn_plus2)
        btn_minus2 = findViewById(R.id.btn_minus2)
        buttonBuy = findViewById(R.id.buttonBuy)
        buttonCancel = findViewById(R.id.buttonCancel)
        tv_order_price = findViewById(R.id.tv_order_price)
        tv_order_qty = findViewById(R.id.tv_order_qty)
        radio_group = findViewById(R.id.radio_group)

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        if(intent.hasExtra("Price")) {
            currentPrice = intent.getIntExtra("Price",0)
            currentName = intent.getStringExtra("Name")!!
            marketName = intent.getStringExtra("Market")!!
            currentCode = intent.getStringExtra("Code")!!
        }

        currentQty = tv_order_qty.text.toString().toInt()
        tv_stock_name.text = currentName
        tv_stock_price.text = "현재가격: "+gb.dec(currentPrice)+" 원"

        radio_group.setOnCheckedChangeListener{ group, checkedId ->
            when(checkedId){
                R.id.radio_btn1 -> {
                    orderType =  "00"
                    btn_plus2.visibility = View.VISIBLE
                    btn_minus2.visibility = View.VISIBLE
                }

                R.id.radio_btn2 -> {
                    orderType =  "01"
                    btn_plus2.visibility = View.GONE
                    btn_minus2.visibility = View.GONE
                }
            }
        }

        val builder = AlertDialog.Builder(this)

        tv_order_price.setText(gb.dec(currentPrice))

        btn_plus1.setOnClickListener {
            val temp = gb.plus(currentQty,1)
            currentQty = temp
            tv_order_qty.setText(temp.toString())
        }
        btn_minus1.setOnClickListener {
            val temp = gb.minus(currentQty,1)
            currentQty = temp
            tv_order_qty.setText(temp.toString())
        }
        btn_plus2.setOnClickListener {
            val temp = gb.plus(currentPrice,getUnit(currentPrice))
            currentPrice = temp
            tv_order_price.setText(gb.dec(temp))
        }
        btn_minus2.setOnClickListener {
            val temp = gb.minus(currentPrice,getUnit(currentPrice))
            currentPrice = temp
            tv_order_price.setText(gb.dec(temp))
        }
        buttonBuy.setOnClickListener {
            builder.setTitle("매도")
            builder.setMessage("$currentName ${currentQty.toString()}주 매도합니다")
            builder.setPositiveButton("네") { dialogInterface: DialogInterface, i: Int ->
                //runSell()
                m_nOrderRqId = trade.runSell(m_OrderTranProc,database,currentCode,orderType,currentQty.toString(),currentPrice.toString())!!
            }
            builder.setNegativeButton("취소") { dialogInterface: DialogInterface, i: Int ->
            }
            builder.show()
        }
        buttonCancel.setOnClickListener {
            finish()
        }

        m_nJangoRqId = info.getJangoInfo(database,m_JangoTranProc)
    }

    override fun onDestroy() {
        super.onDestroy()

        m_OrderTranProc!!.ClearInstance()
        m_OrderTranProc = null
    }

    fun getUnit(price:Int):Int {

        return if(price<1000) 1
        else if(price<5000) 5
        else if(price<10000) 10
        else if(price<50000) 50
        else if(price<100000) 100
        else if(price<500000){
            if(marketName === "KOSPI200") 500
            else 100
        } else{
            if(marketName === "KOSPI200") 1000
            else 100
        }
    }


    @SuppressLint("SetTextI18n")
    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        if (m_nJangoRqId == nRqId) {
            speechAPI.startUsingSpeechSDK2("매도 주문 접수되었습니다")
            val nCount = m_JangoTranProc!!.GetValidCount(0)
            for (i in 0 until nCount) {
                val strName = m_JangoTranProc!!.GetMultiData(0, 1, i)

                if (strName == currentName) {
                    val strQty = m_JangoTranProc!!.GetMultiData(0, 7, i)//수량
                    tv_sell_available_qty.text = strQty+"주"

                }
                //System.out.println("1: " + strCode + ", 2: " + strName)
                //System.out.println("3: " + strQty + ", 4: " + strAverPrice)
            }
        }
    }

    override fun onTranMessageReceived(
        nRqId: Int, strMsgCode: String?,
        strErrorType: String?, strMessage: String?
    ) {
        Toast.makeText(this, strMessage, Toast.LENGTH_LONG).show()
        Thread.sleep(3000)
        Log.e("onTranMessageReceived", String.format("MsgCode:%s ErrorType:%s %s",  strMsgCode ,  strErrorType  , strMessage));
    }

    override fun onTranTimeout(nRqId: Int) {
        // TODO Auto-generated method stub
        Log.e("onTranTimeout", String.format("RqId:%d ", nRqId))
    }

    override fun onRealDataReceived(strServiceId: String) {
        if (strServiceId === "scn_r" || strServiceId === "scn_m") {
            val strOrderNumber = m_OrderRealProc!!.GetRealData(0, 2) //주문번호
            val strOrderGubun = m_OrderRealProc!!.GetRealData(0, 4) //매도매수구분
            val strCode = m_OrderRealProc!!.GetRealData(0, 8) //종목코드

            speechAPI.startUsingSpeechSDK2("매도가 체결되었습니다")
            Log.d(
                "==주식 체결통보==",
                String.format("주문번호:%s 매도매수구분:%s 종목코드:%s", strOrderNumber, strOrderGubun, strCode)
            )
        }
    }

}