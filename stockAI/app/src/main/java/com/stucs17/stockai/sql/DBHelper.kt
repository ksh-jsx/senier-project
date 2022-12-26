package com.stucs17.stockai.sql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
): SQLiteOpenHelper(context, name, factory, version){
    override fun onCreate(db: SQLiteDatabase) {
        val sql: String = "CREATE TABLE if not exists user (" +
                "id text primary key ," +
                "pwd text," +
                "certPwd text," +
                "numPwd integer," +
                "autoTrade integer," +
                "autoTradeLevel integer," +
                "setting12 integer," +
                "setting13 integer" +
                ");"

        db.execSQL(sql)
        val sql2: String = "CREATE TABLE if not exists likes (" +
                "code text primary key ," +
                "name text);"
        db.execSQL(sql2)
        val sql3: String = "CREATE TABLE if not exists orders (" +
                "OrderNumberOri text primary key ," +
                "OrderNumberKET text);"
        db.execSQL(sql3)
        val sql4: String = "CREATE TABLE if not exists autoTradeTarget (" +
                "code text primary key ," +
                "name text);"
        db.execSQL(sql4)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql = "DROP TABLE if exists user"
        db.execSQL(sql)
        val sql2 = "DROP TABLE if exists likes"
        db.execSQL(sql2)
        val sql3 = "DROP TABLE if exists orders"
        db.execSQL(sql3)
        val sql4 = "DROP TABLE if exists autoTradeTarget"
        db.execSQL(sql4)
        onCreate(db)
    }
}