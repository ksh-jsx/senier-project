package com.stucs17.stockai.Public

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
import com.stucs17.stockai.adapter.MyStockAdapter
import com.stucs17.stockai.data.MyStockData
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.ITranDataListener

class AccountInfo: AppCompatActivity(), ITranDataListener {
    private val auth = Auth()
    private val speechAPI = SpeechAPI()
    private var type : String? = ""
    //한투Api 관련
    private var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    private var m_nJangoRqId = -1 //잔고 TR ID
    //sql 관련
    private lateinit var dbHelper: DBHelper
    private lateinit var database: SQLiteDatabase

    val TAG = "****** AI ******"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        m_JangoTranProc = ExpertTranProc(this)
        m_JangoTranProc!!.InitInstance(this)
        m_JangoTranProc!!.SetShowTrLog(false)

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        m_nJangoRqId = getjangoInfo(database,m_JangoTranProc)
    }

    override fun onDestroy() {
        super.onDestroy()

        m_JangoTranProc!!.ClearInstance()
        m_JangoTranProc = null
    }

    fun getjangoInfo(database: SQLiteDatabase,m_JangoTranProc:ExpertTranProc?): Int {
        var strnumPwd = ""

        val c = auth.select(database)
        if (c != null) {
            if(c.moveToNext()){
                strnumPwd = c.getString(c.getColumnIndex("numPwd"))
            }
        }

        val strEncPass = m_JangoTranProc!!.GetEncryptPassword(strnumPwd)
        val strAcc = CommExpertMng.getInstance().GetAccountNo(0)

        m_JangoTranProc.ClearInblockData()
        m_JangoTranProc.SetSingleData(0, 0, strAcc) //계좌번호
        m_JangoTranProc.SetSingleData(0, 1, "01") //상품코드
        m_JangoTranProc.SetSingleData(0, 2, strEncPass)
        m_JangoTranProc.SetSingleData(0, 3, "N") //시간외 단일가여부
        m_JangoTranProc.SetSingleData(0, 4, "N") //오프라인 여부
        m_JangoTranProc.SetSingleData(0, 5, "01") //조회구분
        m_JangoTranProc.SetSingleData(0, 6, "01") //단가구분
        m_JangoTranProc.SetSingleData(0, 7, "N") //펀드결제분 포함여부
        m_JangoTranProc.SetSingleData(0, 8, "N") //융자금액자동상환여부
        m_JangoTranProc.SetSingleData(0, 9, "00") //처리구분
        m_JangoTranProc.SetSingleData(0, 10, " ") //연속조회검색조건
        m_JangoTranProc.SetSingleData(0, 11, " ") //연속조회키

        return m_JangoTranProc.RequestData("satps")
    }

    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
        }
        //총평가금액
        val strTotal1 = m_JangoTranProc!!.GetMultiData(1, 14, 0).toInt()
        //손익
        val strTotal2 = m_JangoTranProc!!.GetMultiData(1, 19, 0).toInt()
        //보유 주식 수
        val nCount = m_JangoTranProc!!.GetValidCount(0)

        val array = Array<MyStockData?>(nCount) { null }
        var buyPriceSum = 0

        for (i in 0 until nCount) {
            //종목
            val strCode = m_JangoTranProc!!.GetMultiData(0, 0, i)
            val strName = m_JangoTranProc!!.GetMultiData(0, 1, i)
            //잔고
            val strQty = m_JangoTranProc!!.GetMultiData(0, 7, i)
            val strbuyPrice = m_JangoTranProc!!.GetMultiData(0, 10, i) // 매입금액
            val strPrice = m_JangoTranProc!!.GetMultiData(0, 12, i) // 평가금액
            val strProfit = m_JangoTranProc!!.GetMultiData(0, 13, i) // 손익
            val strProfitPer = m_JangoTranProc!!.GetMultiData(0, 14, i) // 손익률

            if(strCode.length > 3){
                val data = MyStockData(i+1,strName,strProfit,strProfitPer,strQty,strPrice)
                array[i] = data
                buyPriceSum+=strbuyPrice.toInt()
            }

        }
        if (m_nJangoRqId == nRqId) {
            Log.d(TAG, "type: $type")
            if(type == "profit_or_loss"){
                //손익
                speechAPI.startUsingSpeechSDK2("당신의 수익률은 $strTotal2 원입니다.")
                Thread.sleep(3000)
                gotoTab()
            } else if(type == "total_assets"){
                //총자산
                speechAPI.startUsingSpeechSDK2("당신의 총 자산은 $strTotal1 원입니다.")
                Thread.sleep(3000)
                gotoTab()
            } else if(type == "available_to_order"){
                // 주문가능
                val result = (strTotal1-strTotal2)-buyPriceSum
                speechAPI.startUsingSpeechSDK2("당신의 주문 가능 금액은 $result 원입니다.")
                Thread.sleep(3000)
                gotoTab()
            }
        }
    }

    private fun gotoTab() {
        val intent = Intent(this@AccountInfo, TabActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onTranMessageReceived(
        nRqId: Int, strMsgCode: String?,
        strErrorType: String?, strMessage: String?
    ) {
        Log.e("onTranMessageReceived", String.format("MsgCode:%s ErrorType:%s %s",  strMsgCode ,  strErrorType  , strMessage));
    }

    override fun onTranTimeout(nRqId: Int) {
        Log.e("onTranTimeout", String.format("RqId:%d ", nRqId))
    }
}