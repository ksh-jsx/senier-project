package com.openapi.sample;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;

import com.truefriend.corelib.util.Util;

import java.util.ArrayList;

@SuppressLint({ "InlinedApi", "NewApi" })
public class PermissionManager
{
    private final String LOG_TAG = "PermissionManager";

    private interface PER
    {
        // 앱을 실행 시 선점되어야 하는 권한들(필수)
        String[] PERMISSION = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,  Manifest.permission.READ_PHONE_STATE };
    }


    private interface REQ_CODE
    {
        int SINGLE = 1011;
        int MULTIPLE = 1012;
    }

    private static PermissionManager ms_instance;

    private OnPermissionListener mOnPermissionListener;
    private ArrayList<String> m_arrPermission;

    public interface OnPermissionListener
    {
        void onPermissionResult(boolean isSucs, Object objPermission);
    }

    public void setOnPermissionListener(OnPermissionListener listener)
    {
        mOnPermissionListener = listener;
    }

    public static PermissionManager getInstance()
    {
        if( null == ms_instance )   ms_instance = new PermissionManager();

        return ms_instance;
    }

    private PermissionManager()
    {
        m_arrPermission = new ArrayList<String>();
    }

    public void release()
    {
        if( null != m_arrPermission )   m_arrPermission.clear();
        m_arrPermission = null;

        mOnPermissionListener = null;
        ms_instance = null;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        switch (requestCode)
        {
            case REQ_CODE.SINGLE:
            {
                receiveSinglePermission(permissions, grantResults);
            }
            break;
            case REQ_CODE.MULTIPLE:
            {
                receiveMultiplePermissions(permissions, grantResults);
            }
            break;
        }
    }

    public boolean checkPermission()
    {
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            for(int i = 0; i < PER.PERMISSION.length; i++ )
            {
                String szPermission = PER.PERMISSION[i];
                if( Util.getMainActivity().checkSelfPermission(szPermission) != PackageManager.PERMISSION_GRANTED )
                {
                    m_arrPermission.add(szPermission);
                }
            }

            if( m_arrPermission.size() > 0 )
            {
                return false;
            }
        }

        postPermissionListener(true, null);
        return true;
    }


    public void requestPermissions()
    {
        String[] arrPermission = new String[m_arrPermission.size()];
        for( int i = 0; i < m_arrPermission.size(); i++ )   arrPermission[i] = m_arrPermission.get(i);
        Util.getMainActivity().requestPermissions(arrPermission, REQ_CODE.MULTIPLE);
    }

    private void receiveSinglePermission(String[] permissions, int[] grantResults)
    {
        if( null == permissions || permissions.length <= 0 ) return;
        if( m_arrPermission == null || m_arrPermission.size() == 0 )    return;

        String szPermission = permissions[0];
        if( szPermission.equals(m_arrPermission.get(0) ) )
        {
            boolean isSuccess = true;
            if( grantResults[0] != PackageManager.PERMISSION_GRANTED )
            {
                isSuccess = false;
            }

            postPermissionListener(isSuccess, szPermission);
        }

        m_arrPermission.clear();
    }

    private void receiveMultiplePermissions(String[] permissions, int[] grantResults)
    {
        boolean isSuccess = true;
        for( int i = 0; i < permissions.length; i++ )
        {
            String szPermission = permissions[i];
            if( grantResults[i] != PackageManager.PERMISSION_GRANTED )
            {
                isSuccess = false;
                break;
            }
        }

        postPermissionListener(isSuccess, permissions);

        m_arrPermission.clear();
    }


    private void postPermissionListener(boolean isSucs, Object objPermissions)
    {
        if( null != mOnPermissionListener )
        {
            mOnPermissionListener.onPermissionResult(isSucs, objPermissions);
        }
    }
}
