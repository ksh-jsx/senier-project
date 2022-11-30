package com.stucs17.stockai.Public

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kakao.sdk.newtoneapi.*
import com.stucs17.stockai.R
import com.stucs17.stockai.TabActivity
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

    private lateinit var mAudioManager: AudioManager

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

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

    fun startUsingSpeechSDK(isOutside:Boolean = false, intent: Intent? = null){
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
                        nextStep2(txt,intent)
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

                }
                7->{ //미체결 목록
                    intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                    type = "not_sign_stocks"
                }
                8->{ //관심 주식 추가 삭제

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

                }
                17->{ //특정 주식 정보
                    intent = Intent(this@SpeechAPI, StockIndex::class.java)
                    type = "stockPrice"
                    intent.putExtra("target", stock)
                }
                21->{ //시장가 매수
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "buy_mp"
                    intent.putExtra("target", stock)
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock 시장가 매수하시겠습니까?")
                    Thread.sleep(5000)
                }
                22->{//시장가 매도
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "sell_mp"
                    intent.putExtra("target", stock)
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock 시장가 매도하시겠습니까?")
                    Thread.sleep(5000)
                }
                23->{ //지정가 매수
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "buy"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value)
                    intent.putExtra("price", price)
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock $value 주 $price 원에 매수하시겠습니까?")
                    Thread.sleep(6000)
                }
                24->{ //지정가 매도
                    intent = Intent(this@SpeechAPI, Trade::class.java)
                    type = "sell"
                    intent.putExtra("target", stock)
                    intent.putExtra("quantity", value)
                    intent.putExtra("price", price)
                    isUnderstand = false
                    again = false

                    startUsingSpeechSDK2("$stock $value 주 $price 원에 매도하시겠습니까?")
                    Thread.sleep(6000)
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
                    startUsingSpeechSDK(true ,intent)
                }

            }
        }

/*
            if(txt!!.indexOf("수익률")>-1) {
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "profit_or_loss"
            }
            else if(txt.indexOf("총 자산")>-1) {
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "total_assets"
            }
            else if(txt.indexOf("주문 가능")>-1){
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "available_to_order"
            }
            else if(txt.indexOf("총 매입가")>-1){
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "total_order_price"
            }
            else if(txt.indexOf("내 주식")>-1){
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "my_stocks"
            }
            else if(txt.indexOf("미체결")>-1){
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "not_sign_stocks"
            }
            else if(txt.indexOf("관심 목록")>-1){
                intent = Intent(this@SpeechAPI, AccountInfo::class.java)
                type = "interesting_stocks"
            }
            else if(txt.indexOf("코스피")>-1){
                intent = Intent(this@SpeechAPI, StockIndex::class.java)
                type = "kospi"
            }
            else if(txt.indexOf("코스닥")>-1){
                intent = Intent(this@SpeechAPI, StockIndex::class.java)
                type = "kosdaq"
            }
            else if (txt.indexOf("주가")>-1) {
                intent = Intent(this@SpeechAPI, StockIndex::class.java)
                type = "stockPrice"
                intent.putExtra("target", txt.split(" ")[0])
            }
            else if (txt.indexOf("매수")>-1) {
                intent = Intent(this@SpeechAPI, Trade::class.java)
                type = "buy"
                intent.putExtra("target", txt.split(" ")[0])
                isUnderstand = false
                again = false

                startUsingSpeechSDK2("이트론 1주 매수하시겠습니까?")
                Thread.sleep(5000)
            }
            else if (txt.indexOf("매도")>-1) {
                intent = Intent(this@SpeechAPI, Trade::class.java)
                type = "sell"
                intent.putExtra("target", txt.split(" ")[0])
                again = false
            }
            else{
                isUnderstand = false
            }



*/
    }

    private fun nextStep2(txt: String?,intent:Intent?) {
        if(txt!!.indexOf("네")>-1 || txt.indexOf("그래")>-1 || txt.indexOf("응")>-1 || txt.indexOf("맞아")>-1) {
            startActivity(intent)
            finish()
        }
    }

    private fun volumeUp(){
          mAudioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        mAudioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 50/100.0).toInt(),
           0
        )
    }
}