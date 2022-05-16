package com.stucs17.stockai

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener

class BuyActivity : AppCompatActivity(), ITranDataListener, IRealDataListener {

    var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    var m_OrderTranProc: ExpertTranProc? = null //주문
    var m_OrderListTranProc: ExpertTranProc? = null //주문내역 조회
    var m_OrderRealProc: ExpertRealProc? = null

    var m_nJangoRqId = -1 //잔고 TR ID
    var m_nOrderRqId = -1 //주문 TR ID
    var m_nOrderListRqId = -1 //주문내역 TR ID

    private lateinit var buttonBuy : Button
    private lateinit var tv_order_price : EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        //TR 초기화
        m_OrderTranProc = ExpertTranProc(this)
        m_OrderTranProc!!.InitInstance(this)
        m_OrderTranProc!!.SetShowTrLog(true)

        buttonBuy = findViewById(R.id.buttonBuy)
        tv_order_price = findViewById(R.id.tv_order_price)

        buttonBuy.setOnClickListener {

            runBuy()
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        m_OrderTranProc!!.ClearInstance()
        m_OrderTranProc = null
    }

    fun runBuy() {
        var strPass = "9877"
        var strEncPass = ""
        var m_strCode = ""
        var strOrderPrice = ""

        m_OrderTranProc!!.ClearInblockData()

        //계좌
        m_OrderTranProc!!.SetSingleData(0, 0, "68067116")
        //상품코드
        m_OrderTranProc!!.SetSingleData(0, 1, "01")
        //비밀번호
        strEncPass = m_OrderTranProc!!.GetEncryptPassword(strPass)
        m_OrderTranProc!!.SetSingleData(0, 2, strEncPass)



        m_OrderTranProc!!.SetSingleData(0, 3, "096040") //상품코드
        m_OrderTranProc!!.SetSingleData(0, 4, "00") //주문구분  00:지정가

        //주문수량
        m_OrderTranProc!!.SetSingleData(0, 5, "1")

        //주문단가
        if (tv_order_price == null) return
        m_strCode = tv_order_price.getText().toString()
        m_OrderTranProc!!.SetSingleData(0, 6, m_strCode)
        m_OrderTranProc!!.SetSingleData(0, 7, " ") //연락전화번호

        //축약서명
        m_OrderTranProc!!.SetCertType(1)
        //매수주문
        m_OrderTranProc!!.RequestData("scabo")

        //tResult.setText("매수 " + m_strCode + "  " + strOrderPrice)
    }

    override fun onTranDataReceived(sTranID: String, nRqId: Int) {

    }

    override fun onTranMessageReceived(
        nRqId: Int, strMsgCode: String?,
        strErrorType: String?, strMessage: String?
    ) {

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