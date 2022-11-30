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
import com.stucs17.stockai.data.InterestingStockData
import com.stucs17.stockai.data.MyStockData
import com.stucs17.stockai.data.NotSignedStockData
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener

class AccountInfo: AppCompatActivity(), ITranDataListener, IRealDataListener {
    private val db = Database()
    private val speechAPI = SpeechAPI()
    private var type : String? = ""
    //한투Api 관련
    private var m_JangoTranProc: ExpertTranProc? = null //잔고 조회
    private var m_OrderListTranProc : ExpertTranProc? = null //주문
    private var m_nJangoRqId = -1 //잔고 TR ID
    var m_nOrderListRqId = -1 //주문내역 TR ID

    //sql 관련
    private lateinit var dbHelper: DBHelper
    private lateinit var database: SQLiteDatabase

    private val TAG = "****** AI ******"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speak)

        m_JangoTranProc = ExpertTranProc(this)
        m_JangoTranProc!!.InitInstance(this)
        m_JangoTranProc!!.SetShowTrLog(false)

        m_OrderListTranProc  = ExpertTranProc(this)
        m_OrderListTranProc !!.InitInstance(this)
        m_OrderListTranProc !!.SetShowTrLog(true)

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        if(intent.hasExtra("type")) {
            type = intent.getStringExtra("type")
        }
        when(type){
            "not_sign_stocks"->{
                m_nOrderListRqId = getNotSignedList(database,m_OrderListTranProc )
            }
            "interesting_stocks"->{
                val c = db.select_like(database)

                val array = Array<InterestingStockData?>(c!!.count) { null }
                var arraySize = 0
                if(c.count<0) speechAPI.startUsingSpeechSDK2("저장된 관심종목이 없습니다.")

                while(c.moveToNext()) {
                    val stockCode = c.getString(c.getColumnIndex("code"))
                    val stockName = c.getString(c.getColumnIndex("name"))

                    val data = InterestingStockData(stockCode, stockName)
                    array[arraySize] = data
                    arraySize += 1
                }

                val arrs = array.mapNotNull { it?.stockName }.joinToString(",","","")
                speechAPI.startUsingSpeechSDK2("당신이 관심 주식은 $arrs 입니다.")
            }
            else -> m_nJangoRqId = getJangoInfo(database,m_JangoTranProc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        m_JangoTranProc!!.ClearInstance()
        m_JangoTranProc = null

        m_OrderListTranProc!!.ClearInstance()
        m_OrderListTranProc = null
    }

    fun getJangoInfo(database: SQLiteDatabase,m_JangoTranProc:ExpertTranProc?): Int {
        var strnumPwd = ""

        val c = db.select(database)

        if(c!!.moveToNext()){
            strnumPwd = c.getString(c.getColumnIndex("numPwd"))
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

    fun getNotSignedList(database: SQLiteDatabase,m_OrderListTranProc :ExpertTranProc?): Int {
        var strnumPwd = ""

        val c = db.select(database)

        if(c!!.moveToNext()){
            strnumPwd = c.getString(c.getColumnIndex("numPwd"))
        }


        val strEncPass = m_OrderListTranProc !!.GetEncryptPassword(strnumPwd) //비밀번호
        val strAcc = CommExpertMng.getInstance().GetAccountNo(0)

        m_OrderListTranProc .ClearInblockData()
        m_OrderListTranProc .SetSingleData(0, 0, strAcc) //계좌
        m_OrderListTranProc .SetSingleData(0, 1, "01") //상품코드
        m_OrderListTranProc .SetSingleData(0, 2, strEncPass) //비밀번호
        m_OrderListTranProc .SetSingleData(0, 3, " ") //연속조회검색조건100
        m_OrderListTranProc .SetSingleData(0, 4, " ") //연속조회키100
        m_OrderListTranProc .SetSingleData(0, 5, "0") //조회구분 0주문순, 1종목순

        return m_OrderListTranProc .RequestData("smcp")  //주식 정정 취소 가능 주문 조회
    }

    override fun onTranDataReceived(sTranID: String, nRqId: Int) {
        if (m_nJangoRqId == nRqId) {

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

                if(strCode.length > 3 && strQty.toInt() > 0){
                    val data = MyStockData(i+1,strName,strProfit,strProfitPer,strQty,strPrice)
                    array[i] = data
                    buyPriceSum+=strbuyPrice.toInt()
                }

            }

            Log.d(TAG, "type: $type")
            when(type){
                "total_info"->{ //손익
                    speechAPI.startUsingSpeechSDK2("현재 계좌와 보유주식 현황입니다." +
                            "총 자산은 $strTotal1 원이고 수익률은 $strTotal2 원입니다" +
                            "이 중 주문가능 금액은 ${(strTotal1-strTotal2)-buyPriceSum}원이에요")
                }
                "total_assets"->{ //총자산
                    speechAPI.startUsingSpeechSDK2("당신의 총 자산은 $strTotal1 원이고 수익률은 $strTotal2 원입니다.")
                }
                "available_to_order"->{ // 주문가능
                    speechAPI.startUsingSpeechSDK2("당신의 주문 가능 금액은 ${(strTotal1-strTotal2)-buyPriceSum} 원입니다.")
                }
                "my_stocks"->{ //매입 주식 목록
                    val arrs = array.mapNotNull { it?.stockName }.joinToString(",","","")
                    speechAPI.startUsingSpeechSDK2("당신이 매입한 주식은 $arrs 입니다.")
                    val names = array.mapNotNull { it?.stockName }
                    val qtys = array.mapNotNull { it?.stockQty }
                    val profits = array.mapNotNull { it?.stockProfit }
                    val pers = array.mapNotNull { it?.stockProfitPer }
                    Thread.sleep(5000)
                    for(i in 0 until nCount){
                        if(qtys[i].toInt() > 0) {
                            speechAPI.startUsingSpeechSDK2("${names[i]} ${qtys[i]} 주는 총 ${pers[i]} %이고 손익은 ${profits[i]}원 입니다")
                            Thread.sleep(8000)
                        }
                    }

                }
                "total_order_price"->{
                    speechAPI.startUsingSpeechSDK2("당신의 총 매입가는 $buyPriceSum 원 입니다.")
                }
            }
            gotoTab1()
        } else if(m_nOrderListRqId == nRqId) { //취소 대상 주문 리스트

            val nCount: Int = m_OrderListTranProc!!.GetValidCount(0)
            val array = Array<NotSignedStockData?>(nCount) { null }

            for (i in 0 until nCount) {
                val strNo = m_OrderListTranProc!!.GetMultiData(0, 0, i) //주문채번지점번호
                val strOrderNumber = m_OrderListTranProc!!.GetMultiData(0, 1, i) //주문번호
                val strOrderNumberOri = m_OrderListTranProc!!.GetMultiData(0, 2, i) //원주문번호
                val strCode = m_OrderListTranProc!!.GetMultiData(0, 4, i) //상품번호
                val strName = m_OrderListTranProc!!.GetMultiData(0, 5, i) //상품명
                val nOrderCount = m_OrderListTranProc!!.GetMultiData(0, 7, i).toInt() //주문수량
                val nOrderPrice = m_OrderListTranProc!!.GetMultiData(0, 8, i).toInt() //주문단가
                val tradeType = m_OrderListTranProc!!.GetMultiData(0, 13, i)

                if (strOrderNumber.isEmpty()) continue
                else{
                    val data = NotSignedStockData(i+1,strName,nOrderCount,(nOrderPrice*nOrderCount),tradeType,strOrderNumberOri)
                    array[i] = data
                }
                Log.d(TAG,
                    "KospiEx : 주문채번지점번호 - $strNo 원주문번호 - $strOrderNumberOri 주문번호 - $strOrderNumber"
                )
                Log.d(TAG, "KospiEx : 상품번호 - $strCode 주문수량 - $nOrderCount 주문단가 - $nOrderPrice")
            }

            val arrs = array.mapNotNull{ it?.stockName }.joinToString(",","","")
            when(type){
                "not_sign_stocks"->{ //미체결
                    if(arrs.isNotEmpty()) speechAPI.startUsingSpeechSDK2("현재 미체결 주식은 $arrs 입니다")
                    else speechAPI.startUsingSpeechSDK2("현재 미체결 주식은 없습니다")
                }
            }
            Thread.sleep(1000)
            gotoTab1()

        }
    }

    private fun gotoTab1() {
        val intent = Intent(this@AccountInfo, TabActivity::class.java)
        intent.putExtra("tab", 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
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

    override fun onRealDataReceived(p0: String?) {
        Log.d(TAG, "type: $type")
    }
}