package com.stucs17.stockai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.text.DecimalFormat

class HomeActivity : AppCompatActivity(), ITranDataListener {

    var m_JangoTranProc: ExpertTranProc? = null //잔고 조회

    var m_nJangoRqId = -1 //잔고 TR ID

    private lateinit var tv_total_assets : TextView
    private lateinit var tv_total_profit_or_loss : TextView

    private lateinit var btn_goto_CSActivity : Button
    private lateinit var btn_goto_ISActivity : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        m_JangoTranProc = ExpertTranProc(this@HomeActivity)
        m_JangoTranProc!!.InitInstance(this@HomeActivity)
        m_JangoTranProc!!.SetShowTrLog(false)

        tv_total_assets = findViewById(R.id.tv_total_assets)
        tv_total_profit_or_loss = findViewById(R.id.tv_total_profit_or_loss)
        btn_goto_CSActivity = findViewById(R.id.btn_goto_CSActivity)
        btn_goto_ISActivity = findViewById(R.id.btn_goto_ISActivity)

        btn_goto_CSActivity.setOnClickListener {
            val intent = Intent(this@HomeActivity, CurrentStockPriceActivity::class.java)
            startActivity(intent)
            finish()
        }
        btn_goto_ISActivity.setOnClickListener {
            val intent = Intent(this@HomeActivity, InvestmentSettingActivity::class.java)
            startActivity(intent)
            finish()
        }

        getJango()
    }

    override fun onDestroy() {
        super.onDestroy()

        m_JangoTranProc!!.ClearInstance()
        m_JangoTranProc = null
    }

    fun getJango(){
        var strPass = "9877"
        var strEncPass = ""
        m_JangoTranProc!!.ClearInblockData()
        //if (tStatus == null) return
        m_JangoTranProc!!.SetSingleData(0, 0, "68067116")
        //상품코드
        m_JangoTranProc!!.SetSingleData(0, 1, "01")

        strEncPass = m_JangoTranProc!!.GetEncryptPassword(strPass)
        m_JangoTranProc!!.SetSingleData(0, 2, strEncPass)
        m_JangoTranProc!!.SetSingleData(0, 3, "N") //시간외 단일가여부
        m_JangoTranProc!!.SetSingleData(0, 4, "N") //오프라인 여부
        m_JangoTranProc!!.SetSingleData(0, 5, "01") //조회구분
        m_JangoTranProc!!.SetSingleData(0, 6, "01") //단가구분
        m_JangoTranProc!!.SetSingleData(0, 7, "N") //펀드결제분 포함여부
        m_JangoTranProc!!.SetSingleData(0, 8, "N") //융자금액자동상환여부
        m_JangoTranProc!!.SetSingleData(0, 9, "00") //처리구분
        m_JangoTranProc!!.SetSingleData(0, 10, " ") //연속조회검색조건
        m_JangoTranProc!!.SetSingleData(0, 11, " ") //연속조회키

        m_nJangoRqId = m_JangoTranProc!!.RequestData("satps")
    }

    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        if (m_nJangoRqId == nRqId) {

            //예수금 총금액
            val strTotal1 = m_JangoTranProc!!.GetMultiData(1, 0, 0)
            //총평가금액
            val strTotal2 = m_JangoTranProc!!.GetMultiData(1, 14, 0)
            //D-2 정산금액
            val strD2price = m_JangoTranProc!!.GetMultiData(1, 9, 0)

            val nCount = m_JangoTranProc!!.GetValidCount(0)


            for (i in 0 until nCount) {
                //종목
                val strCode = m_JangoTranProc!!.GetMultiData(0, 0, i)
                val strName = m_JangoTranProc!!.GetMultiData(0, 1, i)
                //잔고
                val strQty = m_JangoTranProc!!.GetMultiData(0, 7, i)
                var strAverPrice = m_JangoTranProc!!.GetMultiData(0, 9, i)


            }

            val dec = DecimalFormat("#,###")
            //tResultText.text = resultText
            //tTotalRaver.text = "레버리지 : " + leverTotal + " 인버스 : " + inverTotal
            //tStockData2.text = "" + (strTotal1.toInt() - strTotal2.toInt())

            tv_total_assets.text = dec.format(strTotal1.toInt())+"원"
            tv_total_profit_or_loss.text = dec.format(strTotal2.toInt())+"원"

            System.out.println("KospiEx 잔고조회 : " + strTotal1 + ", D-2정산금액 : " + strD2price)
        }
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

}