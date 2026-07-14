package com.xiaomi.orderapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class ProductsActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var listView: ListView
    private var productIds: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)
        db = DBHelper(this)

        val etName = findViewById<EditText>(R.id.etProductName)
        listView = findViewById(R.id.lvProducts)

        findViewById<Button>(R.id.btnAddProduct).setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isNotEmpty()) {
                db.addProduct(name)
                etName.text.clear()
                refreshList()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val idToDelete = productIds[position]
            AlertDialog.Builder(this)
                .setTitle("Delete product?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteProduct(idToDelete)
                    refreshList()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        refreshList()
    }

    private fun refreshList() {
        val products = db.getAllProducts()
        productIds = products.map { it.first }
        val display = products.map { it.second }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
    }
}
