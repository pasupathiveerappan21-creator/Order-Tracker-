package com.xiaomi.orderapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray
import org.json.JSONObject

data class OrderSummary(val id: Long, val createdAt: String, val itemCount: Int)
data class OrderLine(val dealer: String, val product: String, val variant: String, val color: String, val qty: String)

class DBHelper(context: Context) : SQLiteOpenHelper(context, "orderapp.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE dealers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, shop TEXT)")
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
        db.execSQL("CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT, created_at TEXT)")
        db.execSQL("CREATE TABLE order_lines (id INTEGER PRIMARY KEY AUTOINCREMENT, order_id INTEGER, dealer TEXT, product TEXT, variant TEXT, color TEXT, qty TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS dealers")
        db.execSQL("DROP TABLE IF EXISTS products")
        db.execSQL("DROP TABLE IF EXISTS orders")
        db.execSQL("DROP TABLE IF EXISTS order_lines")
        onCreate(db)
    }

    // Dealers
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

    // Products
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

    // Orders / history
    fun createOrder(createdAt: String): Long {
        val cv = ContentValues()
        cv.put("created_at", createdAt)
        return writableDatabase.insert("orders", null, cv)
    }

    fun addOrderLine(orderId: Long, dealer: String, product: String, variant: String, color: String, qty: String) {
        val cv = ContentValues()
        cv.put("order_id", orderId)
        cv.put("dealer", dealer)
        cv.put("product", product)
        cv.put("variant", variant)
        cv.put("color", color)
        cv.put("qty", qty)
        writableDatabase.insert("order_lines", null, cv)
    }

    fun getAllOrders(): List<OrderSummary> {
        val list = mutableListOf<OrderSummary>()
        val cursor = readableDatabase.rawQuery(
            "SELECT o.id, o.created_at, COUNT(l.id) FROM orders o LEFT JOIN order_lines l ON l.order_id = o.id GROUP BY o.id ORDER BY o.id DESC",
            null
        )
        while (cursor.moveToNext()) {
            list.add(OrderSummary(cursor.getLong(0), cursor.getString(1), cursor.getInt(2)))
        }
        cursor.close()
        return list
    }

    fun getOrderLines(orderId: Long): List<OrderLine> {
        val list = mutableListOf<OrderLine>()
        val cursor = readableDatabase.rawQuery(
            "SELECT dealer, product, variant, color, qty FROM order_lines WHERE order_id=?",
            arrayOf(orderId.toString())
        )
        while (cursor.moveToNext()) {
            list.add(OrderLine(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4)))
        }
        cursor.close()
        return list
    }

    fun deleteOrder(orderId: Long) {
        val dbw = writableDatabase
        dbw.delete("order_lines", "order_id=?", arrayOf(orderId.toString()))
        dbw.delete("orders", "id=?", arrayOf(orderId.toString()))
    }

    // Backup / restore
    fun exportToJson(): String {
        val root = JSONObject()

        val dealersArr = JSONArray()
        getAllDealers().forEach { (_, name, shop) ->
            val o = JSONObject()
            o.put("name", name)
            o.put("shop", shop)
            dealersArr.put(o)
        }
        root.put("dealers", dealersArr)

        val productsArr = JSONArray()
        getAllProducts().forEach { (_, name) ->
            val o = JSONObject()
            o.put("name", name)
            productsArr.put(o)
        }
        root.put("products", productsArr)

        val ordersArr = JSONArray()
        getAllOrders().forEach { order ->
            val o = JSONObject()
            o.put("created_at", order.createdAt)
            val linesArr = JSONArray()
            getOrderLines(order.id).forEach { line ->
                val lo = JSONObject()
                lo.put("dealer", line.dealer)
                lo.put("product", line.product)
                lo.put("variant", line.variant)
                lo.put("color", line.color)
                lo.put("qty", line.qty)
                linesArr.put(lo)
            }
            o.put("lines", linesArr)
            ordersArr.put(o)
        }
        root.put("orders", ordersArr)

        return root.toString(2)
    }

    fun importFromJson(json: String) {
        val root = JSONObject(json)
        val dbw = writableDatabase
        dbw.beginTransaction()
        try {
            dbw.execSQL("DELETE FROM order_lines")
            dbw.execSQL("DELETE FROM orders")
            dbw.execSQL("DELETE FROM dealers")
            dbw.execSQL("DELETE FROM products")

            root.optJSONArray("dealers")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cv = ContentValues()
                    cv.put("name", o.optString("name"))
                    cv.put("shop", o.optString("shop"))
                    dbw.insert("dealers", null, cv)
                }
            }

            root.optJSONArray("products")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val cv = ContentValues()
                    cv.put("name", o.optString("name"))
                    dbw.insert("products", null, cv)
                }
            }

            root.optJSONArray("orders")?.let { arr ->
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val orderCv = ContentValues()
                    orderCv.put("created_at", o.optString("created_at"))
                    val orderId = dbw.insert("orders", null, orderCv)
                    o.optJSONArray("lines")?.let { linesArr ->
                        for (j in 0 until linesArr.length()) {
                            val lo = linesArr.getJSONObject(j)
                            val lineCv = ContentValues()
                            lineCv.put("order_id", orderId)
                            lineCv.put("dealer", lo.optString("dealer"))
                            lineCv.put("product", lo.optString("product"))
                            lineCv.put("variant", lo.optString("variant"))
                            lineCv.put("color", lo.optString("color"))
                            lineCv.put("qty", lo.optString("qty"))
                            dbw.insert("order_lines", null, lineCv)
                        }
                    }
                }
            }
            dbw.setTransactionSuccessful()
        } finally {
            dbw.endTransaction()
        }
    }
}
