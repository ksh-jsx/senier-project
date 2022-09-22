package com.stucs17.stockai


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.Worker
import com.stucs17.stockai.Public.BackgroundWorker
import java.text.DecimalFormat

class GlobalBackground{

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
