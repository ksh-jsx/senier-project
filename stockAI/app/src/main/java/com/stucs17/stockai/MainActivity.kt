package com.stucs17.stockai

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.commexpert.CommExpertMng
import com.stucs17.stockai.Public.Database
import com.stucs17.stockai.sql.DBHelper
import com.truefriend.corelib.commexpert.intrf.IExpertInitListener
import com.truefriend.corelib.commexpert.intrf.IExpertLoginListener
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), IExpertInitListener, IExpertLoginListener {
    private val TAG : String = "HantooSample" // 로깅용 태그
    private var isConnected : Boolean = false
    private var isLoggedin : Boolean = true
    private lateinit var loginWrapper : LinearLayout
    private lateinit var loadingWrapper : LinearLayout
    private lateinit var loadingIcon : ImageView

    private var idStr : String = ""
    private var pwStr : String = ""
    private var caPwStr : String = ""
    private var numPwStr : String = ""
    private val db = Database()

    //sql 관련
    private lateinit var dbHelper: DBHelper
    lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!requestPermissions())
            startApp()

        val button = findViewById<Button>(R.id.buttonLogin)
        button.setOnClickListener{
            idStr = findViewById<EditText>(R.id.editId).text.toString()
            pwStr = findViewById<EditText>(R.id.editPw).text.toString()
            caPwStr = findViewById<EditText>(R.id.editCaPw).text.toString()
            numPwStr = findViewById<EditText>(R.id.editNumPw).text.toString()
            if (!isConnected){
                Toast.makeText(this, "서버가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                db.login(idStr,pwStr,caPwStr,numPwStr)
            }
        }

        loginWrapper = findViewById(R.id.loginWrapper)
        loadingWrapper = findViewById(R.id.loadingWrapper)
        loadingIcon = findViewById(R.id.loadingIcon)
        Glide.with(this).load(R.raw.loading_icon).into(loadingIcon);

        dbHelper = DBHelper(this, "mydb.db", null, 1)
        database = dbHelper.writableDatabase

    }

    private fun startApp() {
        Log.d(TAG,"라이브러리 초기화 요청")

        // 초기화 및 통신 접속
        CommExpertMng.InitActivity(this)
        CommExpertMng.InitCommExpert(this)

        //Listener 셋팅
        CommExpertMng.getInstance().SetInitListener(this@MainActivity)
        CommExpertMng.getInstance().SetLoginListener(this@MainActivity)

        //"0"리얼 ,  "1" 모의투자
        CommExpertMng.getInstance().SetDevSetting("0")
    }


    override fun onSessionConnecting() {
        Log.d(TAG, "서버 접속 시작")
    }

    override fun onSessionConnected(isSuccess: Boolean, msg: String?) {
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
        isConnected = true

        loginWrapper.visibility = View.VISIBLE
        loadingWrapper.visibility = View.GONE
        if (!isConnected){
            Toast.makeText(this, "서버가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {

            val c = db.select(database)!!

            if(c.moveToNext()) {

                idStr = c.getString(c.getColumnIndex("id"))
                pwStr = c.getString(c.getColumnIndex("pwd"))
                caPwStr = c.getString(c.getColumnIndex("certPwd"))
                numPwStr = c.getString(c.getColumnIndex("numPwd"))

                findViewById<EditText>(R.id.editId).setText(idStr)
                findViewById<EditText>(R.id.editPw).setText(pwStr)
                findViewById<EditText>(R.id.editCaPw).setText(caPwStr)
                findViewById<EditText>(R.id.editNumPw).setText(numPwStr)

                db.login(idStr, pwStr, caPwStr, numPwStr)
                isLoggedin = false
            } else {
                Toast.makeText(baseContext, "자동로그인 실패", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onRequiredRefresh() {
        Log.d(TAG, "재접속 완료")
    }

    private fun requestPermissions() : Boolean {
        val permissions: Array<String> = arrayOf( Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE)
        val needPermissions : ArrayList<String> = ArrayList<String>()

        for (perm in permissions){
            if (perm == Manifest.permission.READ_PHONE_STATE){
                // Android 11 이상에서는 READ_PHONE_STATE 권한 체크 X
            }
            else if (ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                needPermissions.add(perm)
            }
        }

        return if (needPermissions.isNotEmpty()){
            Log.d("HantooSample", "권한 요청")

            ActivityCompat.requestPermissions(this, needPermissions.toArray(arrayOfNulls<String>(needPermissions.size)),0 )
            true
        } else
            false
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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


    /**
     * IExpertLoginListener
     */
    override fun onLoginResult(isSuccess: Boolean, strErrorMsg: String?) {
        if(!isSuccess)
            Toast.makeText(this, strErrorMsg, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Result : $isSuccess, Message : $strErrorMsg" )
    }

    override fun onAccListResult(isSuccess: Boolean, strErrorMsg: String?) {
        Log.d(TAG, "Result : $isSuccess, Message : $strErrorMsg" )
    }

    override fun onPublicCertResult(isSuccess: Boolean) {
        Log.d(TAG, "Result : $isSuccess" )
    }

    override fun onLoginFinished() {
        Log.d(TAG,"onLoginFinished")
        Log.d(TAG, CommExpertMng.getInstance().GetLoginUserID())

        //var arr : Array<String> = arrayOf("shkim787")
        //database.delete("user","id=?",arr)

        if(isLoggedin) {
            val contentValues = ContentValues()
            contentValues.put("id", idStr)
            contentValues.put("pwd", pwStr)
            contentValues.put("certPwd", caPwStr)
            contentValues.put("numPwd", numPwStr)
            contentValues.put("autoTrade", 0)
            db.insert(contentValues,database)
            Toast.makeText(this, "추가되었습니다.", Toast.LENGTH_SHORT).show()
        }
        gotoHome()
    }

    private fun gotoHome() {
        val intent = Intent(this@MainActivity, TabActivity::class.java)
        startActivity(intent)
        finish()
    }
}