package com.stucs17.stockai

import android.os.Bundle
import android.util.Log
import android.widget.Button
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        //TR 초기화
        m_OrderTranProc = ExpertTranProc(this)
        m_OrderTranProc!!.InitInstance(this)
        m_OrderTranProc!!.SetShowTrLog(true)

        buttonBuy = findViewById(R.id.buttonBuy)

        buttonBuy.setOnClickListener {

            runBuy()
        }

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

        //if (eStockCode == null) return
        //m_strCode = eStockCode.getText().toString()
        //m_OrderTranProc!!.SetSingleData(0, 3, m_strCode) //상품코드
        m_OrderTranProc!!.SetSingleData(0, 3, "096040") //상품코드
        m_OrderTranProc!!.SetSingleData(0, 4, "00") //주문구분  00:지정가

        //주문수량
        m_OrderTranProc!!.SetSingleData(0, 5, "1")

        //주문단가
        //strOrderPrice = ePrice.getText().toString()
        //m_OrderTranProc!!.SetSingleData(0, 6, strOrderPrice)
        m_OrderTranProc!!.SetSingleData(0, 6, "")
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

    }

    override fun onTranTimeout(nRqId: Int) {
        // TODO Auto-generated method stub
        Log.e("onTranTimeout", String.format("RqId:%d ", nRqId))
    }

    override fun onRealDataReceived(strServiceId: String) {

    }

}