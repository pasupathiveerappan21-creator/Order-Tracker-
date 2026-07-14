package com.xiaomi.orderapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class DealersActivity : AppCompatActivity() {
    private lateinit var db: DBHelper
    private lateinit var listView: ListView
    private var dealerIds: List<Long> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dealers)
        db = DBHelper(this)

        val etName = findViewById<EditText>(R.id.etDealerName)
        val etShop = findViewById<EditText>(R.id.etShopName)
        listView = findViewById(R.id.lvDealers)

        findViewById<Button>(R.id.btnAddDealer).setOnClickListener {
            val name = etName.text.toString().trim()
            val shop = etShop.text.toString().trim()
            if (name.isNotEmpty()) {
                db.addDealer(name, shop)
                etName.text.clear()
                etShop.text.clear()
                refreshList()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val idToDelete = dealerIds[position]
            AlertDialog.Builder(this)
                .setTitle("Delete dealer?")
                .setPositiveButton("Delete") { _, _ ->
                    db.deleteDealer(idToDelete)
                    refreshList()
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }

        refreshList()
    }

    private fun refreshList() {
        val dealers = db.getAllDealers()
        dealerIds = dealers.map { it.first }
        val display = dealers.map { "${it.second} - ${it.third}" }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
    }
}
