package com.stucs17.stockai.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.commexpert.CommExpertMng
import com.stucs17.stockai.R
import com.stucs17.stockai.StockDetailActivity
import com.stucs17.stockai.data.NotSignedStockData
import java.text.DecimalFormat

class NotSignedStockAdapter(private val context: Context) : RecyclerView.Adapter<NotSignedStockAdapter.ViewHolder>() {

    var datas = mutableListOf<NotSignedStockData>()
    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.not_signed_stock_list,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val stock_name: TextView = itemView.findViewById(R.id.stock_name)
        private val stock_quantity: TextView = itemView.findViewById(R.id.stock_quantity)
        private val stock_price: TextView = itemView.findViewById(R.id.stock_price)

        val dec = DecimalFormat("#,###")

        @SuppressLint("SetTextI18n")
        fun bind(item: NotSignedStockData) {

            itemView.setOnClickListener{
                val stockCode = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(item.stockName) }[0].code //입력한 텍스트와 주식 목록 비교->필터링
                if (stockCode.isNotEmpty()) {
                    val intent = Intent(context, StockDetailActivity::class.java)
                    intent.putExtra("stockCode",stockCode)
                    intent.putExtra("stockName",item.stockName)
                    context.startActivity(intent)
                }
            }

            stock_name.text = item.stockName
            stock_quantity.text = item.stockQty.toString()+"주"
            stock_price.text = dec.format(item.orderPrice.toInt())

        }
    }


}