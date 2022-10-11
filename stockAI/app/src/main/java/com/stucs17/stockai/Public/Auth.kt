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

class Auth: AppCompatActivity() {

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

    fun select_like(database:SQLiteDatabase): Cursor? {
        val query = "SELECT * FROM like;"
        return database.rawQuery(query, null)
    }

    fun insert_like(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("like", null, contentValues)
    }

    fun isExist_like(database:SQLiteDatabase, stockCode:String): Cursor? {
        val query = "SELECT * FROM like where code = '$stockCode';"
        return database.rawQuery(query, null)
    }

    fun delete_like(database:SQLiteDatabase, stockCode:String) {
        val query = "DELETE FROM like WHERE code = '$stockCode';"
        database.execSQL(query)
    }

    fun select_order(database:SQLiteDatabase,strOrderNumberOri:String): Cursor? {
        val query = "SELECT * FROM orders where strOrderNumberOri = $strOrderNumberOri;"
        return database.rawQuery(query, null)
    }

    fun insert_order(contentValues:ContentValues,database:SQLiteDatabase) {
        database.insert("orders", null, contentValues)
    }

}