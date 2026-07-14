package com.xiaomi.orderapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "orderapp.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE dealers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, shop TEXT)")
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS dealers")
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    fun addDealer(name: String, shop: String): Long {
        val values = ContentValues()
        values.put("name", name)
        values.put("shop", shop)
        return writableDatabase.insert("dealers", null, values)
    }

    fun deleteDealer(id: Long) {
        writableDatabase.delete("dealers", "id=?", arrayOf(id.toString()))
    }

    fun getAllDealers(): List<Triple<Long, String, String>> {
        val list = mutableListOf<Triple<Long, String, String>>()
        val cursor = readableDatabase.rawQuery("SELECT id, name, shop FROM dealers ORDER BY name", null)
        while (cursor.moveToNext()) {
            list.add(Triple(cursor.getLong(0), cursor.getString(1), cursor.getString(2)))
        }
        cursor.close()
        return list
    }

    fun addProduct(name: String): Long {
        val values = ContentValues()
        values.put("name", name)
        return writableDatabase.insert("products", null, values)
    }

    fun deleteProduct(id: Long) {
        writableDatabase.delete("products", "id=?", arrayOf(id.toString()))
    }

    fun getAllProducts(): List<Pair<Long, String>> {
        val list = mutableListOf<Pair<Long, String>>()
        val cursor = readableDatabase.rawQuery("SELECT id, name FROM products ORDER BY name", null)
        while (cursor.moveToNext()) {
            list.add(Pair(cursor.getLong(0), cursor.getString(1)))
        }
        cursor.close()
        return list
    }
}
