package com.xiaomi.orderapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupActivity : AppCompatActivity() {
    private lateinit var db: DBHelper

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) restoreFromUri(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup)
        db = DBHelper(this)

        findViewById<Button>(R.id.btnExport).setOnClickListener { exportBackup() }
        findViewById<Button>(R.id.btnImport).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Restore backup?")
                .setMessage("This replaces all current dealers, products and order history with the content of the file you pick.")
                .setPositiveButton("Choose file") { _, _ -> pickFileLauncher.launch("*/*") }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun exportBackup() {
        val json = db.exportToJson()
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getExternalFilesDir(null), "orderapp_backup_$stamp.json")
        file.writeText(json)

        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "application/json"
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(sendIntent, "Save backup to..."))
    }

    private fun restoreFromUri(uri: Uri) {
        try {
            val text = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw Exception("Could not read file")
            db.importFromJson(text)
            Toast.makeText(this, "Backup restored", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not restore: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
