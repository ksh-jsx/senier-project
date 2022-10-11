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
                "numPwd integer);"

        db.execSQL(sql)
        val sql2: String = "CREATE TABLE if not exists like (" +
                "code text primary key ," +
                "name text);"
        db.execSQL(sql2)
        val sql3: String = "CREATE TABLE if not exists orders (" +
                "OrderNumberOri text primary key ," +
                "OrderNumberKET text);"
        db.execSQL(sql3)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql = "DROP TABLE if exists user"
        db.execSQL(sql)
        val sql2 = "DROP TABLE if exists like"
        db.execSQL(sql2)
        val sql3 = "DROP TABLE if exists orders"
        db.execSQL(sql3)
        onCreate(db)
    }
}