package com.openapi.sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.commexpert.CommExpertMng;
import com.commexpert.ExpertRealProc;
import com.commexpert.ExpertTranProc;
import com.truefriend.corelib.commexpert.intrf.IRealDataListener;
import com.truefriend.corelib.commexpert.intrf.ITranDataListener;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SampleDataView extends FrameLayout implements ITranDataListener, IRealDataListener, View.OnClickListener{
	
	ExpertTranProc	m_PriceTranProc = null;				//현재가 조회
	ExpertTranProc	m_TestTranProc = null;				//Test 조회

	ExpertRealProc  m_PriceRealProc = null;				//현재가 체결 실시간
	ExpertRealProc  m_PriceRealHogaProc = null;			//호가 실시간
	ExpertRealProc  m_TestRealProc = null;					// 테스트 실시간
	
	int 	m_nPrcieRqId =-1;									//현재가 TR ID
	int		m_nJangoRqId = -1;								//잔고 TR ID
	int		m_RqId = -1;											//TR ID
	
	String m_strCode = "";										//종목 코드
	String m_strCode2 = "005930";							//종목 코드
	String m_strUserID ="";										// 로그인 ID
	String m_strAccountCode = "01";							//계좌 상품 코드
	String m_strCurTR = "";									// 조회 중인 Test TR
	ArrayList<String> m_spinnerList = null;					//테스트 TR 목록
	
	private sampleActivity				m_actMain					= null;
	//컨트롤 ID
	public interface CtrlID
    {
        int     CTRL_ID_ACC = 400;
        int     CTRL_ID_CODE = 401;
        int 	CTRL_ID_PRICE_REQUEST = 402;
        int 	CTRL_ID_PRICE = 403;
        int		CTRL_ID_ORDER_VIEW = 404;
        int		CTRL_ID_ATTR = 405;
        int		CTRL_ID_REALPRICE1 = 406;
        int		CTRL_ID_REALPRICE2 = 407;
        int		CTRL_ID_PASS = 408;
        int 	CTRL_ID_TEST_REQUEST = 409;
        int		CTRL_ID_COMBO = 410;
        int		CTRL_ID_TEST_TEXT = 411;
    }	
	
	public SampleDataView(Context context) {
		super(context);
		
		//TR 초기화
		m_PriceTranProc = new ExpertTranProc(context);
		m_PriceTranProc.InitInstance(this);
		m_PriceTranProc.SetShowTrLog(true);
		
		m_TestTranProc = new ExpertTranProc(context);
		m_TestTranProc.InitInstance(this);	
		m_TestTranProc.SetShowTrLog(true);
		//실시간 초기화
		m_PriceRealProc = new ExpertRealProc(context);
		m_PriceRealProc.InitInstance(this);
		m_PriceRealProc.SetShowTrLog(true);
		
		m_PriceRealHogaProc = new ExpertRealProc(context);
		m_PriceRealHogaProc.InitInstance(this);
		m_PriceRealHogaProc.SetShowTrLog(true);
				
		m_TestRealProc = new ExpertRealProc(context);
		m_TestRealProc.InitInstance(this);
		m_TestRealProc.SetShowTrLog(true);
		
	}
	//화면 초기화
	public void initView( sampleActivity	actMain)
	{	
		m_actMain = actMain;
		
		LinearLayout layoutFrame = new LinearLayout(getContext());
		layoutFrame.setOrientation(LinearLayout.VERTICAL);
		//계좌번호
		TextView textAcc = makeTextView("계좌번호");
		EditText editAcc = makeEditView("");
		editAcc.setId(CtrlID.CTRL_ID_ACC);
		//비밀번호
		TextView textPass = makeTextView("비밀번호");
		textPass.setPadding(20, 0, 0, 0);
		EditText editPass = makeEditView("");
		editPass.setId(CtrlID.CTRL_ID_PASS);
		editPass.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_PASSWORD);
		editPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
		
		LinearLayout layoutAcc = new LinearLayout(getContext());
		layoutAcc.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutAcc.addView(textAcc, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutAcc.addView(editAcc, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutAcc.addView(textPass, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutAcc.addView(editPass, new LinearLayout.LayoutParams(200, LayoutParams.MATCH_PARENT));
		layoutAcc.setPadding(20, 20, 0, 10);
		layoutFrame.addView(layoutAcc, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 140));
		
		//종목코드
		TextView textCode = makeTextView("종목코드");
		EditText editCode = makeEditView("000660");
		editCode.setId(CtrlID.CTRL_ID_CODE);
		
		LinearLayout layoutCode = new LinearLayout(getContext());
		layoutCode.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutCode.addView(textCode, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutCode.addView(editCode, new LinearLayout.LayoutParams(600, LayoutParams.MATCH_PARENT));
		layoutCode.setPadding(20, 0, 100, 10);
		layoutFrame.addView(layoutCode, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));
		
		//현재가 조회 버튼	
		Button buttonPrice = makeCtrlButton(CtrlID.CTRL_ID_PRICE_REQUEST, "현재가 조회");
		buttonPrice.setOnClickListener(this);
		layoutFrame.addView(buttonPrice, new LinearLayout.LayoutParams(350, 140));
		//현재가
		TextView textPrice = makeTextView("현재가");
		EditText editPrice = makeEditView("");
		editPrice.setId(CtrlID.CTRL_ID_PRICE);
		//현재가 Attr
		TextView textAttr = makeTextView("Attr");
		textAttr.setPadding(50, 0, 0, 0);
		EditText editAttr = makeEditView("");
		editAttr.setId(CtrlID.CTRL_ID_ATTR);
		LinearLayout layoutPrice = new LinearLayout(getContext());
		layoutPrice.setOrientation(LinearLayout.HORIZONTAL);	
		layoutPrice.addView(textPrice, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutPrice.addView(editPrice, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutPrice.addView(textAttr, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutPrice.addView(editAttr, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutPrice.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutPrice, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));	
		//실시간 현재가
		TextView textReal = makeTextView("실시간");
		EditText editRealPrice1 = makeEditView("");
		editRealPrice1.setId(CtrlID.CTRL_ID_REALPRICE1);
		EditText editRealPrice2 = makeEditView("");
		editRealPrice2.setId(CtrlID.CTRL_ID_REALPRICE2);
		LinearLayout layoutReal = new LinearLayout(getContext());
		layoutReal.setOrientation(LinearLayout.HORIZONTAL);
		layoutReal.addView(textReal, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutReal.addView(editRealPrice1, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutReal.addView(editRealPrice2, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutReal.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutReal, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));		
		
		//TR Test 버튼
		Spinner spinner = new Spinner(getContext());	
		spinner.setId(CtrlID.CTRL_ID_COMBO);
		spinner.setGravity(Gravity.CENTER);
		
		m_spinnerList = new ArrayList<String>();
		m_spinnerList.add("scpc");
		m_spinnerList.add("scpd");
		m_spinnerList.add("scph");
		m_spinnerList.add("scpe");
		m_spinnerList.add("scpi");
		m_spinnerList.add("scpm");
		m_spinnerList.add("scap");
		m_spinnerList.add("sdoc");
		m_spinnerList.add("smcp");
		ArrayAdapter<String> arrayAdapterList = new ArrayAdapter<String>(getContext(),android.R.layout.simple_spinner_dropdown_item,m_spinnerList);
		arrayAdapterList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(arrayAdapterList);
		
		Button buttonTest = makeCtrlButton(CtrlID.CTRL_ID_TEST_REQUEST, "TEST");
		buttonTest.setOnClickListener(this);

		LinearLayout layoutTRTest = new LinearLayout(getContext());
		layoutTRTest.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutTRTest.addView(spinner, new LinearLayout.LayoutParams(450, LayoutParams.MATCH_PARENT));
		layoutTRTest.addView(buttonTest, new LinearLayout.LayoutParams(350, LayoutParams.MATCH_PARENT));
		layoutFrame.addView(layoutTRTest, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 130));
		addView(layoutFrame, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//Test Text
		TextView viewText = new TextView(getContext());
		viewText.setTextColor(Color.BLACK);
		viewText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		//viewText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		//EditText editTestText = makeEditView("");
		viewText.setId(CtrlID.CTRL_ID_TEST_TEXT);
		viewText.setBackgroundColor(Color.rgb( 255, 255, 255 ));
		LinearLayout layoutTestTextl = new LinearLayout(getContext());
		layoutTestTextl.setOrientation(LinearLayout.HORIZONTAL);
		layoutTestTextl.addView(viewText, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layoutTestTextl.setPadding(10, 0, 10, 50);
		layoutFrame.addView(layoutTestTextl, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 400));		

		//주문화면
		Button buttonOrderView = makeCtrlButton(CtrlID.CTRL_ID_ORDER_VIEW, "주문화면");
		buttonOrderView.setOnClickListener(this);
		layoutFrame.addView(buttonOrderView, new LinearLayout.LayoutParams(350, 140));
		
		//마스터 파일 종목 확인
//		ArrayList<ItemCode> arrItemKospiCode = CommExpertMng.getInstance().GetKospiCodeList();
//		for(int  i = 0; i< arrItemKospiCode.size(); i++)
//		{
//			ItemCode itme = arrItemKospiCode.get(i);
//			Log.d("KospiCodeList", String.format("종목코드:%s 종목명:%s", itme.getCode(),itme.getName() ));
//		}
//		
//		ArrayList<ItemCode> arrItemKosdaqCode = CommExpertMng.getInstance().GetKosdaqCodeList();
//		for(int  i = 0; i< arrItemKosdaqCode.size(); i++)
//		{
//			ItemCode itme = arrItemKosdaqCode.get(i);
//			Log.d("KosdaqCodeList", String.format("종목코드:%s 종목명:%s", itme.getCode(),itme.getName() ));
//		}
		
	}
	//로그인 ID 셋팅
	public void SetUserID(String strUserID)
	{
		m_strUserID = CommExpertMng.getInstance().GetLoginUserID();
	}
	
	public void releaseView()
	{
		removeAllViews();

		m_PriceTranProc.ClearInstance();
		m_PriceTranProc = null;
		
		m_TestTranProc.ClearInstance();
		m_TestTranProc = null;
	
		m_PriceRealProc.ClearInstance();
		m_PriceRealProc = null;
	
		m_PriceRealHogaProc.ClearInstance();
		m_PriceRealHogaProc = null;

		m_TestRealProc.ClearInstance();
		m_TestRealProc = null;
	}
	//계좌번호 셋팅
	public void SetAccount(String strAccount)
	{
		EditText view = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
		if(view != null)
		{
			view.setText(strAccount);
		}
	}
	//계좌 상품 코드 셋팅
	public void SetAccountCode(String strAccountCode)
	{
		m_strAccountCode = strAccountCode;
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
		//viewEdit.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		viewEdit.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		viewEdit.setText(strText);
		viewEdit.setSingleLine();
		viewEdit.setFocusableInTouchMode(true);
		viewEdit.setFocusable(true);
		viewEdit.setEllipsize(null);
       	viewEdit.setPrivateImeOptions("defaultInputmode=english;");
    	viewEdit.setImeOptions(EditorInfo.IME_ACTION_DONE);

       	viewEdit.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
 		
		return viewEdit;
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
	//Test TR 조회
	public int  RequestTestTR(String strTR)
	{
		int nRet = -1;
	
		EditText view = (EditText)findViewById(CtrlID.CTRL_ID_CODE);		
		m_strCode = view.getText().toString();
		//현재 조회 TR 이 동일하고 다음조회가 있다면 next 조회.
		if(m_strCurTR.equals(strTR) && m_TestTranProc.m_isMoreNextData)
		{
			nRet = m_TestTranProc.RequestNextData(strTR);
			return nRet;
		}		
		//현재 Test TR
		m_strCurTR = strTR;
		//Inblock 데이타 초기화
		m_TestTranProc.ClearInblockData();
		
		if(strTR.contains("scpc"))			//주식 현재가 체결
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);
			nRet = m_TestTranProc.RequestData(strTR);		
		}
		else if(strTR.contains("scpd"))		//주식 현재가 일자별
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);
			m_TestTranProc.SetSingleData(0,2, "D");
			m_TestTranProc.SetSingleData(0,3, "1");
			
			nRet = m_TestTranProc.RequestData(strTR);
		}
		else if(strTR.contains("scph"))		//주식 현재가 호가
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);

			//m_TestTranProc.SetSingleData(1,0, "J");
			//m_TestTranProc.SetSingleData(1,1, m_strCode2);
			
			nRet = m_TestTranProc.RequestData(strTR);	
		}
		else if(strTR.contains("scpe"))		//주식 현재가 예상체결
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);
			
			nRet = m_TestTranProc.RequestData(strTR);	
		}
		else if(strTR.contains("scpi"))		//주식 현재가 투자자
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);
			
			nRet = m_TestTranProc.RequestData(strTR);				
		}
		else if(strTR.contains("scpm"))	//주식 현재가 회원사
		{
			m_TestTranProc.SetSingleData(0,0, "J");
			m_TestTranProc.SetSingleData(0,1, m_strCode);
			
			nRet = m_TestTranProc.RequestData(strTR);							
		}
		else if(strTR.contains("scap"))		//주식현금매수가능조회
		{
			//계좌번호
			EditText viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return nRet;			
			m_TestTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_TestTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			EditText viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return nRet;
			String strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return nRet;
			}
			String strEncPass =  m_TestTranProc.GetEncryptPassword(strPass);
			m_TestTranProc.SetSingleData(0, 2, strEncPass);	
		    m_TestTranProc.SetSingleData(0, 3, m_strCode);	//상품번호
			
			//주문단가
			EditText viewPrice =  (EditText)findViewById(CtrlID.CTRL_ID_PRICE);
			String strOrderPrice = viewPrice.getText().toString();

			m_TestTranProc.SetSingleData(0, 4, strOrderPrice);	//주문단가
			m_TestTranProc.SetSingleData(0, 5, "00");			//주문구분
			m_TestTranProc.SetSingleData(0, 6, "N");				//CMA평가금액포함여부
			
			nRet = m_TestTranProc.RequestData(strTR);			
			
		}
		else if(strTR.contains("sdoc"))	//주식 일별 주문 체결 조회
		{
			//계좌번호
			EditText viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return nRet;			
			m_TestTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_TestTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			EditText viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return nRet;
			String strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return nRet;
			}		
			String strEncPass =  m_TestTranProc.GetEncryptPassword(strPass);
			m_TestTranProc.SetSingleData(0, 2, strEncPass);	
			long now = System.currentTimeMillis();
			Date date = new Date(now);
			SimpleDateFormat CurDate = new SimpleDateFormat("yyyyMMdd");
			String strDate = CurDate.format(date);
			
			Calendar calendar =  Calendar.getInstance();
			calendar.add(Calendar.DATE, -5);
			SimpleDateFormat CurDate2 = new SimpleDateFormat("yyyyMMdd");
			String strDate2 = CurDate2.format(calendar.getTime());
			
			m_TestTranProc.SetSingleData(0, 3, strDate2);			//조회시작일시
			m_TestTranProc.SetSingleData(0, 4, strDate);			//조회종료일시
			m_TestTranProc.SetSingleData(0, 5, "00");				//매도매수구분코드 00 전체
			m_TestTranProc.SetSingleData(0, 6, "00");				//조회구분   00역순		
			m_TestTranProc.SetSingleData(0, 7, m_strCode );		//상품번호
			m_TestTranProc.SetSingleData(0, 8, "00");				//체결구분  00전체
			m_TestTranProc.SetSingleData(0, 9, " ");					//주문채번지점번호
			m_TestTranProc.SetSingleData(0, 10," ");					//주문번호	
			m_TestTranProc.SetSingleData(0, 11, "00" );				//조회구분3     00 전체, 01 현금, 02 융자, 03 대출, 04 대주
			m_TestTranProc.SetSingleData(0, 12, " ");				//조회구분1    없음:전체  1 : ELW , 2 : 프리보드
			m_TestTranProc.SetSingleData(0, 13, " ");				//연속조회검색조건100
			m_TestTranProc.SetSingleData(0, 14, " ");				//연속조회키100
		
			nRet = m_TestTranProc.RequestData(strTR);			
			
		}		
		else if(strTR.contains("smcp"))	//주식 정정 취소 가능 주문 조회
		{
			//계좌번호
			EditText viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return nRet;			
			m_TestTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_TestTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			EditText viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return nRet;
			String strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return nRet;
			}			
			String strEncPass =  m_TestTranProc.GetEncryptPassword(strPass);
			m_TestTranProc.SetSingleData(0, 2, strEncPass);	
			m_TestTranProc.SetSingleData(0, 3, " ");				//연속조회검색조건100
			m_TestTranProc.SetSingleData(0, 4, " ");				//연속조회키100		
			m_TestTranProc.SetSingleData(0, 5, "0");				//조회구분1 0-주문순, 1-종목순 
			
			nRet = m_TestTranProc.RequestData(strTR);			
		}		
		
		return nRet;
	}
	//Test TR 처리
	public void ProcessTRTest(String strTR)
	{
		String strText = "";
		TextView viewText = (TextView)findViewById(CtrlID.CTRL_ID_TEST_TEXT);
		if(viewText != null) 
			viewText.setText(strText);	

		if(strTR.contains("scpc"))			//주식 현재가 체결
		{
			String strTime = " ", strPrice = "0" ,strRate = "0";
			int nPriceAttr = -1 ,nRateAttr = -1;
			int nCount =m_TestTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				strTime = m_TestTranProc.GetMultiData(0,0, i); 		//GetSingleData(0,0);//주식 체결 시간
				strPrice = m_TestTranProc.GetMultiData(0,1,i);			//현재가
				nPriceAttr = m_TestTranProc.GetAttrMultiData(0,1,i);	//현재가 속성
				strRate = m_TestTranProc.GetMultiData(0,6,i);			//전일 대비율,
				nRateAttr = m_TestTranProc.GetAttrMultiData(0,6,i);	//전일비 속성
							
				Log.d("scpc:주식 현재가 체결", String.format("시간:%s 현재가:%s(Attr:%d)  전일대비율:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate),nRateAttr ));	
			}

			Toast.makeText( getContext(),String.format("scpc:주식 현재가 체결 : 시간:%s 현재가:%s(Attr:%d)  전일대비율:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate),nRateAttr ), Toast.LENGTH_SHORT ).show();
			
			if(viewText != null) 
				viewText.setText(String.format("scpc:주식 현재가 체결 : 시간:%s 현재가:%s(Attr:%d)  전일대비율:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate),nRateAttr ));	
		}
		else if(strTR.contains("scpd"))		//주식 현재가 일자별
		{
			String strTime = " " ,strPrice = "0", strRate = "0";
			int nPriceAttr = -1;
			int nCount =m_TestTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				strTime = m_TestTranProc.GetMultiData(0,0, i); 		//주식영업일자
				strPrice = m_TestTranProc.GetMultiData(0,1,i);			//주식 시가
				nPriceAttr = m_TestTranProc.GetAttrMultiData(0,1,i);	//시가 속성
				strRate = m_TestTranProc.GetMultiData(0,13,i);			//누적분할비율

				Log.d("scpd:주식 현재가 체결", String.format("일자:%s 시가:%s(Attr:%d)  누적분할비율:%s", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate) ));	
			}
			Toast.makeText( getContext(),String.format("scpd:주식 현재가 체결 : 일자:%s 시가:%s(Attr:%d)  누적분할비율:%s", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate) ), Toast.LENGTH_SHORT ).show();

			if(viewText != null) 
				viewText.setText(String.format("scpd:주식 현재가 체결 : 일자:%s 시가:%s(Attr:%d)  누적분할비율:%s", strTime, Integer.parseInt( strPrice ), nPriceAttr, Double.parseDouble(strRate) ));	
			
		}
		else if(strTR.contains("scph"))		//주식 현재가 호가
		{
			//Block1
			String strTime = m_TestTranProc.GetSingleData(0,0); 				//시간
			String strAsk = m_TestTranProc.GetSingleData(0,1);				//매도호가
			int nAskAttr = m_TestTranProc.GetAttrSingleData(0,1);			//매도호가 속성
			String strBid = m_TestTranProc.GetSingleData(0,11);				//매수호가
			int nBidAttr = m_TestTranProc.GetAttrSingleData(0,11);			//매수호가 속성
	
			Toast.makeText( getContext(),String.format("scph:주식 호가 : 시간:%s 매도호가:%s(Attr:%d)  매수호가:%s(Attr:%d)", strTime, Integer.parseInt( strAsk ), nAskAttr,  Integer.parseInt(strBid ),nBidAttr ), Toast.LENGTH_SHORT ).show();
			
			Log.d("scph:주식 호가", String.format("시간:%s 매도호가:%s(Attr:%d)  매수호가:%s(Attr:%d)", strTime, Integer.parseInt( strAsk ), nAskAttr,  Integer.parseInt(strBid ),nBidAttr ));	
			
			if(viewText != null) 
				viewText.setText(String.format("scph:주식 호가 : 시간:%s 매도호가:%s(Attr:%d)  매수호가:%s(Attr:%d)", strTime, Integer.parseInt( strAsk ), nAskAttr,  Integer.parseInt(strBid ),nBidAttr ));	
			
//			//Block2
//			String strGCode = m_TestTranProc.GetSingleData(1,0);			 //예상 장운영 구분 코드
//			String strPrice= m_TestTranProc.GetSingleData(1,1);				 //현재가
//			int nPriceAttr = m_TestTranProc.GetAttrSingleData(1,1);
//			String strItemCode = m_TestTranProc.GetSingleData(1,11);		 //주식 단축 종목코드	
//			Log.d("scph:주식 호가2", String.format("예상 장운영 구분 코드:%s 현재가:%s(Attr:%d) 종목코드:%s", strGCode, Integer.parseInt( strPrice ), nPriceAttr,strItemCode));				
		}
		else if(strTR.contains("scpe"))		//주식 현재가 예상체결
		{
			String strGCode = m_TestTranProc.GetSingleData(0,0); 			//예상 장운영 구분 코드
			String strPrice= m_TestTranProc.GetSingleData(0,1);				//현재가
			int nPriceAttr = m_TestTranProc.GetAttrSingleData(0,1);			//현재가 속성
			String strItemCode = m_TestTranProc.GetSingleData(0,11);		//주식 단축 종목코드	

			Toast.makeText( getContext(),String.format("scpe:주식 현재가 예상체결 : 예상 장운영 구분 코드:%s 현재가:%s(Attr:%d) 종목코드:%s", strGCode, Integer.parseInt( strPrice ), nPriceAttr,strItemCode ), Toast.LENGTH_SHORT ).show();
		
			Log.d("scpe:주식 현재가 예상체결", String.format("예상 장운영 구분 코드:%s 현재가:%s(Attr:%d) 종목코드:%s", strGCode, Integer.parseInt( strPrice ), nPriceAttr,strItemCode));				

			if(viewText != null) 
				viewText.setText(String.format("scpe:주식 현재가 예상체결 : 예상 장운영 구분 코드:%s 현재가:%s(Attr:%d) 종목코드:%s", strGCode, Integer.parseInt( strPrice ), nPriceAttr,strItemCode));	
	
		}
		else if(strTR.contains("scpi"))		//주식 현재가 투자자
		{
			String strTime = " " , strPrice = "0" ,strAmt = "0";
			int nPriceAttr = -1,nAmtAttr =-1;
			int nCount =m_TestTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				strTime = m_TestTranProc.GetMultiData(0,0, i); 		//주식영업일자
				strPrice = m_TestTranProc.GetMultiData(0,1,i);			//주식 종가
				nPriceAttr = m_TestTranProc.GetAttrMultiData(0,1,i);	//주식 종가 속성
				strAmt = m_TestTranProc.GetMultiData(0,21,i);			//기관계 매도 거래 대금
				nAmtAttr = m_TestTranProc.GetAttrMultiData(0,21,i);

				Log.d("scpi:주식 현재가 투자자", String.format("일자:%s 종가:%s(Attr:%d) 기관계 매도 거래 대금:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Integer.parseInt( strAmt ), nAmtAttr));
			}

			Toast.makeText( getContext(),String.format("scpi:주식 현재가 투자자 : 일자:%s 종가:%s(Attr:%d) 기관계 매도 거래 대금:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Integer.parseInt( strAmt ), nAmtAttr), Toast.LENGTH_SHORT ).show();
			
			m_TestRealProc.ReleaseReal("sm_r", m_strCode);	//기존 실시간 해제
			//지수 업종 체결 실시간
			m_TestRealProc.RequestReal("juc_r", "0001");		//코스피 업종 종합		

			if(viewText != null) 
				viewText.setText(String.format("scpi:주식 현재가 투자자 : 일자:%s 종가:%s(Attr:%d) 기관계 매도 거래 대금:%s(Attr:%d)", strTime, Integer.parseInt( strPrice ), nPriceAttr, Integer.parseInt( strAmt ), nAmtAttr));	
			
		}
		else if(strTR.contains("scpm"))	//주식 현재가 회원사
		{
			String strMemNo = m_TestTranProc.GetSingleData(0,0); 			//매도 회원사 번호1
			String strMemName= m_TestTranProc.GetSingleData(0,5);		//매도 회원사 명1
		
			String strQty= m_TestTranProc.GetSingleData(0,50);				//외국계 총 매도 수량
			int nQtyAttr = m_TestTranProc.GetAttrSingleData(0,50);			//외국계 총 매도 수량 속성
			
			String strQty2 = m_TestTranProc.GetSingleData(0,67); 			//누적 거래량

			Toast.makeText( getContext(),String.format("scpm:주식 현재가 회원사 : 회원사 번호:%s 회원사 명:%s 외국계 총 매도 수량:%s(Attr:%d) 누적 거래량:%s", strMemNo, strMemName,Integer.parseInt( strQty ), nQtyAttr ,Integer.parseInt( strQty2 )), Toast.LENGTH_SHORT ).show();
			
			Log.d("scpm:주식 현재가 회원사", String.format("회원사 번호:%s 회원사 명:%s 외국계 총 매도 수량:%s(Attr:%d) 누적 거래량:%s", strMemNo, strMemName,Integer.parseInt( strQty ), nQtyAttr ,Integer.parseInt( strQty2 )));				
		
			m_TestRealProc.ReleaseReal("juc_r", "0001"); //기존 실시간 해제
			//주식 거래원 실시간
			m_TestRealProc.RequestReal("sm_r", m_strCode);

			if(viewText != null) 
				viewText.setText(String.format("scpm:주식 현재가 회원사 : 회원사 번호:%s 회원사 명:%s 외국계 총 매도 수량:%s(Attr:%d) 누적 거래량:%s", strMemNo, strMemName,Integer.parseInt( strQty ), nQtyAttr ,Integer.parseInt( strQty2 )));	

		}
		else if(strTR.contains("scap"))		//주식현금매수가능조회
		{
			String strCash = m_TestTranProc.GetSingleData(0,0); 				//주문가능현금
			String strQty= m_TestTranProc.GetSingleData(0,8);					//최대매수수량	
			String strAmt = m_TestTranProc.GetSingleData(0,9);				//CMA평가금액
			
			Toast.makeText( getContext(),String.format("scap:주식현금매수가능조회 : 주문가능현금:%s 최대매수수량:%s 평가금액:%s", Integer.parseInt( strCash ), Integer.parseInt( strQty ) ,Integer.parseInt( strAmt )), Toast.LENGTH_SHORT ).show();

			Log.d("scap:주식현금매수가능조회", String.format("주문가능현금:%s 최대매수수량:%s 평가금액:%s", Integer.parseInt( strCash ), Integer.parseInt( strQty ) ,Integer.parseInt( strAmt )));				
			
			if(viewText != null) 
				viewText.setText(String.format("scap:주식현금매수가능조회 : 주문가능현금:%s 최대매수수량:%s 평가금액:%s", Integer.parseInt( strCash ), Integer.parseInt( strQty ) ,Integer.parseInt( strAmt )));	
		}
		else if(strTR.contains("sdoc"))	//주식 일별 주문 체결 조회
		{
			int nCount =m_TestTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				String strDate = m_TestTranProc.GetMultiData(0,0, i); 					//주문일자
				String strOrderNumber = m_TestTranProc.GetMultiData(0,2,i);		//주문번호
				String strSellBuy = m_TestTranProc.GetMultiData(0,6,i);				//매도매수구분명
				String strCode= m_TestTranProc.GetMultiData(0,7,i);					//상품번호
				String strNo = m_TestTranProc.GetMultiData(0,31,i);					//주문조직번호
			
				Log.d("sdoc:주식 일별 주문 체결 조회", String.format("주문일자:%s 주문번호:%s 매도매수구분:%s 상품번호:%s 주문조직번호:%s", strDate, strOrderNumber, strSellBuy, strCode,strNo ));
			}
		
			int nQty = 0;
			String strQty = m_TestTranProc.GetSingleData(1,0); 		//총주문수량
			if(!strQty.isEmpty())		nQty =  Integer.parseInt( strQty );

			int nQty2 = 0;
			String strQty2 = m_TestTranProc.GetSingleData(1,1);		//총체결수량
			if(!strQty2.isEmpty())		nQty2 =  Integer.parseInt( strQty2 );

			double nPrice = 0;
			String strPrice = m_TestTranProc.GetSingleData(1,2);		//매입평균가격
			if(!strPrice.isEmpty())	nPrice =  Double.parseDouble( strPrice );
				
			Toast.makeText( getContext(),String.format("sdoc:주식 일별 주문 체결 조회 : 총주문수량:%s 총체결수량:%s 매입평균가격:%s ",nQty,  nQty2 ,nPrice), Toast.LENGTH_SHORT ).show();

			Log.d("sdoc:주식 일별 주문 체결 조회", String.format("총주문수량:%s 총체결수량:%s 매입평균가격:%s ", nQty, nQty2 ,nPrice ));

			if(viewText != null) 
				viewText.setText(String.format("sdoc:주식 일별 주문 체결 조회 : 총주문수량:%s 총체결수량:%s 매입평균가격:%s ", nQty, nQty2 ,nPrice ));	
		}		
		else if(strTR.contains("smcp"))	//주식 정정 취소 가능 주문 조회
		{
			String strNo = " ", strOrderNumber = " ", strSellBuy =" ", strCode = " ", strName = " ";
			int nCount =m_TestTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				strNo = m_TestTranProc.GetMultiData(0,0, i); 					//주문채번지점번호
				strOrderNumber = m_TestTranProc.GetMultiData(0,1,i);		//주문번호
				strSellBuy = m_TestTranProc.GetMultiData(0,3,i);				//주문구분명
				strCode= m_TestTranProc.GetMultiData(0,4,i);					//상품번호
				strName = m_TestTranProc.GetMultiData(0,6,i);					//정정취소구분명
			
				Log.d("smcp:주식 정정 취소 가능 주문 조회", String.format("주문채번지점번호:%s 주문번호:%s 주문구분명:%s 상품번호:%s  %s", strNo, strOrderNumber, strSellBuy, strCode,strName ));
			}

			Toast.makeText( getContext(),String.format("smcp:주식 정정 취소 가능 주문 조회 : 주문채번지점번호:%s 주문번호:%s 주문구분명:%s 상품번호:%s  %s", strNo, strOrderNumber, strSellBuy, strCode,strName), Toast.LENGTH_SHORT ).show();			

			if(viewText != null) 
				viewText.setText(String.format("smcp:주식 정정 취소 가능 주문 조회 : 주문채번지점번호:%s 주문번호:%s 주문구분명:%s 상품번호:%s  %s", strNo, strOrderNumber, strSellBuy, strCode,strName ));	
		}			
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int nCtrlId = v.getId();
		switch(nCtrlId)
		{
		case CtrlID.CTRL_ID_PRICE_REQUEST : //현재가 조회
			//이전 실시간 해지
			m_PriceRealHogaProc.ReleaseReal("sh_r", m_strCode);
			m_PriceRealHogaProc.ReleaseReal("sc_r", m_strCode);

			EditText view = (EditText)findViewById(CtrlID.CTRL_ID_CODE);
			if(view == null) return;		
			m_strCode = view.getText().toString();
			m_PriceTranProc.ClearInblockData();
			m_PriceTranProc.SetSingleData(0,0, "J");
			m_PriceTranProc.SetSingleData(0,1, m_strCode);
			m_nPrcieRqId = m_PriceTranProc.RequestData("scp");
			break;
		

		case CtrlID.CTRL_ID_ORDER_VIEW:	//Order View
			//주문화면 전환
			m_actMain.sendMessage(1 , 0, 0 );			
	
			break;
		case CtrlID.CTRL_ID_TEST_REQUEST : //TR Test		
			Spinner spinner =  (Spinner)findViewById(CtrlID.CTRL_ID_COMBO);		
			int nPos= spinner.getSelectedItemPosition();
			if(nPos >= 0 &&  nPos < m_spinnerList.size())
			{
				String strTR = m_spinnerList.get(nPos);
				if(!strTR.isEmpty())
					m_RqId = RequestTestTR(strTR) ; 
			}			
			//"scpc" : 주식 현재가 체결			
			//"scpd" : 주식 현재가 일자별
			//"scph" : 주식 현재가 호가
			//"scpe" : 주식 현재가 예상체결
			//"scpi" : 주식 현재가 투자자
			//"scpm" : 주식 현재가 회원사
			//"scap" : 주식현금매수가능조회
			//"sdoc" : 주식 일별 주문 체결 조회
			//"smcp" : 주식 정정 취소 가능 주문 조회
		}	
	}
	@Override
	public void onTranDataReceived(String sTranID, int nRqId) {
		// TODO Auto-generated method stub
		//조회 데이터 받아서 처리
		if(m_nPrcieRqId == nRqId)		//현재가 조회
		{
			String strPrice = m_PriceTranProc.GetSingleData(0,11);						//현재가
			int nAttr = m_PriceTranProc.GetAttrSingleData(0,11);							//현재가 속성
			
			EditText viewPrice = (EditText)findViewById(CtrlID.CTRL_ID_PRICE);
			if(viewPrice != null) 
				viewPrice.setText(String.format("%d", Integer.parseInt( strPrice )));	

			EditText viewAttr= (EditText)findViewById(CtrlID.CTRL_ID_ATTR);
			if(viewAttr != null && nAttr > 0) 
				viewAttr.setText(String.format("%d",nAttr));	
			
			if(nAttr == 1)		//하락
				viewPrice.setTextColor(Color.rgb(0, 0, 255));
			else if(nAttr == 4)	//상승
				viewPrice.setTextColor(Color.rgb(255, 0, 0));
			else if(nAttr == 5) //보합
				viewPrice.setTextColor(Color.rgb(0, 255, 0));
			else
				viewPrice.setTextColor(Color.rgb(0, 0, 0));
			
			//체결 실시간 등록 - 주식 종목체결 실시간
			m_PriceRealProc.RequestReal("sc_r", m_strCode);
			// 호가 실시간 등록
			m_PriceRealHogaProc.RequestReal("sh_r", m_strCode);
			m_PriceRealHogaProc.RequestReal("sh_r", m_strCode2);
		}

		else if(m_RqId == nRqId)
		{
			ProcessTRTest(sTranID);
		}
	}
	@Override
	public void onTranMessageReceived(int nRqId, String strMsgCode,
			String strErrorType, String strMessage) {
		// TODO Auto-generated method stub
		Log.e("onTranMessageReceived", String.format("MsgCode:%s ErrorType:%s %s",  strMsgCode ,  strErrorType  , strMessage));		
		Toast.makeText( getContext(),String.format("%s", strMessage), Toast.LENGTH_SHORT ).show();
	}
	@Override
	public void onTranTimeout(int nRqId) {
		// TODO Auto-generated method stub
		Log.e("onTranTimeout", String.format("RqId:%d ",  nRqId));		
	}
	@Override
	public void onRealDataReceived(String strServiceId) {
		//주식 종목체결 실시간
		if(strServiceId == "sc_r")
		{
			String strcode = m_PriceRealProc.GetRealData(0,0).trim();				//현재가
			//종목코드 비교
			if( strcode.equals(m_strCode) )
			{
				String strPrice = m_PriceRealProc.GetRealData(0,2);
				int nAttr = m_PriceRealProc.GetAttrRealData(0,2);

				EditText viewPrice = (EditText)findViewById(CtrlID.CTRL_ID_PRICE);
				if(viewPrice != null) 
					viewPrice.setText(String.format("%s", Integer.parseInt( strPrice )));	

				EditText viewAttr= (EditText)findViewById(CtrlID.CTRL_ID_ATTR);
				if(viewAttr != null && nAttr > 0) 
					viewAttr.setText(String.format("%d",nAttr));	
									
				if(nAttr == 1)
					viewPrice.setTextColor(Color.rgb(0, 0, 255));
				else if(nAttr == 4)
					viewPrice.setTextColor(Color.rgb(255, 0, 0));
				else if(nAttr == 5)
					viewPrice.setTextColor(Color.rgb(0, 255, 0));
				else
					viewPrice.setTextColor(Color.rgb(0, 0, 0));
			}
		}
		//주식 종목호가 실시간
		else if(strServiceId == "sh_r")		
		{
			String strcode = m_PriceRealHogaProc.GetRealData(0,0).trim();
			//종목코드 비교
			if( strcode.equals(m_strCode) )
			{
				String strPrice = m_PriceRealHogaProc.GetRealData(0,3);	//1호가
				int nAttr = m_PriceRealHogaProc.GetAttrRealData(0,3);

				EditText viewRealPrice = (EditText)findViewById(CtrlID.CTRL_ID_REALPRICE1);
				if(viewRealPrice != null) 
					viewRealPrice.setText(String.format("%d", Integer.parseInt( strPrice )));	
				
				if(nAttr == 1)
					viewRealPrice.setTextColor(Color.rgb(0, 0, 255));
				else if(nAttr == 4)
					viewRealPrice.setTextColor(Color.rgb(255, 0, 0));
				else if(nAttr == 5)
					viewRealPrice.setTextColor(Color.rgb(0, 255, 0));
				else
					viewRealPrice.setTextColor(Color.rgb(0, 0, 0));
			}
			else if( strcode.equals(m_strCode2) )
			{
				String strPrice = m_PriceRealHogaProc.GetRealData(0,3);	//1호가
				int nAttr = m_PriceRealHogaProc.GetAttrRealData(0,3);

				EditText viewPrice2 = (EditText)findViewById(CtrlID.CTRL_ID_REALPRICE2);
				if(viewPrice2 != null) 
					viewPrice2.setText(String.format("%d", Integer.parseInt( strPrice )));	

				if(nAttr == 1)
					viewPrice2.setTextColor(Color.rgb(0, 0, 255));
				else if(nAttr == 4)
					viewPrice2.setTextColor(Color.rgb(255, 0, 0));
				else if(nAttr == 5)
					viewPrice2.setTextColor(Color.rgb(0, 255, 0));
				else
					viewPrice2.setTextColor(Color.rgb(0, 0, 0));
			}
		}	
		//주식 거래원
		else if(strServiceId == "sm_r")		
		{
			String strMemNo = m_TestRealProc.GetRealData(0,1); 	//매도 회원사 명1
			String strQty= m_TestRealProc.GetRealData(0,11);			//총 매도 수량1
		
			String strTotalQty= m_TestRealProc.GetRealData(0,61);	//외국계 총 매도 수량
			int nQtyAttr = m_TestRealProc.GetAttrRealData(0,61);
			String strRate= m_TestRealProc.GetRealData(0,67);	//외국계 매수2 비중
			Log.d("==:주식거래원==", String.format("회원사 명:%s 총 매도 수량:%s 외국계 총 매도 수량:%s(Attr:%d) 매수2 비중%s", strMemNo, Integer.parseInt( strQty ),Integer.parseInt( strTotalQty ),nQtyAttr ,Double.parseDouble(strRate)));				
		}
		//지수 업종 체결
		else if(strServiceId == "juc_r")		
		{
			String strCode = m_TestRealProc.GetRealData(0,0); 			//업종 구분 코드
			String strTime= m_TestRealProc.GetRealData(0,1);			//영업 시간 
		
			String strPrice= m_TestRealProc.GetRealData(0,2);					//현재가 지수
			int nQtyAttr = m_TestRealProc.GetAttrRealData(0,2);
			
			Log.d("==:지수 업종 체결==", String.format("업종 구분 코드:%s영업 시간 :%s 현재가 지수:%s(Attr:%d)", strCode, strTime ,Double.parseDouble(strPrice),nQtyAttr ));				
		}		
	}

	
}
