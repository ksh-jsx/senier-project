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


class SpeechAPI : AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 1000
    private val STORAGE_REQUEST_CODE = 1000
    private val NETWORK_STATE_CODE = 0

    private lateinit var test_tv : TextView

    private lateinit var mAudioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listen)

        mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        test_tv = findViewById(R.id.test_tv)

        setupPermissions()
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

    private fun startUsingSpeechSDK(){
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
                startUsingSpeechSDK2("네!")
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

                Log.d(TAG, texts?.get(0).toString())
                //정확도가 높은 첫번째 결과값을 텍스트뷰에 출력
                runOnUiThread {
                    volumeUp()
                    val txt = texts?.get(0)
                    //startUsingSpeechSDK2("들은 내용은 $txt")
                    test_tv.text = txt
                    nextStep(txt)
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
            }
            else if (txt.indexOf("매도")>-1) {
                intent = Intent(this@SpeechAPI, Trade::class.java)
                type = "sell"
                intent.putExtra("target", txt.split(" ")[0])
            }
            else{
                isUnderstand = false
                startUsingSpeechSDK2("무슨말인지 모르겠어요")
            }


        if(isUnderstand) {
            intent.putExtra("type", type)
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