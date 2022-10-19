package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.Public.AccountInfo
import com.stucs17.stockai.Public.Database
import com.stucs17.stockai.Public.Trade
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener

class BuyActivity : AppCompatActivity(), ITranDataListener, IRealDataListener {

    var m_OrderTranProc: ExpertTranProc? = null //주문
    var m_OrderListTranProc: ExpertTranProc? = null //주문내역 조회
    var m_OrderRealProc: ExpertRealProc? = null

    private var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    var m_nJangoRqId = -1 //잔고 TR ID
    var m_nOrderRqId = -1 //주문 TR ID

    private lateinit var tv_stock_name : TextView
    private lateinit var tv_stock_price : TextView
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

    private var strTotal1 = 0
    private var strTotal2 = 0
    private var buyPriceSum = 0

    private val db = Database()
    private val gb = GlobalBackground()
    private val trade = Trade()
    private val info = AccountInfo()
    //sql 관련
    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        m_JangoTranProc = ExpertTranProc(this)
        m_JangoTranProc!!.InitInstance(this)
        m_JangoTranProc!!.SetShowTrLog(false)

        //TR 초기화
        m_OrderTranProc = ExpertTranProc(this)
        m_OrderTranProc!!.InitInstance(this)
        m_OrderTranProc!!.SetShowTrLog(true)

        tv_stock_name = findViewById(R.id.tv_stock_name)
        tv_stock_price = findViewById(R.id.tv_stock_price)
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
                R.id.radio_btn1 -> orderType =  "00"

                R.id.radio_btn2 -> orderType =  "01"
            }
        }

        val builder = AlertDialog.Builder(this)

        tv_order_price.setText(gb.dec(currentPrice))
        m_nJangoRqId = info.getJangoInfo(database,m_JangoTranProc)

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
            builder.setTitle("매수")
            builder.setMessage("$currentName $currentQty 주 매수합니다")
            builder.setPositiveButton("네") { _: DialogInterface, i: Int ->
                if((strTotal1-strTotal2)-buyPriceSum>currentQty*currentPrice)
                    m_nOrderRqId = trade.runBuy(m_OrderTranProc,database,currentCode,orderType,currentQty.toString(),currentPrice.toString())!!
                else
                    Toast.makeText(this@BuyActivity, "잔액이 부족합니다", Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton("취소") { _: DialogInterface, i: Int -> }
            builder.show()
        }
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        m_OrderTranProc!!.ClearInstance()
        m_OrderTranProc = null
    }

    private fun getUnit(price:Int):Int {

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

    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        Log.d("onTranDataReceived","$sTranID / $nRqId / $m_nOrderRqId")
        if (m_nJangoRqId == nRqId) {

            strTotal1 = m_JangoTranProc!!.GetMultiData(1, 14, 0).toInt() //총평가금액
            strTotal2 = m_JangoTranProc!!.GetMultiData(1, 19, 0).toInt() //손익
            buyPriceSum = 0
            //보유 주식 수
            val nCount = m_JangoTranProc!!.GetValidCount(0)


            for (i in 0 until nCount) {
                //종목
                val strCode = m_JangoTranProc!!.GetMultiData(0, 0, i)
                //잔고
                val strbuyPrice = m_JangoTranProc!!.GetMultiData(0, 10, i) // 매입금액


                if(strCode.length > 3){
                    buyPriceSum+=strbuyPrice.toInt()
                }
            }
        } else if(m_nOrderRqId == nRqId){

            val orderNumberKET = m_OrderTranProc!!.GetSingleData(0,0) //한국거래소전송주문조직번호
            val orderNumber = m_OrderTranProc!!.GetSingleData(0,1) //주문번호
            val orderTime = m_OrderTranProc!!.GetSingleData(0,2) //주문시각

            Log.d("주문 접수","$orderNumberKET / $orderNumber / $orderTime")

            val contentValues = ContentValues()
            contentValues.put("OrderNumberOri", orderNumber)
            contentValues.put("orderNumberKET", orderNumberKET)

            db.insert_order(contentValues,database)
        }
    }

    override fun onTranMessageReceived(
        nRqId: Int, strMsgCode: String?,
        strErrorType: String?, strMessage: String?
    ) {
        if(strMsgCode == "APBK0013")
            Toast.makeText(this, strMessage, Toast.LENGTH_LONG).show()
            // TODO Auto-generated method stub
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
            Log.d(
                "==주식 체결통보==",
                String.format("주문번호:%s 매도매수구분:%s 종목코드:%s", strOrderNumber, strOrderGubun, strCode)
            )
        }
    }

}