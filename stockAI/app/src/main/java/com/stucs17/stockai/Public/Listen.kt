package com.stucs17.stockai.Public

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kakao.sdk.newtoneapi.SpeechRecognizeListener
import com.kakao.sdk.newtoneapi.SpeechRecognizerClient
import com.kakao.sdk.newtoneapi.SpeechRecognizerManager
import com.stucs17.stockai.MainActivity
import com.stucs17.stockai.TabActivity

class Listen: AppCompatActivity() {

    private val RECORD_REQUEST_CODE = 1000
    private val STORAGE_REQUEST_CODE = 1000

    fun setupSpeechPermissions(){
        val permission_audio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val permission_storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        when {
            permission_audio != PackageManager.PERMISSION_GRANTED -> {
                Log.d(ContentValues.TAG, "Permission to recode denied")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_REQUEST_CODE)
            }
            permission_storage != PackageManager.PERMISSION_GRANTED -> {
                Log.d(ContentValues.TAG, "Permission to recode denied")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_REQUEST_CODE)
            }
            else -> {
                //본문실행
                startUsingSpeechSDK()
            }
        }
    }

    fun startUsingSpeechSDK(){
        Toast.makeText(this, "Start Newton", Toast.LENGTH_SHORT).show()

        //SDK 초기화
        SpeechRecognizerManager.getInstance().initializeLibrary(this)

        //클라이언트 생성
        val builder = SpeechRecognizerClient.Builder().setServiceType(SpeechRecognizerClient.SERVICE_TYPE_WEB)
        val client = builder.build()

        //Callback
        client.setSpeechRecognizeListener(object : SpeechRecognizeListener {
            //콜백함수들
            override fun onReady() {
                Log.d(ContentValues.TAG, "모든 하드웨어 및 오디오 서비스가 준비되었습니다.")
            }

            override fun onBeginningOfSpeech() {
                Log.d(ContentValues.TAG, "사용자가 말을 하기 시작했습니다.")
            }

            override fun onEndOfSpeech() {
                Log.d(ContentValues.TAG, "사용자의 말하기가 끝이 났습니다. 데이터를 서버로 전달합니다.")
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
                val confs = results?.getIntegerArrayList(SpeechRecognizerClient.KEY_CONFIDENCE_VALUES)

                Log.d(ContentValues.TAG, texts?.get(0).toString())
                //정확도가 높은 첫번째 결과값을 텍스트뷰에 출력
                runOnUiThread {
                    Log.d(ContentValues.TAG, "너가 한 말:"+ texts?.get(0))
                }
            }

            override fun onAudioLevel(audioLevel: Float) {
                //Log.d(TAG, "Audio Level(0~1): " + audioLevel.toString())
            }
            override fun onError(errorCode: Int, errorMsg: String?) {
                //에러 출력 해 봄
                Log.d(ContentValues.TAG, "Error: $errorMsg")
            }
            override fun onFinished() {
            }
        })

        //음성인식 시작함
        client.startRecording(true)
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
        }
    }

}