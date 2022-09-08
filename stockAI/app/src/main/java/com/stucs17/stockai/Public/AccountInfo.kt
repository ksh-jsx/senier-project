package com.stucs17.stockai.Public

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.CommExpertMng
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.data.MyStockData

class AccountInfo: AppCompatActivity() {
    private val auth = Auth()
    private val datas = mutableListOf<MyStockData>()

    fun getjango(database:SQLiteDatabase,m_JangoTranProc:ExpertTranProc?): Int {
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
        //if (tStatus == null) return
        m_JangoTranProc.SetSingleData(0, 0, strAcc)
        //상품코드
        m_JangoTranProc.SetSingleData(0, 1, "01")

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
}