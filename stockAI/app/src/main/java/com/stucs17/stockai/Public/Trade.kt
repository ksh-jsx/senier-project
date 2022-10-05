package com.stucs17.stockai.Public

import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.commexpert.CommExpertMng
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.R
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener

class Trade : AppCompatActivity(), ITranDataListener, IRealDataListener {

    var m_nJangoRqId = -1 //잔고 TR ID
    var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    var m_nOrderRqId = -1 //주문 TR ID
    var m_OrderTranProc: ExpertTranProc? = null //주문
    var m_OrderRealProc: ExpertRealProc? = null

    private var type : String = ""
    private var target : String = ""
    private var code : String = ""
    private var sellableQty : Int = 0

    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록

    private val info = AccountInfo()
    private val auth = Auth()
    private val speechAPI = SpeechAPI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        m_OrderTranProc = ExpertTranProc(this)
        m_OrderTranProc!!.InitInstance(this)
        m_OrderTranProc!!.SetShowTrLog(true)

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")!!
            target = intent.getStringExtra("target")!!
            code = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(target) }[0].code //입력한 텍스트와 주식 목록 비교->필터링
        }

        when(type){
            "buy"->{
                m_nOrderRqId = runBuy(database,code,"00","1","")!!

            }
            "sell"->{
                m_nJangoRqId = info.getJangoInfo(database,m_JangoTranProc)
                m_nOrderRqId = runSell(database,code,"00","1","")!!
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        m_OrderTranProc!!.ClearInstance()
        m_OrderTranProc = null
    }

    private fun setTrade(database: SQLiteDatabase,currentCode:String){
        var strnumPwd = ""

        val c = auth.select(database)
        if (c != null) {
            if(c.moveToNext()){
                strnumPwd = c.getString(c.getColumnIndex("numPwd"))
            }
        }

        val strEncPass = m_OrderTranProc!!.GetEncryptPassword(strnumPwd) //비밀번호
        val strAcc = CommExpertMng.getInstance().GetAccountNo(0)

        m_OrderTranProc?.ClearInblockData()
        m_OrderTranProc?.SetSingleData(0, 0, strAcc) //계좌
        m_OrderTranProc?.SetSingleData(0, 1, "01") //상품코드
        m_OrderTranProc?.SetSingleData(0, 2, strEncPass) //비밀번호
        m_OrderTranProc?.SetSingleData(0, 3, currentCode) //상품코드
    }

    fun runBuy(database: SQLiteDatabase,currentCode:String,orderType:String,currentQty:String,currentPrice:String): Int? {

        setTrade(database,currentCode)
        m_OrderTranProc?.SetSingleData(0, 4, orderType) //주문구분  00:지정가 01:시장가
        m_OrderTranProc?.SetSingleData(0, 5, currentQty)//주문수량
        m_OrderTranProc?.SetSingleData(0, 6, currentPrice)//주문단가
        m_OrderTranProc?.SetSingleData(0, 7, " ") //연락전화번호
        m_OrderTranProc?.SetCertType(1) //축약서명

        return m_OrderTranProc?.RequestData("scabo") //매수주문
    }

    fun runSell(database: SQLiteDatabase,currentCode:String,orderType:String,currentQty:String,currentPrice:String ): Int? {

        setTrade(database,currentCode)
        m_OrderTranProc?.SetSingleData(0, 4, "01") //01
        m_OrderTranProc?.SetSingleData(0, 5, orderType) //주문구분  00:지정가 01:시장가
        m_OrderTranProc?.SetSingleData(0, 6, currentQty) //주문수량
        m_OrderTranProc?.SetSingleData(0, 7, currentPrice) //주문단가
        m_OrderTranProc?.SetSingleData(0, 8, " ") //연락전화번호
        m_OrderTranProc?.SetCertType(1) //축약서명

        return m_OrderTranProc?.RequestData("scaao") //매도주문
    }

    @SuppressLint("SetTextI18n")
    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        if (m_nJangoRqId == nRqId) {
            val nCount = m_JangoTranProc!!.GetValidCount(0)
            for (i in 0 until nCount) {
                val strName = m_JangoTranProc!!.GetMultiData(0, 1, i)

                if (strName == target) {
                    sellableQty = m_JangoTranProc!!.GetMultiData(0, 7, i).toInt()//수량
                }
                //System.out.println("1: " + strCode + ", 2: " + strName)
                //System.out.println("3: " + strQty + ", 4: " + strAverPrice)
            }
        }
    }

    override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
        TODO("Not yet implemented")
    }

    override fun onTranTimeout(p0: Int) {
        TODO("Not yet implemented")
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
            speechAPI.startUsingSpeechSDK2("거래되었습니다.")
        }
    }
}