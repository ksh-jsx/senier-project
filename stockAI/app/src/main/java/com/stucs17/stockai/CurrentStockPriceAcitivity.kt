package com.stucs17.stockai

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.commexpert.intrf.ITranDataListener


class CurrentStockPriceAcitivity : AppCompatActivity(), ITranDataListener {

    private var currentPriceRqId = 0
    private lateinit var expertTranProc : ExpertTranProc
    private lateinit var buttonSearch : Button
    private lateinit var editCode : EditText
    private lateinit var priceBox : LinearLayout
    private lateinit var priceView : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_stock_price_acitivity)

        expertTranProc = ExpertTranProc(this)
        expertTranProc.InitInstance(this)
        expertTranProc.SetShowTrLog(true)

        buttonSearch = findViewById(R.id.buttonSearch)
        editCode = findViewById(R.id.editCode)
        priceBox = findViewById(R.id.priceBox)
        priceView = findViewById(R.id.priceView)
        buttonSearch.setOnClickListener {

            requestCurrentPrice(editCode.text.toString())
        }
    }

    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        if (sTranID?.contains("scp") == true && currentPriceRqId == nRqId) {
            val currentPrice = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
            val dayChange = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비

            if(dayChange>0)
                priceBox.setBackgroundResource(R.drawable.radius_red)
            else
                priceBox.setBackgroundResource(R.drawable.radius_blue)
            
            priceView.setText("$currentPrice 원")
            //Toast.makeText(this, "현재가 : $currentPrice, 전일 대비 : $dayChange", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTranMessageReceived(nRqId: Int, strMsgCode: String?, strErrorType: String?,strMessage: String? ) {
        val msg : String = "$nRqId, $strMsgCode, $strErrorType, $strMessage"
        Log.d(TAG, msg)
    }

    override fun onTranTimeout(nRqId: Int) {
        val msg : String = "$nRqId"
        Log.d(TAG, msg)
    }

    private fun requestCurrentPrice(code: String){
        if (code.isNotEmpty()) {
            expertTranProc.ClearInblockData()
            expertTranProc.SetSingleData(0,0, "J") // J 는 주식
            expertTranProc.SetSingleData(0,1, code)
            currentPriceRqId = expertTranProc.RequestData("scp")
        }
    }
}