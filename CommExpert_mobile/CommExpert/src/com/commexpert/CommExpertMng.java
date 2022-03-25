package com.commexpert;

import java.util.ArrayList;

import com.truefriend.corelib.commexpert.CommBaseExpertMng;
import com.truefriend.corelib.commexpert.intrf.IExpertInitListener;
import com.truefriend.corelib.commexpert.intrf.IExpertLoginListener;
import com.truefriend.corelib.shared.ItemMaster.ItemCode;


/**
 *Open API 매니져 클스
 *
 */
public class CommExpertMng extends CommBaseExpertMng {

	
//	/**
//	 * Activity 초기화
//	 * @param activity
//	 */
//	public  static void InitActivity(Activity activity)
//	{
//	}
//	/**
//	 * 통신 모듈 초기화
//	 * 통신 모듈을 사용하는 activity의 onCreate에서 호출한다.
//	 * @param context
//	 */
//	public void InitCommExpert(Context context)
//	{	
//	}

	/**
	 *  InitListener 초기화 
	 * @param listener
	 */
	public void SetInitListener(IExpertInitListener listener)
	{
		super.SetInitListener(listener);
	}
	/**
	 * LoginListener 초기화
	 * @param listener
	 */
	public void SetLoginListener(IExpertLoginListener listener)
	{
		super.SetLoginListener(listener);
	}
	/**
	 * 
	 * @return
	 */
//	public  CommExpertMng getInstance()
//	{
//		
//	}
	
	public CommExpertMng()
	{
		super();
	}
	/**
	 * 로그인 시작
	 * @param strUserId
	 * @param strUserPW
	 * @param strCertPW
	 */
	public void StartLogin( String strUserId, String strUserPW,String strCertPW )
	{
		super.StartLogin(  strUserId,  strUserPW, strCertPW );
	}
	
	/**
	 * 매니져 종료
	 */
	public void Close()
	{
		super.Close();
	}
	
	/**
	 * 로그인 ID 
	 * @return
	 */
	public String GetLoginUserID( )
	{
		return super.GetLoginUserID( );
	}
	/**
	 * 계좌리스트 갯수
	 * @return
	 */
	public int GetAccountSize()
	{
		return super.GetAccountSize();
	}
	/**
	 * 계좌번호
	 * @param nIndex
	 * @return
	 */
	public String GetAccountNo(int nIndex)
	{	
		return super. GetAccountNo( nIndex);
	}
	/**
	 * 상품 코드
	 * @param nIndex
	 * @return
	 */
	public String GetAccountCode(int nIndex)
	{
		return super.GetAccountCode(nIndex);
	}
	/**
	 * 계좌명
	 * @param nIndex
	 * @return
	 */
	public String GetAccountName(int nIndex)
	{	
		return super.GetAccountName( nIndex);
	}	
	/**
	 * 
	 * @param strAccount
	 * @param strAccountCode
	 */
	public void SetCurrAccountInfo(String strAccount ,String strAccountCode )
	{
		super.SetCurrAccountInfo( strAccount , strAccountCode );
	}
	/**
	 * 코스피종목아이템 리스트
	 * @return
	 */
	public  ArrayList<ItemCode> GetKospiCodeList()
	{
		return super.GetKospiCodeList();
	}	
	/**
	 * 코스닥종목아이템 리스트
	 * @return
	 */
	public  ArrayList<ItemCode> GetKosdaqCodeList()
	{
		return super.GetKosdaqCodeList();
	}	
	/**
	 * 리얼/개발 서버 셋팅
	 * @param strDev "0"리얼 / "1" 개발
	 */
	public  void  SetDevSetting(String strDev)
	{
		super.SetDevSetting(strDev);
	}		
}
