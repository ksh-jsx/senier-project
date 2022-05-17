package com.stucs17.stockai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.adapter.MyStockAdapter
import com.stucs17.stockai.data.MyStockData
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.text.DecimalFormat

class HomeActivity : AppCompatActivity(), ITranDataListener {

    var m_JangoTranProc: ExpertTranProc? = null //잔고 조회

    var m_nJangoRqId = -1 //잔고 TR ID

    private lateinit var tv_total_assets : TextView
    private lateinit var tv_rest_assets : TextView
    private lateinit var tv_total_profit_or_loss : TextView
    private lateinit var btn_goto_CSActivity : Button
    private lateinit var btn_goto_ISActivity : Button
    private lateinit var rv_myStock : RecyclerView

    lateinit var myStockAdapter: MyStockAdapter
    val datas = mutableListOf<MyStockData>()
    private val gb = GlobalBackground()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        m_JangoTranProc = ExpertTranProc(this@HomeActivity)
        m_JangoTranProc!!.InitInstance(this@HomeActivity)
        m_JangoTranProc!!.SetShowTrLog(false)

        tv_total_assets = findViewById(R.id.tv_total_assets)
        tv_rest_assets = findViewById(R.id.tv_rest_assets)
        tv_total_profit_or_loss = findViewById(R.id.tv_total_profit_or_loss)
        btn_goto_CSActivity = findViewById(R.id.btn_goto_CSActivity)
        btn_goto_ISActivity = findViewById(R.id.btn_goto_ISActivity)
        rv_myStock = findViewById(R.id.rv_myStock)

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

            //총평가금액
            val strTotal1 = m_JangoTranProc!!.GetMultiData(1, 14, 0)
            //손익
            val strTotal2 = m_JangoTranProc!!.GetMultiData(1, 19, 0)


            val profit = (strTotal2.toInt()-strTotal1.toInt())

            val nCount = m_JangoTranProc!!.GetValidCount(0)

            val array = Array<MyStockData?>(nCount) { null }
            var arraySize = 0
            var buyPriceSum = 0

            myStockAdapter = MyStockAdapter(this)
            rv_myStock.adapter = myStockAdapter

            for (i in 0 until nCount) {
                //종목
                val strCode = m_JangoTranProc!!.GetMultiData(0, 0, i)
                val strName = m_JangoTranProc!!.GetMultiData(0, 1, i)
                //잔고
                val strQty = m_JangoTranProc!!.GetMultiData(0, 7, i)
                var strbuyPrice = m_JangoTranProc!!.GetMultiData(0, 10, i) // 매입금액
                var strPrice = m_JangoTranProc!!.GetMultiData(0, 12, i) // 평가금액
                var strProfit = m_JangoTranProc!!.GetMultiData(0, 13, i) // 손익
                var strProfitPer = m_JangoTranProc!!.GetMultiData(0, 14, i) // 손익률

                if(strCode.length > 3){
                    val data = MyStockData(i+1,strName,strProfit,strProfitPer,strQty,strPrice)
                    array[i] = data
                    arraySize+=1
                    buyPriceSum+=strbuyPrice.toInt()
                }

                //System.out.println("1: " + strCode + ", 2: " + strName)
                //System.out.println("3: " + strQty + ", 4: " + strAverPrice)
            }

            tv_total_assets.text = gb.dec(strTotal1.toInt())+"원"
            tv_total_profit_or_loss.text = gb.dec(strTotal2.toInt())+"원"
            tv_rest_assets.text = "주문 가능: "+gb.dec((strTotal1.toInt()-strTotal2.toInt())-buyPriceSum)+"원"

            if(profit > 0)
                tv_total_profit_or_loss.setTextColor((ContextCompat.getColor(applicationContext!!, R.color.red)))
            else if(profit === 0)
                tv_total_profit_or_loss.setTextColor((ContextCompat.getColor(applicationContext!!, R.color.gray)))
            else
                tv_total_profit_or_loss.setTextColor((ContextCompat.getColor(applicationContext!!, R.color.blue)))

            datas.apply {

                for (x in 0 until arraySize) {

                    add(
                        MyStockData(
                        id=array[x]!!.id,
                        stockName=array[x]!!.stockName,
                        stockProfit=array[x]!!.stockProfit,
                        stockProfitPer=array[x]!!.stockProfitPer,
                        stockQty=array[x]!!.stockQty,
                        stockPrice=array[x]!!.stockPrice)
                    )
                }

                myStockAdapter.datas = datas
                myStockAdapter.notifyDataSetChanged()
            }

            //tResultText.text = resultText
            //tTotalRaver.text = "레버리지 : " + leverTotal + " 인버스 : " + inverTotal
            //tStockData2.text = "" + (strTotal1.toInt() - strTotal2.toInt())



            //System.out.println("KospiEx 잔고조회 : " + strTotal1 + ", D-2정산금액 : " + strD2price)
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