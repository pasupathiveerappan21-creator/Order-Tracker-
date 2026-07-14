package com.xiaomi.orderapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.widget.Button>(R.id.btnDealers).setOnClickListener {
            startActivity(Intent(this, DealersActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnProducts).setOnClickListener {
            startActivity(Intent(this, ProductsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnNewOrder).setOnClickListener {
            startActivity(Intent(this, OrderActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnOrderHistory).setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnBackup).setOnClickListener {
            startActivity(Intent(this, BackupActivity::class.java))
        }
    }
}
