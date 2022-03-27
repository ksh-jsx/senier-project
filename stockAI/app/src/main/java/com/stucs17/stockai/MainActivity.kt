package com.stucs17.stockai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.commexpert.CommExpertMng
import com.truefriend.corelib.commexpert.intrf.IExpertInitListener

class MainActivity : AppCompatActivity(), IExpertInitListener {

    private val TAG : String = "HantooSample" // 로깅용 태그

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!requestPermissions()) // 권한이 이미 전부 허용되었다면,
            startApp() // 초기화 함수 바로 호출
    }

    private fun startApp() {
        // 초기화 및 통신 접속
        CommExpertMng.InitActivity(this)
        CommExpertMng.InitCommExpert(this)

        //Listener 셋팅
        CommExpertMng.getInstance().SetInitListener(this@MainActivity)

        //"0"리얼 ,  "1" 모의투자
        CommExpertMng.getInstance().SetDevSetting("1")
    }

    override fun onSessionConnecting() {
        Log.d(TAG, "서버 접속 시작")
    }

    override fun onSessionConnected(isSuccess: Boolean, msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        if (msg != null) {
            Log.d(TAG, msg)
        }
    }

    override fun onAppVersionState(p0: Boolean) {
        Log.d(TAG, "라이브러리 버젼체크 완료.")
    }

    override fun onMasterDownState(p0: Boolean) {
        Log.d(TAG, "Master 파일 DownLoad...")
    }

    override fun onMasterLoadState(p0: Boolean) {
        Log.d(TAG, "Master 파일 Loading...")
    }

    override fun onInitFinished() {
        Log.d(TAG, "초기화 작업 완료")
        Toast.makeText(this, "초기화 작업 완료", Toast.LENGTH_SHORT).show()
        // TODO : Login
    }

    override fun onRequiredRefresh() {
        Log.d(TAG, "재접속 완료")
    }



    private fun requestPermissions() : Boolean {
        // 앱에서 필요한 권한 리스트
        val permissions: Array<String> = arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE)
        // 요청 해야할 권한 리스트
        val needPermissions : ArrayList<String> = ArrayList<String>()

        // 필요 권한 리스트 중 아직 허용되지 않은 권한들만 추가
        for (perm in permissions){
            if (perm == Manifest.permission.READ_PHONE_STATE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                // Android 11 이상에서는 READ_PHONE_STATE 권한 체크 X
            }
            else if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                needPermissions.add(perm)
            }
        }

        // 요청 리스트가 비어있지 않다면 권한 요청 팝업
        return if (needPermissions.isNotEmpty()){
            Log.d("HantooSample", "권한 요청")

            ActivityCompat.requestPermissions(this, needPermissions.toArray(arrayOfNulls<String>(needPermissions.size)),0 )
            true
        } else
            false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show();
                        Log.e(TAG, "권한 취소 : ${permissions[i]}")
                        finishAndRemoveTask()
                        return
                    }
                    else {
                        Log.d(TAG, "권한 승인 : ${permissions[i]}")
                    }
                }

                Toast.makeText(this, "앱 실행을 위한 권한이 설정 되었습니다", Toast.LENGTH_LONG).show()
                startApp()
                return

            } else {
                Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다", Toast.LENGTH_LONG).show()
                Log.e(TAG, "grantResults is Empty")
            }
        }

        finishAndRemoveTask()
    }

}