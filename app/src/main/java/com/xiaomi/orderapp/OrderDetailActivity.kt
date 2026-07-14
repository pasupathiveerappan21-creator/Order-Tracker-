package com.xiaomi.orderapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private var lines: List<OrderLine> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        db = DBHelper(this)

        val orderId = intent.getLongExtra("order_id", -1)
        lines = db.getOrderLines(orderId)

        val orderSummary = db.getAllOrders().find { it.id == orderId }
        findViewById<TextView>(R.id.tvOrderTitle).text = orderSummary?.createdAt ?: "Order"

        val display = lines.map { "${it.product} [${it.variant}] (${it.color}) x${it.qty}  -> ${it.dealer}" }
        findViewById<ListView>(R.id.lvOrderDetailLines).adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, display)

        findViewById<Button>(R.id.btnShareAgain).setOnClickListener {
            val message = buildString {
                append("Order (${orderSummary?.createdAt}):\n\n")
                lines.forEach { append("- ${it.product} [${it.variant}] (${it.color}) x${it.qty}  -> ${it.dealer}\n") }
            }
            val sendIntent = Intent(Intent.ACTION_SEND)
            sendIntent.type = "text/plain"
            sendIntent.putExtra(Intent.EXTRA_TEXT, message)
            sendIntent.setPackage("com.whatsapp")
            try {
                startActivity(sendIntent)
            } catch (e: Exception) {
                sendIntent.setPackage(null)
                startActivity(Intent.createChooser(sendIntent, "Share order"))
            }
        }
    }
}
