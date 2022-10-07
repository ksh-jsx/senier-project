package com.stucs17.stockai

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.fragment.app.Fragment
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.truefriend.corelib.shared.ItemMaster.ItemCode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements


class Tab2 : Fragment() {

    private lateinit var expertTranProc : ExpertTranProc
    private lateinit var buttonSearch : Button
    private lateinit var editStockName : EditText
    private lateinit var stockList : ListView
    private lateinit var infoBox : LinearLayout
    private lateinit var tv_kospi1 : TextView
    private lateinit var tv_kospi2 : TextView
    private lateinit var tv_kosdaq1 : TextView
    private lateinit var tv_kosdaq2 : TextView
    private lateinit var tv_news1 : TextView
    private lateinit var tv_news2 : TextView
    private lateinit var tv_news3 : TextView
    private lateinit var tv_news4 : TextView
    private lateinit var tv_news5 : TextView
    private lateinit var tv_news6 : TextView

    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록

    private var stockCode = ""

    val weburl = "https://finance.naver.com/"
    val TAG = "****** Tab2 ******"

    private val gb = GlobalBackground()
    lateinit var tabActivity: TabActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)

        tabActivity = context as TabActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v: View = inflater.inflate(R.layout.fragment_tab2, container, false)


        buttonSearch = v.findViewById(R.id.buttonSearch)
        editStockName = v.findViewById(R.id.editStockName)
        stockList =v.findViewById(R.id.stockList)
        infoBox = v.findViewById(R.id.infoBox)
        tv_kospi1 = v.findViewById(R.id.tv_kospi1)
        tv_kospi2 = v.findViewById(R.id.tv_kospi2)
        tv_kosdaq1 = v.findViewById(R.id.tv_kosdaq1)
        tv_kosdaq2 = v.findViewById(R.id.tv_kosdaq2)
        tv_news1 = v.findViewById(R.id.tv_news1)
        tv_news2 = v.findViewById(R.id.tv_news2)
        tv_news3 = v.findViewById(R.id.tv_news3)
        tv_news4 = v.findViewById(R.id.tv_news4)
        tv_news5 = v.findViewById(R.id.tv_news5)
        tv_news6 = v.findViewById(R.id.tv_news6)

        //Toast.makeText(this, start, Toast.LENGTH_SHORT).show()

        editStockName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                stockList.visibility = View.VISIBLE
                infoBox.visibility = View.GONE
                val textValue :String = editStockName.text.toString()
                val resultList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(textValue) }
                if (resultList.isNotEmpty())
                    makeStockList(resultList)
            }
        })

        buttonSearch.setOnClickListener {
            Log.d(TAG, "" +arrItemKospiCode)
            stockList.visibility = View.GONE
            infoBox.visibility = View.VISIBLE
            requestCurrentPrice(editStockName.text.toString())
        }

        Thread {
            while (!Thread.interrupted()) try {
                Thread.sleep(1000)
                tabActivity.runOnUiThread {
                    MyAsyncTask().execute(weburl)
                }
            } catch (e: InterruptedException) {
                // ooops
            }
        }.start()



        return v
    }


    @SuppressLint("StaticFieldLeak")
    inner class MyAsyncTask: AsyncTask<String, String, String>() { //input, progress update type, result type

        @SuppressLint("ResourceAsColor", "SetTextI18n")
        override fun doInBackground(vararg params: String?): String {

            val doc: Document = Jsoup.connect(weburl).get()
            val kospi: Elements = doc.select("#content > div.article > div.section2 > div.section_stock_market > div.section_stock > div.kospi_area.group_quot.quot_opn > div.heading_area > a > span span")
            val kosdaq: Elements = doc.select("#content > div.article > div.section2 > div.section_stock_market > div.section_stock > div.kosdaq_area.group_quot > div.heading_area > a > span span")
            val news: Elements = doc.select("#content > div.article > div.section > div.news_area > div.section_strategy > ul > li")
            val newsSize = news.size
            //Log.d(TAG, newsSize.toString())
            val listKospi = ArrayList<String>()
            val listKosdaq = ArrayList<String>()
            val listNewsText = ArrayList<String>()
            val listNewsHref = ArrayList<String>()

            kospi.forEachIndexed { index, elem ->
                val kospiText = elem.select("span").text()
                listKospi.add(kospiText)
            }
            kosdaq.forEachIndexed { index, elem ->
                val kosdaqText = elem.select("span").text()
                listKosdaq.add(kosdaqText)
            }
            news.forEachIndexed { index, elem ->
                val newsText = elem.select("li > span > a").text()
                val newsHref = elem.select("li > span > a").attr("href")
                listNewsText.add(newsText)
                listNewsHref.add(newsHref)
            }


            tv_kospi1.text = listKospi[0]
            tv_kospi2.text = "${listKospi[3]}${listKospi[1]}, ${listKospi[2].split("%")[0]}%"
            tv_kosdaq1.text = listKosdaq[0]
            tv_kosdaq2.text ="${listKosdaq[3]}${listKosdaq[1]}, ${listKosdaq[2].split("%")[0]}%"

            tv_news1.text = listNewsText[0]
            /*
            tv_news1.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://finance.naver.com${listNewsHref[0]}"))
                startActivity(intent)
            }*/
            tv_news2.text = listNewsText[1]

            tv_news3.text = listNewsText[2]

            tv_news4.text = listNewsText[3]

            tv_news5.text = listNewsText[4]

            tv_news6.text = listNewsText[5]

            if(listKospi[3] === "-")
                tv_kospi2.setTextColor(R.color.blue)
            else
                tv_kospi2.setTextColor(R.color.red)

            if(listKosdaq[3] === "-")
                tv_kosdaq2.setTextColor(R.color.blue)
            else
                tv_kosdaq2.setTextColor(R.color.red)
            return ""
        }
    }
    private fun makeStockList(sn: List<ItemCode>){ //검색한 주식 목록 생성
        val list: MutableList<String> = ArrayList()
        for(i in sn.indices) {
            list.add(sn[i].name)
        }

        val adpater = ArrayAdapter(tabActivity, R.layout.stock_list, list)
        stockList.adapter = adpater

        stockList.onItemClickListener = OnItemClickListener { adapterView, view, position, l ->
            val data = adapterView.getItemAtPosition(position) as String
            editStockName.setText(data)
            stockList.visibility = View.GONE
            infoBox.visibility = View.VISIBLE
        }

    }



    private fun requestCurrentPrice(stockName: String){ //주가 검색
        val resultList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(stockName) } //입력한 텍스트와 주식 목록 비교->필터링
        if (resultList.isNotEmpty()) {
            stockCode = resultList[0].code
            val intent = Intent(tabActivity, StockDetailActivity::class.java)
            Log.d(TAG, "code: $stockCode")
            intent.putExtra("stockCode",stockCode)
            intent.putExtra("stockName",stockName)
            startActivity(intent)
        }
        else{
            Toast.makeText(tabActivity.baseContext, "종목을 입력해주세요", Toast.LENGTH_SHORT).show()
        }
    }

}