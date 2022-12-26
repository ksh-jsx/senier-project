package com.stucs17.stockai.Public

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.commexpert.CommExpertMng
import com.stucs17.stockai.sql.DBHelper

class Database: AppCompatActivity() {

    fun login(idStr:String, pwStr:String, caPwStr:String, numPwStr:String) {

        if (idStr.isEmpty()) {
            Toast.makeText(baseContext, "아이디를 확인하세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (pwStr.isEmpty()) {
            Toast.makeText(baseContext, "비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            return
        } else if (caPwStr.isEmpty()) {
            Toast.makeText(baseContext, "공인인증 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            return

        } else if (numPwStr.isEmpty()) {
            Toast.makeText(baseContext, "간편 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            CommExpertMng.getInstance().StartLogin(idStr, pwStr, caPwStr);
            return
        }
    }

    fun select(database:SQLiteDatabase): Cursor? {
        val query = "SELECT * FROM user;"
        return database.rawQuery(query, null)
    }

    fun insert(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("user", null, contentValues)
    }

    fun update(database:SQLiteDatabase,state:Int) {
        val query = "UPDATE user SET autoTrade = '$state';"
        database.execSQL(query)
    }
    fun updateTradeLevel(database:SQLiteDatabase,state:Int) {
        val query = "UPDATE user SET autoTradeLevel = '$state';"
        database.execSQL(query)
    }
    fun update12(database:SQLiteDatabase,state:Int) {
        val query = "UPDATE user SET setting12 = '$state';"
        database.execSQL(query)
    }
    fun update13(database:SQLiteDatabase,state:Int) {
        val query = "UPDATE user SET setting13 = '$state';"
        database.execSQL(query)
    }

    fun select_like(database:SQLiteDatabase): Cursor? {
        val query = "SELECT * FROM likes;"
        return database.rawQuery(query, null)
    }

    fun insert_like(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("likes", null, contentValues)
    }

    fun isExist_like(database:SQLiteDatabase, stockCode:String): Cursor? {
        val query = "SELECT * FROM likes where code = '$stockCode';"
        return database.rawQuery(query, null)
    }

    fun delete_like(database:SQLiteDatabase, stockCode:String) {
        val query = "DELETE FROM likes WHERE code = '$stockCode';"
        database.execSQL(query)
    }

    fun select_order(database:SQLiteDatabase,OrderNumberOri:String): Cursor? {
        val query = "SELECT * FROM orders where OrderNumberOri = '$OrderNumberOri';"
        val temp = "SELECT * FROM orders;"  //
        val c = database.rawQuery(temp, null)
        while(c.moveToNext()) {
            val OrderNumberOri = c.getString(c.getColumnIndex("OrderNumberOri"))
            val OrderNumberKET = c.getString(c.getColumnIndex("OrderNumberKET"))

            Log.d("db","OrderNumberOri:$OrderNumberOri / OrderNumberKET:$OrderNumberKET /")
        }
        return database.rawQuery(query, null)
    }
    fun insert_order(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("orders", null, contentValues)
    }
    fun delete_order(database:SQLiteDatabase, OrderNumberOri:String) {
        val query = "DELETE FROM orders WHERE OrderNumberOri = '$OrderNumberOri';"
        database.execSQL(query)
    }

    fun select_autoTradeTarget(database:SQLiteDatabase): Cursor? {
        val query = "SELECT * FROM autoTradeTarget;"
        return database.rawQuery(query, null)
    }

    fun insert_autoTradeTarget(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("autoTradeTarget", null, contentValues)
    }
    fun isExist_autoTradeTarget(database:SQLiteDatabase, stockCode:String): Cursor? {
        val query = "SELECT * FROM autoTradeTarget where code = '$stockCode';"
        return database.rawQuery(query, null)
    }
    fun delete_autoTradeTarget(database:SQLiteDatabase, stockCode:String) {
        val query = "DELETE FROM autoTradeTarget WHERE code = '$stockCode';"
        database.execSQL(query)
    }

}