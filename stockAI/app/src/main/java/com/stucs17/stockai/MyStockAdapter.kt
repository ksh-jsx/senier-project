package com.stucs17.stockai

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class MyStockAdapter(private val context: Context) : RecyclerView.Adapter<MyStockAdapter.ViewHolder>() {

    var datas = mutableListOf<MyStockData>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.my_stock_list,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = datas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val stock_name: TextView = itemView.findViewById(R.id.stock_name)
        private val stock_profit_or_loss: TextView = itemView.findViewById(R.id.stock_profit_or_loss)
        private val stock_quantity: TextView = itemView.findViewById(R.id.stock_quantity)
        private val stock_price: TextView = itemView.findViewById(R.id.stock_price)

        val dec = DecimalFormat("#,###")

        fun bind(item: MyStockData) {
            stock_name.text = item.stockName

            stock_profit_or_loss.text = dec.format(item.stockProfit.toInt())+" ("+item.stockProfitPer+"%)"
            if(item.stockProfit.toInt()>=0)
                stock_profit_or_loss.setTextColor((ContextCompat.getColor(context!!, R.color.red)))
            else
                stock_profit_or_loss.setTextColor((ContextCompat.getColor(context!!, R.color.blue)))
            stock_quantity.text = item.stockQty+"주"
            stock_price.text = dec.format(item.stockPrice.toInt())

        }
    }


}