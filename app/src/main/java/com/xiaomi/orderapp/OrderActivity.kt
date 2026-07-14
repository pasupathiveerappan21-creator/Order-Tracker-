package com.xiaomi.orderapp

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderItem(
    val dealer: String,
    val product: String,
    val variant: String,
    val color: String,
    val qty: String
) {
    override fun toString(): String {
        val variantPart = if (variant.isNotEmpty()) " [$variant]" else ""
        val colorPart = if (color.isNotEmpty()) " ($color)" else ""
        return "$product$variantPart$colorPart x$qty  -> $dealer"
    }
}

class OrderActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private val orderItems = mutableListOf<OrderItem>()
    private lateinit var orderAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        db = DBHelper(this)

        val acDealer = findViewById<AutoCompleteTextView>(R.id.acDealer)
        val acProduct = findViewById<AutoCompleteTextView>(R.id.acProduct)
        val etVariant = findViewById<EditText>(R.id.etVariant)
        val etColor = findViewById<EditText>(R.id.etColor)
        val etQty = findViewById<EditText>(R.id.etQty)
        val lvOrderItems = findViewById<ListView>(R.id.lvOrderItems)

        val dealerNames = db.getAllDealers().map { "${it.second} - ${it.third}" }
        acDealer.setAdapter(SearchableAdapter(this, dealerNames))

        val productNames = db.getAllProducts().map { it.second }
        acProduct.setAdapter(SearchableAdapter(this, productNames))

        orderAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        lvOrderItems.adapter = orderAdapter

        findViewById<Button>(R.id.btnAddItem).setOnClickListener {
            val dealer = acDealer.text.toString().trim()
            val product = acProduct.text.toString().trim()
            val variant = etVariant.text.toString().trim()
            val color = etColor.text.toString().trim()
            val qty = etQty.text.toString().trim().ifEmpty { "1" }

            if (dealer.isEmpty() || product.isEmpty()) {
                Toast.makeText(this, "Pick a dealer and a product", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = OrderItem(dealer, product, variant, color, qty)
            orderItems.add(item)
            orderAdapter.add(item.toString())

            acProduct.text.clear()
            etVariant.text.clear()
            etColor.text.clear()
            etQty.text.clear()
        }

        lvOrderItems.setOnItemLongClickListener { _, _, position, _ ->
            orderItems.removeAt(position)
            orderAdapter.remove(orderAdapter.getItem(position))
            true
        }

        findViewById<Button>(R.id.btnShare).setOnClickListener {
            if (orderItems.isEmpty()) {
                Toast.makeText(this, "Order is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save this order to history with the current date/time
            val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            val orderId = db.createOrder(timestamp)
            orderItems.forEach { item ->
                db.addOrderLine(orderId, item.dealer, item.product, item.variant, item.color, item.qty)
            }

            val message = buildString {
                append("Order:\n\n")
                orderItems.forEach { append("- $it\n") }
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

            // Cleared here since it's now saved in Order History
            orderItems.clear()
            orderAdapter.clear()
        }
    }
}
