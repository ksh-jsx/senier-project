package com.openapi.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.commexpert.CommExpertMng;
import com.truefriend.corelib.commexpert.intrf.IExpertInitListener;
import com.truefriend.corelib.commexpert.intrf.IExpertLoginListener;

import java.util.ArrayList;

@SuppressLint("NewApi")
public class sampleActivity extends Activity implements View.OnClickListener ,IExpertInitListener,IExpertLoginListener
{
	private SampleBaseView m_SampleBaseView = null;
	private SamlpeLoginView m_viewLogin = null;
	private SampleDataView m_ViewData = null;
	private SampleOrderView m_ViewOrder = null;
	
	String m_strUserID;
	
	ArrayList<String> m_arrAccount = null;				//계좌리스트
	ArrayList<String> m_arrAccountName = null;		//계좌명 리스트
	ArrayList<String> m_arrAccountCode = null;		//상품코드 리스트
	
	/* Called when the activity is first created. */
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );

		m_arrAccount = new ArrayList<String>();
		m_arrAccountName = new ArrayList<String>();
		m_arrAccountCode = new ArrayList<String>();

		m_SampleBaseView = new SampleBaseView(this);
		m_SampleBaseView.setLayoutParams( new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT ) );
		m_SampleBaseView.setBackgroundColor( Color.rgb( 0, 0, 0 ) );

		setContentView(m_SampleBaseView);

		//Activity 셋팅
		CommExpertMng.InitActivity(this);

		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
		{
			if (!PermissionManager.getInstance().checkPermission()) {
				PermissionManager.getInstance().setOnPermissionListener(new PermissionManager.OnPermissionListener() {
					@Override
					public void onPermissionResult(boolean isSucs, Object objPermission)
					{
						if( isSucs ) {
							startApp();
						}
						else {
							Toast.makeText(getBaseContext(), "앱 권한 허용이 필요합니다.", Toast.LENGTH_SHORT ).show();
							finish();
						}
					}
				});
				PermissionManager.getInstance().requestPermissions();
			}
			else {
				startApp();
			}
		}
		else {
			startApp();
		}

	}

	public void startApp()
	{
		/**
		 * ExpertMng 기본 셋팅
		 */
		// 초기화 및 통신 접속
		CommExpertMng.InitCommExpert(this);
		//Listener 셋팅
		CommExpertMng.getInstance().SetInitListener(sampleActivity.this);
		CommExpertMng.getInstance().SetLoginListener(sampleActivity.this);
		//"0"리얼 ,  "1" 모의투자
		CommExpertMng.getInstance().SetDevSetting("0");
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		PermissionManager.getInstance().release();
		
		if(m_SampleBaseView != null)
		{
			m_SampleBaseView.removeAllViews();
			m_SampleBaseView = null;
		}

		if(m_viewLogin != null)
		{
			m_viewLogin.releaseView();
			m_viewLogin = null;
		}
	
		if(m_ViewData != null)
		{
			m_ViewData.releaseView();
			m_ViewData = null;
		}	
		
		if(m_ViewOrder != null)
		{
			m_ViewOrder.releaseView();
			m_ViewOrder = null;
		}	
		
		if(m_arrAccount != null)
		{
			m_arrAccount.clear();
			m_arrAccount = null;
		}
		
		if(m_arrAccountName != null)	
		{
			m_arrAccountName.clear();
			m_arrAccountName = null;
		}
	
		if(m_arrAccountName != null)	
		{		
			m_arrAccountCode.clear();
			m_arrAccountCode = null;
		}
		/**
		 * ExpertMng 종료...
		 */
		CommExpertMng.getInstance().Close();
	}

	@Override
	public void onSessionConnecting() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "서버 접속 시작.", Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onSessionConnected(boolean isSuccess, String strErrorMsg) {
		// TODO Auto-generated method stub
		//서버 성공
		if(isSuccess == true)
		{
			Toast.makeText(this, strErrorMsg, Toast.LENGTH_SHORT ).show();
		}
		else//서버 실패
		{
			Toast.makeText(this, strErrorMsg, Toast.LENGTH_SHORT ).show();
		}
	}
	
	@Override
	public void onAppVersionState(boolean isDone) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "라이브러리 버젼체크 완료.", Toast.LENGTH_SHORT ).show();
	}
		
	@Override
	public void onMasterDownState(boolean isDone) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Master 파일 DownLoad...", Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onMasterLoadState(boolean isDone) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Master 파일 Loading...", Toast.LENGTH_SHORT ).show();
	}
	@Override
	public void onInitFinished() {
		// TODO Auto-generated method stub
		/**
		 * 로그인View를 생성 하고 클릭 이벤트 지정
		 */
		m_viewLogin = new SamlpeLoginView(this);
		m_viewLogin.initView();
		m_viewLogin.setActivity(this);
			
		m_viewLogin.setCtrlClickListener(SamlpeLoginView.ID.CTRL_ID_LOGIN, this);
		m_viewLogin.setCtrlClickListener(SamlpeLoginView.ID.CTRL_ID_CANCEL, this);
		m_viewLogin.setBackgroundColor( Color.rgb( 192, 192, 192 ) );
		m_SampleBaseView.addView(m_viewLogin, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		PermissionManager.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void onRequiredRefresh() {
		// TODO Auto-generated method stub
		Toast.makeText( getBaseContext(), "재접속 되었습니다.", Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int nCtrlId = v.getId();
		/**
		 * 사용자가 로그인을 요청 할 때 로그인에 필요한 정보를 확인 하고
		 * 필요한 정보가 전부 입력되지 않았을 경우 해당 정보를 요청하는 메시지를 보여주거나
		 * 필요한 정보가 모두 갖춰졌을 때 해당정보를 기반으로 로그인을 요청한다.
		 */
		switch(nCtrlId)
		{
		case SamlpeLoginView.ID.CTRL_ID_LOGIN :
		    if( m_viewLogin.getUserId().isEmpty() )
		    {
		    	Toast.makeText( getBaseContext(), "아이디를 확인하세요.", Toast.LENGTH_SHORT ).show();
	            return;
		    }
		    else if(m_viewLogin.getUserPwd().isEmpty())
		    {
		    	Toast.makeText( getBaseContext(), "비밀번호를 확인하세요.", Toast.LENGTH_SHORT ).show();
		        return;
		    }
		    else if(m_viewLogin.getCertPwd().isEmpty() && !CommExpertMng.getInstance().GetMotu())
		    {
		    	Toast.makeText( getBaseContext(), "공인인증 비밀번호를 확인하세요.", Toast.LENGTH_SHORT ).show();
		        return;
		    }
			break;
		/**
		 * 유저가 로그인 취소를 할 경우
		 */
		case SamlpeLoginView.ID.CTRL_ID_CANCEL :
			
			finish();
			break;
		}	
		//로그인 시작
		if (CommExpertMng.getInstance().GetMotu()) CommExpertMng.getInstance().StartLogin(m_viewLogin.getUserId(), m_viewLogin.getUserPwd());
		else CommExpertMng.getInstance().StartLogin(m_viewLogin.getUserId(), m_viewLogin.getUserPwd(), m_viewLogin.getCertPwd());
	}

	@Override
	public void onLoginResult(boolean isSuccess, String strErrorMsg) {
		// TODO Auto-generated method stub
		if(isSuccess == true )
			Toast.makeText( getBaseContext(), "로그인 TR 성공", Toast.LENGTH_SHORT ).show();
		else
			Toast.makeText( getBaseContext(), strErrorMsg, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onAccListResult(boolean isSuccess, String strErrorMsg) {
		// TODO Auto-generated method stub
		if(isSuccess == true )
			Toast.makeText( getBaseContext(), "계좌리스트 조회 TR 성공", Toast.LENGTH_SHORT ).show();
		else
			Toast.makeText( getBaseContext(), strErrorMsg, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onPublicCertResult(boolean isSuccess) {
		// TODO Auto-generated method stub
		String strMsg = "";
		if(isSuccess == true )
			strMsg = "공인인증 검증 성공";
		else
			strMsg = "공인인증 검증 실패";
		
		Toast.makeText( getBaseContext(), strMsg, Toast.LENGTH_SHORT ).show();
	}

	@Override
	public void onLoginFinished() {
		// TODO Auto-generated method stub
		String strMsg = "로그인 성공";
		Toast.makeText( getBaseContext(), strMsg, Toast.LENGTH_SHORT ).show();
		m_strUserID = m_viewLogin.getUserId();
		
		ShowDataView();
	}
	
	public void ShowDataView()
	{
		if(m_ViewData != null)
		{
			m_ViewData.releaseView();
			m_ViewData = null;
		}	
		m_ViewData = new SampleDataView(this);
		m_ViewData.initView(this);
		m_ViewData.SetUserID(m_strUserID);
		//계좌리스트 
		int nCount = CommExpertMng.getInstance().GetAccountSize();
		if(nCount >0)
		{
			String strAcc = "";
			String strAccName = "";
			String strAccCode ="";
			for(int i = 0; i <nCount ; i++)
			{
				strAcc = CommExpertMng.getInstance().GetAccountNo(i);				//계좌번호
				strAccName = CommExpertMng.getInstance().GetAccountName(i);		//계좌명
				strAccCode = CommExpertMng.getInstance().GetAccountCode(i);		//상품코드
				if(strAccCode.contains("01"))	//위탁계좌
				{
					m_arrAccount.add(strAcc);
					m_arrAccountName.add(strAccName);
					m_arrAccountCode.add(strAccCode);
				}
			}		
			if(m_arrAccount.size() > 0)
			{
				m_ViewData.SetAccount(m_arrAccount.get(0));					//계좌번호
				m_ViewData.SetAccountCode(m_arrAccountCode.get(0));		//계좌 상품코드
				
				CommExpertMng.getInstance().SetCurrAccountInfo(m_arrAccount.get(0),m_arrAccountCode.get(0));
			}
		}
		m_ViewData.setBackgroundColor( Color.rgb( 192, 192, 192 ) );
		m_SampleBaseView.removeAllViews();
		m_SampleBaseView.addView(m_ViewData, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	public void ShowOrderView()
	{
		if(m_ViewOrder != null)
		{
			m_ViewOrder.releaseView();
			m_ViewOrder = null;
		}	
		m_ViewOrder = new SampleOrderView(this);
		m_ViewOrder.initView(this);
		m_ViewOrder.SetUserID(m_strUserID);
		//계좌리스트 
		int nCount = CommExpertMng.getInstance().GetAccountSize();
		if(nCount >0)
		{
			String strAcc = "";
			String strAccName = "";
			String strAccCode ="";
			for(int i = 0; i <nCount ; i++)
			{
				strAcc = CommExpertMng.getInstance().GetAccountNo(i);				//계좌번호
				strAccName = CommExpertMng.getInstance().GetAccountName(i);		//계좌명
				strAccCode = CommExpertMng.getInstance().GetAccountCode(i);		//상품코드
				if(strAccCode.contains("01"))	//위탁계좌
				{
					m_arrAccount.add(strAcc);
					m_arrAccountName.add(strAccName);
					m_arrAccountCode.add(strAccCode);
				}
			}		
			if(m_arrAccount.size() > 0)
			{
				m_ViewOrder.SetAccount(m_arrAccount.get(0));					//계좌번호
				m_ViewOrder.SetAccountCode(m_arrAccountCode.get(0));		//계좌 상품코드
				
				CommExpertMng.getInstance().SetCurrAccountInfo(m_arrAccount.get(0),m_arrAccountCode.get(0));
			}
		}
		m_ViewOrder.setBackgroundColor( Color.rgb( 192, 192, 192 ) );
		m_SampleBaseView.removeAllViews();
		m_SampleBaseView.addView(m_ViewOrder, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}	
	
	public void sendMessage(int nMsg, int nArg1, int nArg2)
	{
		if(m_handlerProc == null) return;

		Message msg = m_handlerProc.obtainMessage(nMsg, nArg1, nArg2);
		m_handlerProc.sendMessage(msg);
	}
	private final Handler m_handlerProc = new Handler()
	{
		@Override
		public void handleMessage(Message message)
		{
			switch(message.what)
			{
				case 0:				//ViewData
					ShowDataView();
					break;
					
				case 1 :				//ViewOrder
					ShowOrderView();
					break;
					
			}
		}
	};
}
