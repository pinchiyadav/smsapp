package com.example.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SMSActivity : AppCompatActivity() {
    private val requestSmsPermission = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        val contactNumber = intent.getStringExtra("CONTACT_NUMBER")

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS),
                requestSmsPermission
            )
        } else {
            contactNumber?.let { displaySMS(it) }
        }
    }

    private fun displaySMS(contactNumber: String) {
        val smsList = getSMSList(contactNumber)
        if (smsList.isEmpty()) {
            Toast.makeText(this, "No SMS found with $contactNumber.", Toast.LENGTH_SHORT).show()
        } else {
            val adapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, smsList)
            val smsListView: ListView = findViewById(R.id.smsListView)
            smsListView.adapter = adapter
        }
    }

    private fun getSMSList(contactNumber: String): List<String> {
        val smsList = mutableListOf<String>()
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.TYPE)

        val uri: Uri = Telephony.Sms.CONTENT_URI
        val selection = "address = ?"
        val selectionArgs = arrayOf(contactNumber)
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC" // Sort by date in descending order
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            val typeIndex = it.getColumnIndex(Telephony.Sms.TYPE)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val type = it.getInt(typeIndex)

                val smsDirection = if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) "Received" else "Sent"
                val smsDetails = "$smsDirection: $address\n$body"
                smsList.add(smsDetails)
            }
        }

        cursor?.close()
        return smsList
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestSmsPermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val contactNumber = intent.getStringExtra("CONTACT_NUMBER")
                    contactNumber?.let { displaySMS(it) }
                } else {
                    Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
