package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.Public.Database
import com.stucs17.stockai.adapter.InterestingStockAdapter
import com.stucs17.stockai.data.InterestingStockData
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import com.truefriend.corelib.shared.ItemMaster.ItemCode
import kotlin.math.abs


class Tab3 : Fragment() {
    private lateinit var button_for_autoTrade : Button
    private lateinit var seekBar_for_risk : SeekBar
    private lateinit var textView_for_risk : TextView
    private lateinit var rv_myStock : RecyclerView
    private val datas = mutableListOf<InterestingStockData>()
    private var state = 0
    private var tradeLevel = 2
    lateinit var tabActivity: TabActivity
    private lateinit var interestingStockAdapter: InterestingStockAdapter
    private val db = Database()

    //sql 관련
    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    override fun onAttach(context: Context) {
        super.onAttach(context)

        tabActivity = context as TabActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val v: View = inflater.inflate(R.layout.fragment_tab3, container, false)
        button_for_autoTrade = v.findViewById(R.id.button_for_autoTrade)
        seekBar_for_risk = v.findViewById(R.id.seekBar_for_risk)
        textView_for_risk = v.findViewById(R.id.textView_for_risk)
        rv_myStock = v.findViewById(R.id.rv_myStock
        )
        val strArr = arrayOf<String>("매우 낮음","낮음","중간","높음","매우 높음")

        seekBar_for_risk.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{


            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView_for_risk.text = strArr[progress]
                db.updateTradeLevel(database,progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                textView_for_risk.text = strArr[seekBar!!.progress]
            }
        })

        dbHelper = DBHelper(tabActivity, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        val c = db.select(database)
        if(c!!.moveToNext()) {
            state = c.getString(c.getColumnIndex("autoTrade")).toInt()
            tradeLevel = c.getString(c.getColumnIndex("autoTradeLevel")).toInt()
        }
        textView_for_risk.text = strArr[tradeLevel]
        seekBar_for_risk.progress = tradeLevel
        if(state == 0) {
            button_for_autoTrade.text = "켜기"
        } else {
            button_for_autoTrade.text = "끄기"
        }

        //Log.d("tab3", state.toString())

        button_for_autoTrade.setOnClickListener{
            val c2 = db.select(database)!!
            if(c2.moveToNext()) {
                state = c2.getString(c2.getColumnIndex("autoTrade")).toInt()
            }
            if(state == 0) {
                db.update(database,1)
                button_for_autoTrade.text = "끄기"
            } else {
                db.update(database,0)
                button_for_autoTrade.text = "켜기"
            }
        }
        getAutoTradeStock()
        Thread {
            while (!Thread.interrupted()) try {
                Thread.sleep(10000)
                tabActivity.runOnUiThread {
                    getAutoTradeStock()
                }
            } catch (e: InterruptedException) {
                // ooops
            }
        }.start()

        return v
    }

    private fun getAutoTradeStock(){
        interestingStockAdapter = InterestingStockAdapter(tabActivity)
        rv_myStock.adapter = interestingStockAdapter

        val c = db.select_autoTradeTarget(database)

        val array = Array<InterestingStockData?>(c!!.count) { null }
        var arraySize = 0
        if(c.count<0) Log.d("tab3", "저장된 관심종목 없음")

        while(c.moveToNext()) {
            val stockCode = c.getString(c.getColumnIndex("code"))
            val stockName = c.getString(c.getColumnIndex("name"))

            val data = InterestingStockData(stockCode,stockName)
            array[arraySize] = data
            arraySize+=1
        }

        datas.clear()
        datas.apply {
            for (x in 0 until arraySize) {
                add(
                    InterestingStockData(stockCode=array[x]!!.stockCode, stockName=array[x]!!.stockName)
                )
            }

            interestingStockAdapter.datas = datas
            interestingStockAdapter.notifyDataSetChanged()
        }
    }

}