package com.openapi.sample;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;

//로그인 뷰
@SuppressLint("ClickableViewAccessibility")
public class SamlpeLoginView extends FrameLayout
{

	public interface ID
    {
        int     CTRL_ID_LOGIN = 100;
        int     CTRL_ID_CANCEL = 101;
    }	
	private Activity	m_activity;

	private String      m_strUserId = "";
	private String      m_strUserPwd = "";
	private String       m_strAuthPwd = "";
 	
    private EditText	m_editUserId;
	private EditText	m_editUserPwd;
	private EditText	m_editCertPwd;

	public SamlpeLoginView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setActivity(Activity act)
	{
		m_activity = act;
	}
	
	public Activity getActivity()
	{
		return m_activity;
	}
	
	//초기화
	public void initView()
	{	
		LinearLayout layoutFrame = new LinearLayout(getContext());
		layoutFrame.setOrientation(LinearLayout.VERTICAL);
		//아이디
		TextView viewIDTitle = makeTextView("아이디");
		m_editUserId = makeEditView("");
		
		LinearLayout layoutID = new LinearLayout(getContext());
		layoutID.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutID.addView(viewIDTitle, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutID.addView(m_editUserId, new LinearLayout.LayoutParams(500, LayoutParams.MATCH_PARENT));
		layoutID.setPadding(100, 20, 100, 20);
		layoutFrame.addView(layoutID, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 200));
		
		setEditText(m_editUserId, true);
		//비밀번호
		TextView viewPWTitle = makeTextView("비밀번호");
		m_editUserPwd = makeEditView("");
	
		LinearLayout layoutPW = new LinearLayout(getContext());
		layoutPW.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutPW.addView(viewPWTitle, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutPW.addView(m_editUserPwd, new LinearLayout.LayoutParams(500, LayoutParams.MATCH_PARENT));
		layoutPW.setPadding(100, 20, 100, 20);
		layoutFrame.addView(layoutPW, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 200));
		
		setEditText(m_editUserPwd, false);		
		//공인인증
		TextView viewCertPWTitle = makeTextView("공인인증");
		m_editCertPwd = makeEditView("");
		
		LinearLayout layoutCertPW = new LinearLayout(getContext());
		layoutCertPW.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutCertPW.addView(viewCertPWTitle, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutCertPW.addView(m_editCertPwd, new LinearLayout.LayoutParams(500, LayoutParams.MATCH_PARENT));
		layoutCertPW.setPadding(100, 20, 100, 20);	
		layoutFrame.addView(layoutCertPW, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 200));
	
		setEditText(m_editCertPwd, false);
		//버튼	
		Button buttonLogin = makeCtrlButton(ID.CTRL_ID_LOGIN, "로그인");
		Button buttonCancel = makeCtrlButton(ID.CTRL_ID_CANCEL, "취소");
			
		LinearLayout layoutbtn = new LinearLayout(getContext());
		layoutbtn.setOrientation(LinearLayout.HORIZONTAL);
		layoutbtn.setPadding(100, 20, 100, 20);	
		layoutbtn.addView(buttonLogin, new LinearLayout.LayoutParams(400, LayoutParams.MATCH_PARENT));
		layoutbtn.addView(buttonCancel, new LinearLayout.LayoutParams(400, LayoutParams.MATCH_PARENT));
		layoutFrame.addView(layoutbtn, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 200));
	
		addView(layoutFrame, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	//Text view 생성
	protected TextView makeTextView(String strText)
	{
		TextView viewText = new TextView(getContext());
		viewText.setTextColor(Color.BLACK);
		viewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		viewText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		viewText.setText(strText);
		viewText.setSingleLine();
		
		return viewText;
	}
	//Edit view 생성
	protected EditText makeEditView(String strText)
	{
		EditText viewEdit = new EditText(getContext());
		viewEdit.setTextColor(Color.BLACK);
		viewEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		viewEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		viewEdit.setText(strText);
		viewEdit.setSingleLine();
		
		return viewEdit;
	}	
	//Edit 속성 생성	
	protected void setEditText(EditText viewEdit, boolean isNormal)
	{
		viewEdit.setLongClickable(false);
		viewEdit.setFocusableInTouchMode(true);
		viewEdit.setFocusable(true);
		viewEdit.setEllipsize(null);
       	viewEdit.setPrivateImeOptions("defaultInputmode=english;");
    	viewEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);

        if(isNormal)
        {
        	viewEdit.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
        else
        {
        	viewEdit.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
        	viewEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
	}
	//Button view 생성
	protected Button makeCtrlButton(int nCtrlId, String strText)
	{
		Button button = new Button(getContext());	
		button.setTextColor(Color.BLACK);
		button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		button.setGravity(Gravity.CENTER);
		button.setText(strText);
		button.setSingleLine();
		button.setId(nCtrlId);
		button.setPadding(0, 0, 0, 0);

		return button;
	}
	//버튼 Listener 셋팅
	public void setCtrlClickListener(int nCtrlId, View.OnClickListener listener)
	{
		View view = findViewById(nCtrlId);
		if(view == null) return;
		
		view.setOnClickListener(listener);
	}
	
	public void releaseView()
	{
		removeAllViews();

		m_editUserId = null;
		m_editUserPwd = null;
		m_editCertPwd = null;
	}
	
	/**
	 * Activity에서 유저 아이디란에 세팅을 위한 함수
	 * @param strUserId
	 */
	public void setUserId(String strUserId)
	{
		m_editUserId.setText(strUserId);
		m_strUserId = strUserId;
	}
	/**
	 * 입력된 아이디를 리턴
	 * @return
	 */
	public String getUserId()
	{
		m_strUserId = m_editUserId.getText().toString();
		return m_strUserId ;
	}
	
	/**
	 * Activity에서 View의 유저 비밀번호란에 세팅을 위한 함수
	 * @param strUserPwd
	 */
	public void setUserPwd(String strUserPwd)
	{
		m_strUserPwd = strUserPwd;
		m_editUserPwd.setText(strUserPwd);
	}
	
	/**
	 *입력된 비밀번호를 리턴
	 * @return
	 */
	public String getUserPwd()
	{
		m_strUserPwd = m_editUserPwd.getText().toString();
		return m_strUserPwd;
	}
	
	/**
	 *Activity에서 View의 유저 비밀번호란에 세팅을 위한 함수
	 * @param strCertPwd
	 */
	public void setCertPwd(String strCertPwd)
	{
		m_editCertPwd.setText(strCertPwd);
	}
	
	/**
	 * 입력된 공인인증 비밀번호를 리턴
	 * @return
	 */
	public String getCertPwd()
	{
		m_strAuthPwd = m_editCertPwd.getText().toString();
		return m_strAuthPwd;
	}
}
