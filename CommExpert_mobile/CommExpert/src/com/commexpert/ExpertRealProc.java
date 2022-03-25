package com.commexpert;

import android.content.Context;

import com.truefriend.corelib.commexpert.ExpertBaseRealProc;
import com.truefriend.corelib.commexpert.intrf.IRealDataListener;



public class ExpertRealProc extends ExpertBaseRealProc 
{
	
	public ExpertRealProc(Context oContext)
	{
		super(oContext);		
	}
	
	public void InitInstance(IRealDataListener listener)
	{
		super.InitInstance(listener);
	}
	
	/**
	 *  Inblock key Date set
	 * @param nBlockIndex  : 블럭 인덱스
	 * @param nItemIndex : 데이타 항목 
	 * @param strData : 데이타
	 */
	public void SetRealData(int nBlockIndex,int nItemIndex,String strData)
	{
		super.SetRealData( nBlockIndex, nItemIndex, strData);
	}
	
	public void ClearInstance()
	{
		super.ClearInstance();
	}
	
	/**
	 * 리얼테이타 요청
	 * @param strRealId
	 * @param strKey
	 */
	public void RequestReal(String strRealId,String strKey)
	{
		super.RequestReal(strRealId,strKey);
	}
	/**
	 * 리얼데이터 해지
	 * @param strRealId
	 * @param strKey
	 */
	public void ReleaseReal(String strRealId,String strKey)
	{		
		super.ReleaseReal(strRealId,strKey);
	}
	/**
	 *  Real Data Get
	 * @param nBlockIndex
	 * @param nItemIndex
	 * @return
	 */
	public String GetRealData(int nBlockIndex,int nItemIndex)
	{
			return super.GetRealData( nBlockIndex, nItemIndex);
	}

	/**
	 *  Get Real Attr Data
	 * @param nBlockIndex
	 * @param nItemIndex
	 * @return
	 */
	public int GetAttrRealData(int nBlockIndex, int nItemIndex)
	{
		return super.GetAttrRealData( nBlockIndex,  nItemIndex);
	}

	public void SetShowTrLog(boolean	bShowTRLog )
	{
		super.SetShowTrLog(bShowTRLog );
	}
}




