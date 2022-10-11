package com.stucs17.stockai.adapter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.Public.Trade
import com.stucs17.stockai.R
import com.stucs17.stockai.StockDetailActivity
import com.stucs17.stockai.data.NotSignedStockData
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.text.DecimalFormat

class NotSignedStockAdapter(private val context: Context) : RecyclerView.Adapter<NotSignedStockAdapter.ViewHolder>() {

    var datas = mutableListOf<NotSignedStockData>()
    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록

    var m_nOrderRqId = -1 //주문 TR ID
    var m_OrderTranProc: ExpertTranProc? = null //주문

    lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    private val trade = Trade()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.not_signed_stock_list,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), ITranDataListener {

        private val stock_name: TextView = itemView.findViewById(R.id.stock_name)
        private val stock_quantity: TextView = itemView.findViewById(R.id.stock_quantity)
        private val stock_price: TextView = itemView.findViewById(R.id.stock_price)
        private val btn_cancelOrder: TextView = itemView.findViewById(R.id.btn_cancelOrder)


        val dec = DecimalFormat("#,###")

        @SuppressLint("SetTextI18n")
        fun bind(item: NotSignedStockData) {

            dbHelper = DBHelper(context, "mydb.db", null, 1)
            database = dbHelper.writableDatabase

            m_OrderTranProc = ExpertTranProc(context)
            m_OrderTranProc!!.InitInstance(this)
            m_OrderTranProc!!.SetShowTrLog(true)

            itemView.setOnClickListener{
                val stockCode = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(item.stockName) }[0].code //입력한 텍스트와 주식 목록 비교->필터링
                if (stockCode.isNotEmpty()) {
                    val intent = Intent(context, StockDetailActivity::class.java)
                    intent.putExtra("stockCode",stockCode)
                    intent.putExtra("stockName",item.stockName)
                    context.startActivity(intent)
                }
            }

            btn_cancelOrder.setOnClickListener{
                m_nOrderRqId = trade.runCancel(m_OrderTranProc,database,item.strOrderNumberOri)!!
            }

            val type = if (item.tradeType == "01") "매도" else "매수"
            stock_name.text = item.stockName
            stock_quantity.text = item.stockQty.toString()+"주"
            stock_price.text = type+": "+dec.format(item.orderPrice)

        }

        override fun onTranDataReceived(sTranID: String, nRqId: Int) {
            if(m_nOrderRqId == nRqId){

                val orderNumberKET = m_OrderTranProc!!.GetSingleData(0,0) //한국거래소전송주문조직번호
                val orderNumber = m_OrderTranProc!!.GetSingleData(0,1) //주문번호
                val orderTime = m_OrderTranProc!!.GetSingleData(0,2) //주문시각
                Log.d("주문 접수","$orderNumberKET / $orderNumber / $orderTime")

            }
        }

        override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
            TODO("Not yet implemented")
        }

        override fun onTranTimeout(p0: Int) {
            TODO("Not yet implemented")
        }
    }


}