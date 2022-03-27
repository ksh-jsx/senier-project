package com.stock_app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.CommExpertMng
import com.stock_app.R
import com.truefriend.corelib.commexpert.intrf.IExpertInitListener
import com.truefriend.corelib.commexpert.intrf.IExpertLoginListener
import kotlinx.android.synthetic.main.activity_hello.*

class HelloActivity : AppCompatActivity(), IExpertInitListener, IExpertLoginListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)


        /** * ExpertMng 기본 셋팅 */
        // Activity 셋팅
        CommExpertMng.InitActivity(this);
        // 초기화 및 통신 접속
        CommExpertMng.InitCommExpert(this);
        // Listener 셋팅
        CommExpertMng.getInstance().SetInitListener(this@HelloActivity);
        CommExpertMng.getInstance().SetLoginListener(this@HelloActivity);
        // "0"리얼 , "1" 개발
        CommExpertMng.getInstance().SetDevSetting("1");


        btnHello.setOnClickListener(){
            //로그인 시작
            CommExpertMng.getInstance().StartLogin ( "shkim787", "xotnrla7^", "tjdgus$1123" );
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        /** * ExpertMng 종료...  */
        CommExpertMng.getInstance().Close()
    }

    override fun onSessionConnecting() {}
    override fun onSessionConnected(isSuccess: Boolean, strErrorMsg: String?) {}
    override fun onAppVersionState(isDone: Boolean) {}
    override fun onMasterDownState(isDone: Boolean) {}
    override fun onMasterLoadState(isDone: Boolean) {}
    override fun onInitFinished() {}
    override fun onRequiredRefresh() {}

    override fun onLoginResult(isSuccess: Boolean, strErrorMsg: String?) {}
    override fun onAccListResult(isSuccess: Boolean, strErrorMsg: String?) {}
    override fun onPublicCertResult(isSuccess: Boolean) {}
    override fun onLoginFinished() {}
}