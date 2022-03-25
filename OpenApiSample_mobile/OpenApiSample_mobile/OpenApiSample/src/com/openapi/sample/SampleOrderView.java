package com.openapi.sample;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.commexpert.CommExpertMng;
import com.commexpert.ExpertRealProc;
import com.commexpert.ExpertTranProc;
import com.truefriend.corelib.commexpert.intrf.IRealDataListener;
import com.truefriend.corelib.commexpert.intrf.ITranDataListener;
import com.truefriend.corelib.util.ConfigUtil;

public class SampleOrderView extends FrameLayout implements ITranDataListener, IRealDataListener, View.OnClickListener {
	ExpertTranProc	m_JangoTranProc = null;				//잔고 조회
	ExpertTranProc	m_OrderTranProc = null;				//주문
	ExpertTranProc	m_OrderListTranProc = null;			//주문내역 조회

	ExpertRealProc  m_OrderRealProc = null;				//주문체결 실시간
	
	int		m_nJangoRqId = -1;								//잔고 TR ID
	int		m_nOrderRqId = -1;								//주문 TR ID
	int		m_nOrderListRqId = -1;							//주문내역 TR ID
	
	String m_strCode = "";										//종목 코드
	String m_strCode2 = "005930";							//종목 코드
	String m_strUserID ="";										// 로그인 ID
	String m_strAccountCode = "01";							//계좌 상품 코드
	String m_strCurTR = "";									// 조회 중인 Test TR
	
	private sampleActivity				m_actMain					= null;
	
	//컨트롤 ID
	public interface CtrlID
    {
        int     CTRL_ID_ACC = 400;
        int     CTRL_ID_CODE = 401;
        int		CTRL_ID_ORDER_REQUEST = 402;
        int		CTRL_ID_ORDER_PRICE = 403;
        int		CTRL_ID_ORDER_QTY = 404;
        int		CTRL_ID_JANGO_REQUEST = 405;
        int		CTRL_ID_J_CODENAME1 = 406;
        int		CTRL_ID_J_QTY1 = 407;
        int		CTRL_ID_J_CODENAME2 = 408;
        int		CTRL_ID_J_QTY2 = 409;
        int		CTRL_ID_REALPRICE1 = 410;
        int		CTRL_ID_REALPRICE2 = 411;
        int		CTRL_ID_PASS = 412;
        int		CTRL_ID_J_TOTAL1 = 413;
        int		CTRL_ID_J_TOTAL2 = 414;
        int 	CTRL_ID_DATA_VIEW = 415;
        int		CTRL_ID_ORDER_REQUEST2 = 416;
        int		CTRL_ID_ORDER_REQUEST3 = 417;
        int		CTRL_ID_ORDER_REQUEST4 = 418;
        int		CTRL_ID_ORDER_NO = 419;
        int		CTRL_ID_ORDER_NUMBER = 420;
        int		CTRL_ID_ORDER_LIST = 421;
        int 	CTRL_ID_ORDERLIST_NO1 = 422;
        int 	CTRL_ID_ORDERLIST_NO2 = 423;
    }	
	
	public SampleOrderView(Context context) {
		super(context);
		//TR 초기화
		m_OrderTranProc = new ExpertTranProc(context);
		m_OrderTranProc.InitInstance(this);	
		m_OrderTranProc.SetShowTrLog(true);
	
		m_JangoTranProc = new ExpertTranProc(context);
		m_JangoTranProc.InitInstance(this);	
		m_JangoTranProc.SetShowTrLog(true);
		
		m_OrderListTranProc = new ExpertTranProc(context);
		m_OrderListTranProc.InitInstance(this);	
		m_OrderListTranProc.SetShowTrLog(true);
		
		//실시간 초기화
		m_OrderRealProc = new ExpertRealProc(context);
		m_OrderRealProc.InitInstance(this);
		m_OrderRealProc.SetShowTrLog(true);
				
	}
	//화면 초기화
	public void initView(sampleActivity	actMain)
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
			
