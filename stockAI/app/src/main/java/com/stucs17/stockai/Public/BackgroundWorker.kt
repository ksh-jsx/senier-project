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
import com.stucs17.stockai.GlobalBackground
import com.stucs17.stockai.MainActivity
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
import com.stucs17.stockai.data.InterestingStockData
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IRealDataListener
import com.truefriend.corelib.commexpert.intrf.ITranDataListener
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

class BackgroundWorker: BroadcastReceiver(), ITranDataListener, IRealDataListener {

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
    var m_nOrderRqId = -1 //주문 TR ID
    var m_OrderRealProc: ExpertRealProc? = null

    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    private val auth = Auth()
    private val stockInfo = StockIndex()
    private val trade = Trade()

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent != null){
            if (context != null) {
                ctt = context
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            expertTranProc = ExpertTranProc(context)
            expertTranProc.InitInstance(this)
            expertTranProc.SetShowTrLog(true)

            dbHelper = DBHelper(context, "mydb.db", null, 1)
            database = dbHelper.writableDatabase

            c = auth.select_like(database)!!
            if(c.moveToNext()){
                target = c.getString(c.getColumnIndex("code"))
                currentPriceRqId = stockInfo.getStockInfo(expertTranProc,target)
                cnt++
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun displayNotification(cnt:Int,context :Context?,name:String,per:Float,sign:Int) {
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
        val word = if(sign == 1) "상승" else "하락"
        Log.d("test", CHANNEL_ID+cnt.toString())
        val builder = context?.let {
            NotificationCompat.Builder(it, CHANNEL_ID+cnt.toString())
                .setSmallIcon(R.drawable.logo) // 아이콘
                .setContentTitle("로보스톡") // 제목
                .setContentText("관심종목 ${name}: $per% $word 중입니다.") // 내용
                .setContentIntent(contentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        }

        if (builder != null) {
            notificationManager.notify(0, builder.build())
        }
    }

    fun displayNotification2(cnt:Int,context :Context?,name:String,strOrderGubun:String) {
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

        val info11 = expertTranProc.GetSingleData(0, 11).toInt() // 11 : 주식 현재가
        val info12 = expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비
        val info13= expertTranProc.GetSingleData(0, 12).toInt() // 12 : 전일 대비 부호 0:보합, 1:상승, 2:상한

        val inList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.code.startsWith(target) }[0].name //입력한 텍스트와 주식 목록 비교->필터링

        val variancePercent = (abs(info12.toDouble()) /(info11+info12*(-1)).toDouble())*100
        val setDecimal = (variancePercent * 100).roundToInt() /100f
        if(setDecimal>5){ //변동폭이 5% 이상이면 알림
            createNotificationChannel(cnt)
            displayNotification(cnt,ctt,inList,setDecimal,info13)
        }

        if(info11<900){
            m_nOrderRqId = trade.runBuy(database,target,"00","1","")!!
        }

        if(c.moveToNext()) {
            Thread.sleep(5000)
            target = c.getString(c.getColumnIndex("code"))
            currentPriceRqId = stockInfo.getStockInfo(expertTranProc,target)
            cnt++
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
        if (strServiceId === "scn_r" || strServiceId === "scn_m") {
            val strOrderNumber = m_OrderRealProc!!.GetRealData(0, 2) //주문번호
            val strOrderGubun = m_OrderRealProc!!.GetRealData(0, 4) //매도매수구분
            val strCode = m_OrderRealProc!!.GetRealData(0, 8) //종목코드
            val inList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.code.startsWith(strCode) }[0].name //입력한 텍스트와 주식 목록 비교->필터링
            Log.d(
                "==주식 체결통보==",
                String.format("주문번호:%s 매도매수구분:%s 종목코드:%s", strOrderNumber, strOrderGubun, strCode)
            )
            createNotificationChannel(cnt)
            displayNotification2(cnt,ctt,inList,strOrderGubun)
        }
    }
}
