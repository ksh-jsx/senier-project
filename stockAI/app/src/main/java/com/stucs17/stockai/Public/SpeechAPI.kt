package com.stucs17.stockai.Public

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.commexpert.CommExpertMng
import com.kakao.sdk.newtoneapi.*
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
import com.stucs17.stockai.sql.DBHelper
import com.stucs17.stockai.sql.HttpRequestHelper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext


class SpeechAPI : AppCompatActivity(), CoroutineScope {

    private val RECORD_REQUEST_CODE = 1000
    private val STORAGE_REQUEST_CODE = 1000
    private val NETWORK_STATE_CODE = 0

    private lateinit var test_tv : TextView
    private var isFirst = true

    private val arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList() // 코스피 주식 목록
    private val arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList() // 코스닥 주식 목록

    //sql 관련
    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase
    private val db = Database()

    private lateinit var mAudioManager: AudioManager

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        test_tv = findViewById(R.id.test_tv)

        job = Job()

        setupPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Activity종료시 job이 종료되도록 한다.
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when(keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                startUsingSpeechSDK()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun setupPermissions(){
        val permission_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val permission_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permission_network = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        if(permission_audio != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
        } else if(permission_storage != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
        } else if(permission_network != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "Permission to recode denied")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), NETWORK_STATE_CODE)
        }
        else {
            //본문실행
            SpeechRecognizerManager.getInstance().initializeLibrary(this)
            TextToSpeechManager.getInstance().initializeLibrary(this)
            startUsingSpeechSDK()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }

            STORAGE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
            NETWORK_STATE_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun startUsingSpeechSDK(isOutside:Boolean = false, intent: Intent? = null,type: String? =null){
        Toast.makeText(this, "말하세요", Toast.LENGTH_SHORT).show()

        test_tv.text = ""
        //클라이언트 생성
        val builder = SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
        val client = builder.build()

        //Callback
        client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
            //콜백함수들
            override fun onReady() {
                Log.d(TAG, "모든 하드웨어 및 오디오 서비스가 준비되었습니다.")
                volumeUp()
                if(isFirst) {
                    startUsingSpeechSDK2("네!")
                    isFirst = false
                }
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "사용자가 말을 하기 시작했습니다.")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "사용자의 말하기가 끝이 났습니다. 데이터를 서버로 전달합니다.")
            }

            override fun onPartialResult(partialResult: String?) {
                //현재 인식된 음성테이터 문자열을 출력해 준다. 여러번 호출됨. 필요에 따라 사용하면 됨.
                //Log.d(TAG, "현재까지 인식된 문자열:" + partialResult)
            }

            /*
            최종결과 - 음성입력이 종료 혹은 stopRecording()이 호출되고 서버에 질의가 완료되고 나서 호출됨
            Bundle에 ArrayList로 값을 받음. 신뢰도가 높음 것 부터...
             */
            override fun onResults(results: Bundle?) {
                val texts = results?.getStringArrayList(SpeechRecognizerClient.KEY_RECOGNITION_RESULTS)

                runOnUiThread {
                    volumeUp()
                    val txt = texts?.get(0)
                    //정확도가 높은 첫번째 결과값을 텍스트뷰에 출력
                    test_tv.text = txt
                    if(isOutside){
                        nextStep2(txt,intent,type)
                    } else {
                        nextStep(txt)
                    }
                }
            }

            override fun onAudioLevel(audioLevel: Float) {
                //Log.d(TAG, "Audio Level(0~1): " + audioLevel.toString())
            }
            override fun onError(errorCode: Int, errorMsg: String?) {
                //에러 출력 해 봄
                Log.d(TAG, "Error: $errorMsg")
            }
            override fun onFinished() {
            }
        })

        //음성인식 시작함
        client.startRecording(true)
    }

    fun startUsingSpeechSDK2(txt : String?) {
        var ttsClient : TextToSpeechClient? = null
        //TTS 클라이언트 생성
        ttsClient = TextToSpeechClient.Builder()
            .setSpeechMode(TextToSpeechClient.NEWTONE_TALK_1)     // 음성합성방식
            .setSpeechSpeed(1.0)            // 발음 속도(0.5~4.0)
            .setSpeechVoice(TextToSpeechClient.VOICE_WOMAN_READ_CALM)  //TTS 음색 모드 설정(여성 차분한 낭독체)
            .setListener(object : TextToSpeechListener {
                //아래 두개의 메소드만 구현해 주면 된다. 음성합성이 종료될 때 호출된다.
                override fun onFinished() {
                    val intSentSize = ttsClient?.getSentDataSize()      //세션 중에 전송한 데이터 사이즈
                    val intRecvSize = ttsClient?.getReceivedDataSize()  //세션 중에 전송받은 데이터 사이즈

                    val strInacctiveText = "handleFinished() SentSize : $intSentSize  RecvSize : $intRecvSize"

                    Log.i(TAG, strInacctiveText)
                }

                override fun onError(code: Int, message: String?) {
                    Log.d(TAG, "err!: $code")
                }
            }).build()
        Log.d(TAG, "speak start!: $txt")
        ttsClient.play(txt)
    }

    private fun nextStep(txt: String?) {
        var intent = Intent(this@SpeechAPI, TabActivity::class.java)
        var type = ""
        var isUnderstand = true
        var again = true

        launch(Dispatchers.Main) {
            val res = HttpRequestHelper().requestKtorIo(txt!!)
            val json = JSONObject(res)

            val command : Int = json.getInt("command")
            val view : Int = json.getInt("view")
            val value : Int = json.getInt("value")
            val stock: String = json.getString("stock")
            val price: Int = json.getInt("price")


            when(command){
                0->{// 이해 못함
                    isUnderstand = false
                }
                1->{ //홈 화면 이동
                    startUsingSpeechSDK2("홈 화면으로 이동할게요")
                    val intent = Intent(this@SpeechAPI, TabActivity::class.java)
                    intent.putExtra("tab", 0)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                2->{ //계좌 정보
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "total_info"
                }
                3->{ //총자산
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "total_assets"
                }
                4->{ //주문 가능 금액
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "available_to_order"
                }
                5->{ //보유 주식 정보
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "my_stocks"
                }
                6->{ //특정 보유 주식 정보
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "my_stock_target"
                    intent.putExtra("target", stock)
                }
                7->{ //미체결 목록
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "not_sign_stocks"
                }
                8->{ //관심 주식 추가 삭제

                    val resultList = (arrItemKospiCode+arrItemKosdaqCode).sorted().filter{ it.name.startsWith(stock) } //입력한 텍스트와 주식 목록 비교->필터링
                    if (resultList.isNotEmpty()) {
                        val stockCode =resultList[0].code
                        val c = db.isExist_like(database,stockCode)!!
                        if(c.count> 0){
                            db.delete_like(database,stockCode)
                            startUsingSpeechSDK2("$stock 종목이 관심종목에서 제어어되었어요")
                       } else {
                            val contentValues = ContentValues()

                            contentValues.put("code", stockCode)
                            contentValues.put("name", stock)
                            db.insert_like(contentValues, database)
                            startUsingSpeechSDK2("$stock 종목이 관심종목에 추가되었어요")
                        }

                    } else {
                        startUsingSpeechSDK2("해당주식을 찾지 못했어요")
                    }
                }
                9->{ //지수 정보
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    if(value == 0) {
                        type = "kospi"
                    } else {
                        type = "kosdaq"
                    }
                }
                10->{ // 뉴스 정보
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    type="news"
                }
                11->{ //자동매매 on
                    db.update(database,value)
                    intent = Intent(this@SpeechAPI, TabActivity::class.java)
                    intent.putExtra("tab", 2)

                    type="auto_onoff"
                    isUnderstand = false
                    again = false

                    var state ="켤"
                    if(value == 0){
                        state = "끌"
                    }
                    startUsingSpeechSDK2("자동매매 $state 까요?")
                    Thread.sleep(2000)
                }
                12 ->{
                    db.update12(database,value)
                    val voice = if(value ==1) "켤게요" else "끌게요"
                    startUsingSpeechSDK2("실시간 알림을 $voice")
                    type="alarm_onoff"
                }
                13 -> {
                    if(value == 0) {
                        db.update13(database,0)
                        startUsingSpeechSDK2("목소리를 껐어요")
                    } else if(value==1) {
                        db.update13(database,50)
                        startUsingSpeechSDK2("목소리를 켰어요")
                    }else if(value==2) {
                        db.update13(database,70)
                        startUsingSpeechSDK2("목소리를 키웠어요")
                    }else if(value==3) {
                        db.update13(database,30)
                        startUsingSpeechSDK2("목소리를 줄였어요")
                    }
                }
                15->{
                    var txt = "중간"
                    db.updateTradeLevel(database,value)
                    when(value) {
                        0 -> {
                            txt = "매우낮음"
                        }
                        1 -> {
                            txt = "낮음"
                        }
                        2 -> {
                            txt = "중간"
                        }
                        3 -> {
                            txt = "높음"
                        }
                        4 -> {
                            txt = "매우높음"
                        }
                    }

                    startUsingSpeechSDK2("투자 강도를 $txt 으로 설정했어요.")
                }
                17->{ //특정 주식 정보
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    type = "stockPrice"
                    intent.putExtra("target", stock)
                }
                18->{
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    type = "stockPrice"
                    intent.putExtra("target", stock)
                    intent.putExtra("option", 1)
                }
                19->{
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    type = "stockPrice"
                    intent.putExtra("target", stock)
                    intent.putExtra("option", 2)
                    intent.putExtra("value", value)

                }
                21->{ //시장가 매수
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "buy_mp"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value.toString())
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock 시장가 매수하시겠습니까?")
                    Thread.sleep(5000)
                }
                22->{//시장가 매도
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "sell_mp"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value.toString())
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock 시장가 매도하시겠습니까?")
                    Thread.sleep(4000)
                }
                23->{ //지정가 매수
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "buy"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value.toString())
                    intent.putExtra("price", price.toString())
                    isUnderstand = false
                    again = false


                    startUsingSpeechSDK2("$stock $value 주 $price 원에 매수하시겠습니까?")
                    Thread.sleep(5000)
                }
                24->{ //지정가 매도
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "sell"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value.toString())
                    intent.putExtra("price", price.toString())
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock $value 주 $price 원에 매도하시겠습니까?")
                    Thread.sleep(6000)
                }
                25->{
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "cancel"
                    intent.putExtra("target", stock)
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock 주문을 취소할까요?")
                    Thread.sleep(3500)
                }
            }

            if(isUnderstand) {
                startUsingSpeechSDK2("처리중입니다")
                Thread.sleep(3000)
                intent.putExtra("type", type)
                startActivity(intent)
                finish()
            } else {
                if(again){
                    startUsingSpeechSDK2("무슨말인지 모르겠어요. 다시 한 번 말씀해 주세요")
                    Thread.sleep(5000)
                    startUsingSpeechSDK()
                } else {
                    intent.putExtra("type", type)
                    startUsingSpeechSDK(true ,intent,type)
                }

            }
        }
    }

    private fun nextStep2(txt: String?,intent:Intent?,type:String?) {
        if(txt!!.indexOf("네")>-1 || txt.indexOf("그래")>-1 || txt.indexOf("응")>-1 || txt.indexOf("맞아")>-1) {
            if(type=="auto_onoff"){
                startUsingSpeechSDK2("자동매매 설정을 교체합니다.")
            }

            startActivity(intent)
            finish()
        }
    }

    private fun volumeUp(){
        val c = db.select(database)!!
        var vol = 50;
        if(c.moveToNext()) {
            vol = c.getString(c.getColumnIndex("setting13")).toInt()
        }
        mAudioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * vol/100.0).toInt(),
           0
        )
    }
}