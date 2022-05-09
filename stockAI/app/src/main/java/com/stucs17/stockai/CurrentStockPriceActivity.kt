package com.stucs17.stockai

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import com.truefriend.corelib.shared.ItemMaster.ItemCode
import java.text.DecimalFormat
import kotlin.math.*


class CurrentStockPriceActivity : AppCompatActivity(), ITranDataListener {

    private var currentPriceRqId = 0
    private lateinit var expertTranProc : ExpertTranProc
    private lateinit var buttonSearch : Button
    private lateinit var editStockName : EditText
    private lateinit var stockList : ListView
    private lateinit var priceBox : LinearLayout
    private lateinit var BSbuttonBox : LinearLayout
    private lateinit var centerBox : LinearLayout
    private lateinit var nameView : TextView
    private lateinit var priceView : TextView
    private lateinit var priceView2 : TextView
    private lateinit var expectPercentView : TextView
    private lateinit var buttonForBuy : Button
    private lateinit var buttonForSell : Button

    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_stock_price)

        expertTranProc = ExpertTranProc(this)
        expertTranProc.InitInstance(this)
        expertTranProc.SetShowTrLog(true)

        buttonSearch = findViewById(R.id.buttonSearch)
        editStockName = findViewById(R.id.editStockName)
        stockList =findViewById(R.id.stockList)
        priceBox = findViewById(R.id.priceBox)
        BSbuttonBox = findViewById(R.id.BSbuttonBox)
        centerBox = findViewById(R.id.centerBox)
        priceView = findViewById(R.id.priceView) // 현재가
        priceView2 = findViewById(R.id.priceView2) // 변동치
        buttonForBuy = findViewById(R.id.buttonForBuy)
        buttonForSell = findViewById(R.id.buttonForSell)

        buttonForBuy.setOnClickListener {
            val intent = Intent(this@CurrentStockPriceActivity, BuyActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonForSell.setOnClickListener {
            val intent = Intent(this@CurrentStockPriceActivity, SellActivity::class.java)
            startActivity(intent)
            finish()
        }

        expectPercentView = findViewById(R.id.expectPercentView)
        val content = expectPercentView.getText().toString() //특정 글자만 색상 바꾸기
        val ssb = SpannableStringBuilder("75% 확률로 상승 예상되는 종목입니다!")
        ssb.apply{
            setSpan(ForegroundColorSpan(Color.RED), 8, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        }
        expectPercentView.text = ssb

//        Toast.makeText(this, start, Toast.LENGTH_SHORT).show()


        editTextWathcer()

        buttonSearch.setOnClickListener {
            requestCurrentPrice(editStockName.text.toString())
            priceBox.visibility = View.VISIBLE
            BSbuttonBox.visibility = View.VISIBLE
            centerBox.visibility = View.VISIBLE
        }

        editStockName.setOnClickListener(View.OnClickListener {
            stockList.visibility = View.VISIBLE
            priceBox.visibility = View.GONE
            BSbuttonBox.visibility = View.GONE
            centerBox.visibility = View.GONE
        })

    }



    private fun makeStockList(sn: List<ItemCode>){ //검색한 주식 목록 생성
        val list: MutableList<String> = ArrayList()
        for(i in sn.indices) {
            list.add(sn[i].name)
        }

        val adpater = ArrayAdapter<String>(this, R.layout.stock_list, list)
        stockList.adapter = adpater

        stockList.onItemClickListener = OnItemClickListener { adapterView, view, position, l ->
            val data = adapterView.getItemAtPosition(position) as String
            editStockName.setText(data)
            stockList.visibility = View.GONE

        }


    }

    private fun editTextWathcer(){ //editStockName onchangeEventListener

        editStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val textValue :String = editStockName.text.toString()

                val resultList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(textValue) }
                if (resultList.isNotEmpty())
                    makeStockList(resultList)
            }
        })
    }

    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        if (sTranID?.contains("scp") == true && currentPriceRqId == nRqId) {
            val currentPrice = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
            val dayChange = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비
            val stockName = expertTranProc.GetSingleData(0, 4) // 4 : 업종 한글 종목명
            var plus="";
            for(i in 0..50) {
                val info = expertTranProc.GetSingleData(0, i) // 11 : 주식 현재가
                Log.d(TAG, i.toString()+":"+info)
            }

            val variancePercent = (abs(dayChange.toDouble())/(currentPrice+dayChange*(-1)).toDouble())*100
            var setDecimal = Math.round(variancePercent*100)/100f

            val dec = DecimalFormat("#,###")

            if(dayChange>0) {
                priceBox.setBackgroundResource(R.drawable.radius_red)
                setDecimal = setDecimal
                plus = "+"
            }
            else {
                priceBox.setBackgroundResource(R.drawable.radius_blue)
            }

            dec.format(currentPrice)
            priceView.setText(dec.format(currentPrice)+"원")
            priceView2.setText(plus+""+dec.format(dayChange)+", "+"$setDecimal%")
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

    private fun requestCurrentPrice(stockName: String){ //주가 검색

        val resultList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(stockName) } //입력한 텍스트와 주식 목록 비교->필터링

        if (resultList.isNotEmpty()) {
            editStockName.setText(resultList[0].name)

            expertTranProc.ClearInblockData()
            expertTranProc.SetSingleData(0,0, "J") // J 는 주식
            expertTranProc.SetSingleData(0,1, resultList[0].code)
            currentPriceRqId = expertTranProc.RequestData("scp")
        }
        else{
            Toast.makeText(baseContext, "종목을 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }
}