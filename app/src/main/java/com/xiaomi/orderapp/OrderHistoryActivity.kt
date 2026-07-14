package com.xiaomi.orderapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var listView: ListView
    private var orderIds: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)
        db = DBHelper(this)
        listView = findViewById(R.id.lvOrderHistory)

        listView.setOnItemClickListener { _, _, position, _ ->
            val orderId = orderIds[position]
            val intent = Intent(this, OrderDetailActivity::class.java)
            intent.putExtra("order_id", orderId)
            startActivity(intent)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val orderId = orderIds[position]
            AlertDialog.Builder(this)
                .setTitle("Delete this order?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteOrder(orderId)
                    refreshList()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val orders = db.getAllOrders()
        orderIds = orders.map { it.id }
        val display = orders.map { "${it.createdAt}  (${it.itemCount} items)" }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
    }
}
