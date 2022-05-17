package com.stucs17.stockai

import android.content.ContentValues.TAG
import android.util.Log
import java.text.DecimalFormat

class GlobalBackground{


    fun logTest() {
        Log.d(TAG,"wow")
    }

    fun dec(num:Int): String{
        val dec = DecimalFormat("#,###")
        return dec.format((num))
    }

    fun plus(price:Int,unitNum:Int): Int{
        return price+unitNum
    }
    fun minus(price:Int,unitNum:Int): Int{
        return if(price-unitNum>0)
            price-unitNum
        else 0
    }

}