package com.commexpert;

import com.truefriend.corelib.commexpert.ExpertBaseTranProc;
import com.truefriend.corelib.commexpert.intrf.ITranDataListener;
import android.content.Context;


public class ExpertTranProc extends ExpertBaseTranProc {

	
	public ExpertTranProc(Context oContext)
	{
		super(oContext);
		
	}
		
	public void ClearInstance()
	{
		super.ClearInstance();
	}
	
	public void InitInstance(ITranDataListener listener)
	{
		super.InitInstance(listener);
	}
	/**
	 * Single Inblock Data set  
	 * @param nBlockIndex : 블럭 인덱스
	 * @param nItemIndex : 데이타 항목 
	 * @param strData : 데이타
	 */
	public void SetSingleData(int nBlockIndex, int nItemIndex,String strData)
	{
		super.SetSingleData( nBlockIndex,  nItemIndex, strData);
	}
	/**
	 * Multi Inblock Data set  
	 * @param nBlockIndex  : 블럭 인덱스
	 * @param nItemIndex : 데이타 항목 
	 * @param strData  : 데이타
	 * @param nIndex  : 데이타 인덱스
	 */
	public void SetMultiData(int nBlockIndex,int nItemIndex,String strData,int nIndex)
	{
		super.SetMultiData( nBlockIndex, nItemIndex, strData, nIndex);
	}
	/**
	 * Single Item Data Get
	 * @param nBlockIndex : 블럭 인덱스
	 * @param nItemIndex: 데이타 항목 
	 * @return
	 */
	public String GetSingleData(int nBlockIndex,int nItemIndex)
	{
		return super.GetSingleData( nBlockIndex, nItemIndex);
	}
	/**
	 * Mult Item Data Get
	 * @param nBlockIndex : 블럭 인덱스
	 * @param nItemIndex : 데이타 항목 
	 * @param nDataIndex : 데이타 인덱스
	 * @return
	 */
	public String GetMultiData(int nBlockIndex,int nItemIndex, int nDataIndex)
	{
		return  super.GetMultiData( nBlockIndex, nItemIndex,  nDataIndex);
	}
	/**
	 * Get Attr Single Data
	 * @param nBlockIndex : 블럭 인덱스
	 * @param nItemIndex  : 데이타 항목 
	 * @return
	 */
	public int GetAttrSingleData(int nBlockIndex,int nItemIndex)
	{
		return super.GetAttrSingleData(nBlockIndex,nItemIndex);
	}
	/**
	 * Get Attr Multi Data
	 * @param nBlockIndex : 블럭 인덱스
	 * @param nItemIndex : 데이타 항목 
	 * @param nDataIndex :  : 데이타 인덱스 
	 * @return
	 */
	public int GetAttrMultiData(int nBlockIndex,int nItemIndex, int nDataIndex)
	{
		return super.GetAttrMultiData( nBlockIndex, nItemIndex,  nDataIndex);
	}
	/**
	 * Password 암호화
	 * @param sValue
	 * @return
	 */
	public String GetEncryptPassword(String sValue)
	{
		return super.GetEncryptPassword( sValue);
	}

	/**
	 * 공인인증 타입
	 * @param nType	0:일반 1:축약 
	 */
	public void SetCertType(int nType)
	{
		super.SetCertType(nType);
	}
	/**
	 * Request data 
	 * @param strTrCode
	 * @return
	 */
	public int RequestData(String strTrCode )
	{		
		return super.RequestData( strTrCode );
	}
	/**
	 * Request NextData
	 * @param strTrCode
	 * @return
	 */
	public int RequestNextData(String strTrCode )
	{
		return super.RequestNextData(strTrCode );
	}	
	/**
	 * Get Block  Valid Data Count
	 * @param nBlockIndex
	 * @return
	 */
	public int GetValidCount(int nBlockIndex)
	{
		return super.GetValidCount(nBlockIndex);
	}
	/**
	 * Show log 
	 * @param bShowTRLog    true:show
	 */
	public void SetShowTrLog(boolean	bShowTRLog )
	{
		super.SetShowTrLog(bShowTRLog );
	}
}