		//주문 버튼	
		Button buttonOrder = makeCtrlButton(CtrlID.CTRL_ID_ORDER_REQUEST, "매수");
		buttonOrder.setOnClickListener(this);			
		Button buttonOrder2 = makeCtrlButton(CtrlID.CTRL_ID_ORDER_REQUEST2, "매도");
		buttonOrder2.setOnClickListener(this);	
		Button buttonOrder3 = makeCtrlButton(CtrlID.CTRL_ID_ORDER_REQUEST3, "정정");
		buttonOrder3.setOnClickListener(this);
		Button buttonOrder4 = makeCtrlButton(CtrlID.CTRL_ID_ORDER_REQUEST4, "취소");
		buttonOrder4.setOnClickListener(this);
		LinearLayout layoutOrderBtn = new LinearLayout(getContext());
		layoutOrderBtn.setOrientation(LinearLayout.HORIZONTAL);
		layoutOrderBtn.addView(buttonOrder, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderBtn.addView(buttonOrder2, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderBtn.addView(buttonOrder3, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderBtn.addView(buttonOrder4, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		
		layoutOrderBtn.setPadding(10, 10, 10, 10);
		layoutFrame.addView(layoutOrderBtn, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 150));
			
		//주문수량/가격
		TextView textOrderPrice = makeTextView("주문가격");
		EditText editOrderPrice = makeEditView("");
		editOrderPrice.setId(CtrlID.CTRL_ID_ORDER_PRICE);
		TextView textOrderQty = makeTextView("주문수량");
		textOrderQty.setPadding(50, 0, 0, 0);
		EditText editOrderQty = makeEditView("");
		editOrderQty.setId(CtrlID.CTRL_ID_ORDER_QTY);
		LinearLayout layoutOrder = new LinearLayout(getContext());
		layoutOrder.setOrientation(LinearLayout.HORIZONTAL);	
		layoutOrder.addView(textOrderPrice, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrder.addView(editOrderPrice, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrder.addView(textOrderQty, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutOrder.addView(editOrderQty, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrder.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutOrder, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));
		//원주문번호
		TextView textOrderNo = makeTextView("조직번호");
		EditText editOrderNo = makeEditView("");
		editOrderNo.setId(CtrlID.CTRL_ID_ORDER_NO);
		TextView textOrderNumber = makeTextView("주문번호");
		textOrderNumber.setPadding(50, 0, 0, 0);
		EditText editOrderNumber = makeEditView("");
		editOrderNumber.setId(CtrlID.CTRL_ID_ORDER_NUMBER);
		LinearLayout layoutOrderNumber  = new LinearLayout(getContext());
		layoutOrderNumber.setOrientation(LinearLayout.HORIZONTAL);	
		layoutOrderNumber.addView(textOrderNo, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderNumber.addView(editOrderNo, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderNumber.addView(textOrderNumber, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutOrderNumber.addView(editOrderNumber, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutOrderNumber.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutOrderNumber, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));
			
		//주문내역 버튼
		Button buttonOrderList = makeCtrlButton(CtrlID.CTRL_ID_ORDER_LIST, "주문내역");
		buttonOrderList.setOnClickListener(this);
		layoutFrame.addView(buttonOrderList, new LinearLayout.LayoutParams(350, 140));
		
		//주문내역 1
		EditText editOrderList1 = makeEditView("");
		editOrderList1.setId(CtrlID.CTRL_ID_ORDERLIST_NO1);
			
		LinearLayout layoutOrderList1 = new LinearLayout(getContext());
		layoutOrderList1.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutOrderList1.addView(editOrderList1, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layoutOrderList1.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutOrderList1, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 130));
		
		//주문내역 2
		EditText editOrderList2 = makeEditView("");
		editOrderList2.setId(CtrlID.CTRL_ID_ORDERLIST_NO2);
	
		LinearLayout layoutOrderList2 = new LinearLayout(getContext());
		layoutOrderList2.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutOrderList2.addView(editOrderList2, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		layoutOrderList2.setPadding(10, 0, 10, 20);
		layoutFrame.addView(layoutOrderList2, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 130));
				
		//잔고 버튼
		Button buttonJango = makeCtrlButton(CtrlID.CTRL_ID_JANGO_REQUEST, "잔고조회");
		buttonJango.setOnClickListener(this);
		layoutFrame.addView(buttonJango, new LinearLayout.LayoutParams(350, 140));
		
		//예수금
		TextView textTotal1 = makeTextView("예수금");
		EditText editJTotal1 = makeEditView("");
		editJTotal1.setId(CtrlID.CTRL_ID_J_TOTAL1);
		//평가금액	
		TextView textTotal2 = makeTextView("평가금액");
		textTotal2.setPadding(20, 0, 0, 0);
		EditText editJTotal2 = makeEditView("");
		editJTotal2.setId(CtrlID.CTRL_ID_J_TOTAL2);	
		LinearLayout layoutTotal = new LinearLayout(getContext());
		layoutTotal.setOrientation(LinearLayout.HORIZONTAL);
		layoutTotal.addView(textTotal1, new LinearLayout.LayoutParams(200, LayoutParams.MATCH_PARENT));
		layoutTotal.addView(editJTotal1, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutTotal.addView(textTotal2, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutTotal.addView(editJTotal2, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutTotal.setPadding(10, 10, 10, 10);
		layoutFrame.addView(layoutTotal, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 130));
			
		//잔고1
		TextView textJCodeName1 = makeTextView("종목명");
		EditText editJCodeName1 = makeEditView("");
		editJCodeName1.setId(CtrlID.CTRL_ID_J_CODENAME1);
	
		TextView textJQty1 = makeTextView("잔고수량");
		textJQty1.setPadding(50, 0, 0, 0);
		EditText editJQty1 = makeEditView("");
		editJQty1.setId(CtrlID.CTRL_ID_J_QTY1);
		
		LinearLayout layoutJango1 = new LinearLayout(getContext());
		layoutJango1.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutJango1.addView(textJCodeName1, new LinearLayout.LayoutParams(200, LayoutParams.MATCH_PARENT));
		layoutJango1.addView(editJCodeName1, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutJango1.addView(textJQty1, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutJango1.addView(editJQty1, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutJango1.setPadding(10, 0, 10, 10);
		layoutFrame.addView(layoutJango1, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));
		
		//잔고2
		TextView textJCodeName2 = makeTextView("종목명");
		EditText editJCodeName2 = makeEditView("");
		editJCodeName2.setId(CtrlID.CTRL_ID_J_CODENAME2);
	
		TextView textJQty2 = makeTextView("잔고수량");
		textJQty2.setPadding(50, 0, 0, 0);
		EditText editJQty2 = makeEditView("");
		editJQty2.setId(CtrlID.CTRL_ID_J_QTY2);
		
		LinearLayout layoutJango2 = new LinearLayout(getContext());
		layoutJango2.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutJango2.addView(textJCodeName2, new LinearLayout.LayoutParams(200, LayoutParams.MATCH_PARENT));
		layoutJango2.addView(editJCodeName2, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutJango2.addView(textJQty2, new LinearLayout.LayoutParams(300, LayoutParams.MATCH_PARENT));
		layoutJango2.addView(editJQty2, new LinearLayout.LayoutParams(250, LayoutParams.MATCH_PARENT));
		layoutJango2.setPadding(10, 0, 10, 20);
		layoutFrame.addView(layoutJango2, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 120));
		//조회 화면
		Button buttonDataview = makeCtrlButton(CtrlID.CTRL_ID_DATA_VIEW, "조회화면");
		buttonDataview.setOnClickListener(this);

		LinearLayout layoutDataView = new LinearLayout(getContext());
		layoutDataView.setOrientation(LinearLayout.HORIZONTAL);
		
		layoutDataView.addView(buttonDataview, new LinearLayout.LayoutParams(350, LayoutParams.MATCH_PARENT));
		layoutFrame.addView(layoutDataView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 130));
		addView(layoutFrame, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
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
		//주문체결 실시간 등록
		if ( ConfigUtil.getMotu() ) m_OrderRealProc.RequestReal("scn_m", m_strUserID);
		else m_OrderRealProc.RequestReal("scn_r", m_strUserID);
	}
	
	public void releaseView()
	{
		removeAllViews();

		m_OrderTranProc.ClearInstance();
		m_OrderTranProc = null;
			
		m_JangoTranProc.ClearInstance();
		m_JangoTranProc = null;
		
		m_OrderListTranProc.ClearInstance();
		m_OrderListTranProc = null;
		
		m_OrderRealProc.ClearInstance();
		m_OrderRealProc = null;	
		
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
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int nCtrlId = v.getId();
		EditText viewacc = null;
		EditText viewPass = null;
		EditText Codeview = null;
		EditText viewQty = null;
		EditText viewPrice = null;
		EditText viewNo = null;
		EditText viewOrderNumber = null;
		String strPass,strEncPass,strOrderQty,strOrderPrice;
		String strNo,strOrderNumber;
		switch(nCtrlId)
		{
		case CtrlID.CTRL_ID_ORDER_REQUEST : // 매수주문 				
			m_OrderTranProc.ClearInblockData();
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return;			
			m_OrderTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_OrderTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_OrderTranProc.GetEncryptPassword(strPass);
			m_OrderTranProc.SetSingleData(0, 2, strEncPass);	
			Codeview = (EditText)findViewById(CtrlID.CTRL_ID_CODE);
			if(Codeview == null) return;		
			m_strCode = Codeview.getText().toString();
			m_OrderTranProc.SetSingleData(0, 3, m_strCode);							//상품코드
			m_OrderTranProc.SetSingleData(0, 4, "00");									//주문구분  00:지정가
			//주문수량
			viewQty = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_QTY);
			strOrderQty = viewQty.getText().toString();		
			m_OrderTranProc.SetSingleData(0, 5,strOrderQty);	
			//주문단가
			viewPrice =  (EditText)findViewById(CtrlID.CTRL_ID_ORDER_PRICE);
			strOrderPrice = viewPrice.getText().toString();
			m_OrderTranProc.SetSingleData(0, 6, strOrderPrice);						
			m_OrderTranProc.SetSingleData(0, 7, " ");									//연락전화번호
			//축약서명
			m_OrderTranProc.SetCertType(1);
			//매수주문 
			m_nOrderRqId = m_OrderTranProc.RequestData("scabo");
			break;
		
		case CtrlID.CTRL_ID_ORDER_REQUEST2 : // 매도주문 		
			m_OrderTranProc.ClearInblockData();
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return;			
			m_OrderTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_OrderTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_OrderTranProc.GetEncryptPassword(strPass);
			m_OrderTranProc.SetSingleData(0, 2, strEncPass);	
			Codeview = (EditText)findViewById(CtrlID.CTRL_ID_CODE);
			if(Codeview == null) return;		
			m_strCode = Codeview.getText().toString();
			m_OrderTranProc.SetSingleData(0, 3, m_strCode);					//상품코드
			m_OrderTranProc.SetSingleData(0, 4, "01");							//매도유형
			m_OrderTranProc.SetSingleData(0, 5, "00");							//주문구분
			//주문수량
			viewQty = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_QTY);
			strOrderQty = viewQty.getText().toString();					
			m_OrderTranProc.SetSingleData(0, 6, strOrderQty);				
			//주문단가
			viewPrice =  (EditText)findViewById(CtrlID.CTRL_ID_ORDER_PRICE);
			strOrderPrice = viewPrice.getText().toString();
			m_OrderTranProc.SetSingleData(0, 7,strOrderPrice);				
			m_OrderTranProc.SetSingleData(0, 8, "2 ");							//연락전화번호
			//축약서명
			m_OrderTranProc.SetCertType(1);
			//매도주문 
			m_nOrderRqId = m_OrderTranProc.RequestData("scaao");
			break;
			
		case CtrlID.CTRL_ID_ORDER_REQUEST3 : // 정정주문 		
			m_OrderTranProc.ClearInblockData();
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return;			
			m_OrderTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_OrderTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_OrderTranProc.GetEncryptPassword(strPass);
			m_OrderTranProc.SetSingleData(0, 2, strEncPass);
			//한국거래소전송주문조직번호
			viewNo = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NO);
			if(viewNo == null) return;		
			strNo = viewNo.getText().toString();
			m_OrderTranProc.SetSingleData(0, 3, strNo);					
			//원주문번호
			viewOrderNumber = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NUMBER);
			if(viewOrderNumber == null) return;		
			strOrderNumber = viewOrderNumber.getText().toString();
			m_OrderTranProc.SetSingleData(0, 4, strOrderNumber);					
			m_OrderTranProc.SetSingleData(0, 5, "00");							//주문구분		
			m_OrderTranProc.SetSingleData(0, 6, "01");							//정정취소구분코드	01정정02취소
			//주문수량
			viewQty = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_QTY);
			strOrderQty = viewQty.getText().toString();					
			m_OrderTranProc.SetSingleData(0, 7, strOrderQty);				
			//주문단가
			viewPrice =  (EditText)findViewById(CtrlID.CTRL_ID_ORDER_PRICE);
			strOrderPrice = viewPrice.getText().toString();
			m_OrderTranProc.SetSingleData(0, 8,strOrderPrice);		
			m_OrderTranProc.SetSingleData(0, 9, "N");							//잔량전부주문여부
			m_OrderTranProc.SetSingleData(0, 10, "2 ");							//연락전화번호
			//축약서명
			m_OrderTranProc.SetCertType(1);
			//정정주문 
			m_nOrderRqId = m_OrderTranProc.RequestData("smco");
			break;			
		
		case CtrlID.CTRL_ID_ORDER_REQUEST4 : // 취소주문 		
			m_OrderTranProc.ClearInblockData();
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return;			
			m_OrderTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_OrderTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_OrderTranProc.GetEncryptPassword(strPass);
			m_OrderTranProc.SetSingleData(0, 2, strEncPass);
			//한국거래소전송주문조직번호
			viewNo = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NO);
			if(viewNo == null) return;		
			strNo = viewNo.getText().toString();
			m_OrderTranProc.SetSingleData(0, 3, strNo);					
			//원주문번호
			viewOrderNumber = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NUMBER);
			if(viewOrderNumber == null) return;		
			strOrderNumber = viewOrderNumber.getText().toString();
			m_OrderTranProc.SetSingleData(0, 4, strOrderNumber);					
			m_OrderTranProc.SetSingleData(0, 5, "00");							//주문구분		
			m_OrderTranProc.SetSingleData(0, 6, "02");							//정정취소구분코드 	01정정02취소
			//주문수량
			viewQty = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_QTY);
			strOrderQty = viewQty.getText().toString();					
			m_OrderTranProc.SetSingleData(0, 7, strOrderQty);				
			//주문단가
			viewPrice =  (EditText)findViewById(CtrlID.CTRL_ID_ORDER_PRICE);
			strOrderPrice = viewPrice.getText().toString();
			m_OrderTranProc.SetSingleData(0, 8,strOrderPrice);		
			m_OrderTranProc.SetSingleData(0, 9, "N");							//잔량전부주문여부
			m_OrderTranProc.SetSingleData(0, 10, "2 ");							//연락전화번호
			//축약서명
			m_OrderTranProc.SetCertType(1);
			//취소주문 
			m_nOrderRqId = m_OrderTranProc.RequestData("smco");
			break;		
		case CtrlID.CTRL_ID_ORDER_LIST:		//주문리스트
			m_OrderListTranProc.ClearInblockData();
			EditText viewOrderList = (EditText)findViewById(CtrlID.CTRL_ID_ORDERLIST_NO1);
			if(viewOrderList != null) 
				viewOrderList.setText("");	

			EditText viewOrderList2 = (EditText)findViewById(CtrlID.CTRL_ID_ORDERLIST_NO2);
			if(viewOrderList2 != null) 
				viewOrderList2.setText("");	
		
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return ;			
			m_OrderListTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_OrderListTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return ;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_OrderListTranProc.GetEncryptPassword(strPass);
			m_OrderListTranProc.SetSingleData(0, 2, strEncPass);	
			m_OrderListTranProc.SetSingleData(0, 3, " ");				//연속조회검색조건100
			m_OrderListTranProc.SetSingleData(0, 4, " ");				//연속조회키100		
			m_OrderListTranProc.SetSingleData(0, 5, "0");				//조회구분1 0-주문순, 1-종목순 
			
			m_nOrderListRqId = m_OrderListTranProc.RequestData("smcp");						
			break;
			
		case CtrlID.CTRL_ID_JANGO_REQUEST:	//잔고조회
			m_JangoTranProc.ClearInblockData();
			//계좌번호
			viewacc = (EditText)findViewById(CtrlID.CTRL_ID_ACC);
			if(viewacc == null) return;			
			m_JangoTranProc.SetSingleData(0, 0, viewacc.getText().toString());	
			//상품코드
			m_JangoTranProc.SetSingleData(0, 1, m_strAccountCode); 	
			//비밀번호
			viewPass = (EditText)findViewById(CtrlID.CTRL_ID_PASS);
			if(viewPass == null) return;
			strPass = viewPass.getText().toString();
			if(strPass.isEmpty())
			{
				Toast.makeText( getContext(),String.format("비밀번호 입력하세요" ), Toast.LENGTH_SHORT ).show();
				return ;
			}			
			strEncPass =  m_JangoTranProc.GetEncryptPassword(strPass);
			m_JangoTranProc.SetSingleData(0, 2, strEncPass);	
			m_JangoTranProc.SetSingleData(0, 3, "N");				//시간외 단일가여부
			m_JangoTranProc.SetSingleData(0, 4, "N");				//오프라인 여부
			m_JangoTranProc.SetSingleData(0, 5, "01");				//조회구분
			m_JangoTranProc.SetSingleData(0, 6, "01");				//단가구분
			m_JangoTranProc.SetSingleData(0, 7, "N");				//펀드결제분 포함여부
			m_JangoTranProc.SetSingleData(0, 8, "N");				//융자금액자동상환여부
			m_JangoTranProc.SetSingleData(0, 9, "00");				//처리구분
			m_JangoTranProc.SetSingleData(0,10, " ");				//연속조회검색조건
			m_JangoTranProc.SetSingleData(0, 11, " " );				//연속조회키
			
			m_nJangoRqId = m_JangoTranProc.RequestData("satps");
			break;
		case CtrlID.CTRL_ID_DATA_VIEW : //
			//조회 화면 전환
			m_actMain.sendMessage(0 , 0, 0 );			
		}	
	}
	@Override
	public void onTranDataReceived(String sTranID, int nRqId) {
		// TODO Auto-generated method stub
		//조회 데이터 받아서 처리
		if(m_nOrderRqId == nRqId)			//주문
		{
			String stNo= m_OrderTranProc.GetSingleData(0,0);				//한국거래소전송주문조직번호
			String strOrederNo= m_OrderTranProc.GetSingleData(0,1);		//주문번호
			String strTime = m_OrderTranProc.GetSingleData(0,2);			//주문시각
			//매도/매수 주문
			if(sTranID.contains("scabo") || sTranID.contains("scaao"))			
			{
				EditText viewNo = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NO);
				if(viewNo != null) 
					viewNo.setText(String.format("%s",stNo));	
	
				EditText viewOrderNumber = (EditText)findViewById(CtrlID.CTRL_ID_ORDER_NUMBER);
				if(viewOrderNumber != null) 
					viewOrderNumber.setText(String.format("%s",strOrederNo));	
			}
			Log.d("==주식 주문==", String.format("한국거래소전송주문조직번호:%s 주문번호:%s 주문시각:%s",  stNo ,  strOrederNo  , strTime));				
		}
		else if(m_nOrderListRqId == nRqId)			//주문 리스트
		{
			String strNo = " ", strOrderNumber = " ", strSellBuy =" ", strCode = " ", strName = " ";
			int nCount =m_OrderListTranProc.GetValidCount(0);
			for(int i = 0; i < nCount; i++ )
			{
				strNo = m_OrderListTranProc.GetMultiData(0,0, i); 					//주문채번지점번호
				strOrderNumber = m_OrderListTranProc.GetMultiData(0,1,i);			//주문번호
				strSellBuy = m_OrderListTranProc.GetMultiData(0,3,i);					//주문구분명
				strCode= m_OrderListTranProc.GetMultiData(0,4,i);						//상품번호
				strName = m_OrderListTranProc.GetMultiData(0,6,i);					//정정취소구분명
			
				if(strOrderNumber.isEmpty())
					continue;
				
				if(i == 0)
				{
					EditText viewOrderList = (EditText)findViewById(CtrlID.CTRL_ID_ORDERLIST_NO1);
					if(viewOrderList != null) 
						viewOrderList.setText(String.format("지점번호:%s 주문번호:%s 구분:%s  %s  %s", strNo, strOrderNumber, strSellBuy, strCode,strName ));	
				}
				else if(i == 1)
				{
					EditText viewOrderList = (EditText)findViewById(CtrlID.CTRL_ID_ORDERLIST_NO2);
					if(viewOrderList != null) 
						viewOrderList.setText(String.format("지점번호:%s 주문번호:%s 구분:%s  %s  %s", strNo, strOrderNumber, strSellBuy, strCode,strName ));	
				}				
			}
		}
		else if(m_nJangoRqId == nRqId)		//잔고조회
		{
			//예수금 총금액
			String strTotal1 = m_JangoTranProc.GetMultiData(1, 0, 0);
			EditText viewTotal1 = (EditText)findViewById(CtrlID.CTRL_ID_J_TOTAL1);
			if(viewTotal1 != null) 
				viewTotal1.setText(String.format("%d", Integer.parseInt( strTotal1 )));	
			//총평가금액
			String strTotal2 = m_JangoTranProc.GetMultiData(1, 14, 0);
			EditText viewTotal2= (EditText)findViewById(CtrlID.CTRL_ID_J_TOTAL2);
			if(viewTotal2 != null) 
				viewTotal2.setText(String.format("%d", Integer.parseInt( strTotal2 )));	
			
			int nCount =m_JangoTranProc.GetValidCount(0);
			for(int i = 0;  i< nCount; i++)
			{
				//종목
				String strCode =  m_JangoTranProc.GetMultiData(0,1, i);
				//잔고
				String strQty = m_JangoTranProc.GetMultiData(0,7, i);
				
				if(i == 0)
				{
					EditText viewCode = (EditText)findViewById(CtrlID.CTRL_ID_J_CODENAME1);
					if(viewCode != null) 
						viewCode.setText(strCode);	
					
					EditText viewQty = (EditText)findViewById(CtrlID.CTRL_ID_J_QTY1);
					if(viewQty != null) 
						viewQty.setText(String.format("%d", Integer.parseInt( strQty )));	
				}
				else if(i ==1)
				{
					EditText viewCode = (EditText)findViewById(CtrlID.CTRL_ID_J_CODENAME2);
					if(viewCode != null) 
						viewCode.setText(strCode);	
					
					EditText viewQty = (EditText)findViewById(CtrlID.CTRL_ID_J_QTY2);
					if(viewQty != null) 
						viewQty.setText(String.format("%d", Integer.parseInt( strQty )));		
				}
			}
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
		
		if(strServiceId == "scn_r" || strServiceId == "scn_m")		
		{
			String strOrderNumber = m_OrderRealProc.GetRealData(0,2);		//주문번호
			String strOrderGubun = m_OrderRealProc.GetRealData(0,4);			//매도매수구분
			String strCode = m_OrderRealProc.GetRealData(0,8);					//종목코드
			
			Log.d("==주식 체결통보==", String.format("주문번호:%s 매도매수구분:%s 종목코드:%s",  strOrderNumber ,  strOrderGubun  , strCode));				
		}
	}

}
