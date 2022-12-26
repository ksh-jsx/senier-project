package com.stucs17.stockai.Public

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.commexpert.CommExpertMng
import com.commexpert.ExpertRealProc
import com.commexpert.ExpertTranProc
import com.stucs17.stockai.MainActivity
import com.stucs17.stockai.R
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

class BackgroundWorker_autoTrade: BroadcastReceiver(), ITranDataListener, IRealDataListener {

    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "roboStock"
    private lateinit var ctt : Context
    private var target =  ""
    private var cnt =  0
    private lateinit var c: Cursor
    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록
    private lateinit var expertTranProc : ExpertTranProc
    private var currentPriceRqId = 0
    var m_timeId = -1 //주문가능시간 TR ID
    var m_nOrderRqId = -1 //주문 TR ID
    var m_OrderRealProc: ExpertRealProc? = null
    var m_OrderTranProc: ExpertTranProc? = null //주문

    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    private val db = Database()
    private val stockInfo = StockIndex()
    private val trade = Trade()
    private val speechAPI = SpeechAPI()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?) {
        val dateNow = LocalDate.now()
        val dayOfWeek = dateNow.dayOfWeek.toString()

        if(intent != null && dayOfWeek != "SATURDAY" && dayOfWeek != "SUNDAY" ){
            if (context != null) {
                ctt = context
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            expertTranProc = ExpertTranProc(context)
            expertTranProc.InitInstance(this)
            expertTranProc.SetShowTrLog(true)

            m_OrderTranProc = ExpertTranProc(context)
            m_OrderTranProc!!.InitInstance(this)
            m_OrderTranProc!!.SetShowTrLog(true)

            dbHelper = DBHelper(context, "mydb.db", null, 1)
            database = dbHelper.writableDatabase

            val user = db.select(database)!!
            user.moveToNext()
            val isAuto = user.getString(user.getColumnIndex("autoTrade")).toInt()

            if(isAuto == 1){
                c = db.select_autoTradeTarget(database)!!
                if(c.moveToNext()){
                    target = c.getString(c.getColumnIndex("code"))
                    Log.d("target", target)
                    currentPriceRqId = stockInfo.getStockInfo(expertTranProc,target)
                    cnt++
                }
            }


        }
    }

    private fun displayNotification(cnt:Int, context :Context?, name:String, strOrderGubun:String) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0, // requestCode
            contentIntent, // 알림 클릭 시 이동할 인텐트
            PendingIntent.FLAG_UPDATE_CURRENT
            /*
            1. FLAG_UPDATE_CURRENT : 현재 PendingIntent를 유지하고, 대신 인텐트의 extra data는 새로 전달된 Intent로 교체
            2. FLAG_CANCEL_CURRENT : 현재 인텐트가 이미 등록되어있다면 삭제, 다시 등록
            3. FLAG_NO_CREATE : 이미 등록된 인텐트가 있다면, null
            4. FLAG_ONE_SHOT : 한번 사용되면, 그 다음에 다시 사용하지 않음
             */
        )
        val builder = context?.let {
            NotificationCompat.Builder(it, CHANNEL_ID+cnt.toString())
                .setSmallIcon(R.drawable.logo) // 아이콘
                .setContentTitle("로보스톡") // 제목
                .setContentText("$name - $strOrderGubun 되었습니다.") // 내용
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        if (builder != null) {
            notificationManager.notify(0, builder.build())
        }
    }

    private fun createNotificationChannel(cnt:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID+cnt.toString(), // 채널의 아이디
                "RoboStock$cnt", // 채널의 이름
                NotificationManager.IMPORTANCE_HIGH
                /*
                1. IMPORTANCE_HIGH = 알림음이 울리고 헤드업 알림으로 표시
                2. IMPORTANCE_DEFAULT = 알림음 울림
                3. IMPORTANCE_LOW = 알림음 없음
                4. IMPORTANCE_MIN = 알림음 없고 상태줄 표시 X
                 */
            )
            notificationChannel.enableLights(true) // 불빛
            notificationChannel.lightColor = Color.RED // 색상
            notificationChannel.enableVibration(true) // 진동 여부
            notificationChannel.description = "채널의 상세정보입니다." // 채널 정보
            notificationManager.createNotificationChannel(
                notificationChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTranDataReceived(sTranID: String?, nRqId: Int) {
        if(sTranID!! === "scp") {
            val target_decision = "buy"
            val target_qty = "1"

            if (target_decision === "buy"){
                m_nOrderRqId = trade.runBuy(m_OrderTranProc, database, target, "01", target_qty, "")!!
                speechAPI.startUsingSpeechSDK2("자동 매수가 체결되었습니다")
                val inList = (arrItemKospiCode + arrItemKosdaqCode).sorted()
                    .filter { it.code.startsWith(target) }[0].name //입력한 텍스트와 주식 목록 비교->필터링
                createNotificationChannel(cnt)
                displayNotification(cnt, ctt, inList, "매수")
            } else if (target_decision === "sell") {
                m_nOrderRqId = trade.runSell(m_OrderTranProc, database, target, "01", target_qty, "")!!
                speechAPI.startUsingSpeechSDK2("자동 매도가 체결되었습니다")
                val inList = (arrItemKospiCode + arrItemKosdaqCode).sorted()
                    .filter { it.code.startsWith(target) }[0].name //입력한 텍스트와 주식 목록 비교->필터링
                createNotificationChannel(cnt)
                displayNotification(cnt, ctt, inList, "매도")
            }
            if (c.moveToNext()) {

                Thread.sleep(5000)
                target = c.getString(c.getColumnIndex("code"))

                currentPriceRqId = stockInfo.getStockInfo(expertTranProc, target)
                cnt++
            }
        }
    }


    override fun onTranMessageReceived(p0: Int, p1: String?, p2: String?, p3: String?) {
        Log.d("onTranMessageReceived", "$p0/$p1")
    }

    override fun onTranTimeout(p0: Int) {
        Log.d("onTranTimeout", "$p0")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRealDataReceived(strServiceId: String) {
        Log.d("test1", "test1")
        if (strServiceId === "scn_r" || strServiceId === "scn_m") {
            Log.d("test2", "test2")
            val strOrderNumber = m_OrderRealProc!!.GetRealData(0, 2) //주문번호
            val strOrderGubun = m_OrderRealProc!!.GetRealData(0, 4) //매도매수구분
            val strCode = m_OrderRealProc!!.GetRealData(0, 8) //종목코드
            val inList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.code.startsWith(strCode) }[0].name //입력한 텍스트와 주식 목록 비교->필터링
            Log.d(
                "==주식 체결통보==",
                String.format("주문번호:%s 매도매수구분:%s 종목코드:%s", strOrderNumber, strOrderGubun, strCode)
            )

        }
    }
}
