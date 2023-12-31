package com.example.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SMSActivity : AppCompatActivity() {
    private val requestSmsPermission = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)

        val contactName = intent.getStringExtra("CONTACT_NAME")
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
            contactName?.let {
                contactNumber?.let { number ->
                    displaySMS(it, number)
                }
            }
        }
    }

    private fun displaySMS(contactName: String, contactNumber: String) {
        val smsList = getSMSList(contactNumber)
        if (smsList.isEmpty()) {
            Toast.makeText(this, "No SMS found with $contactName.", Toast.LENGTH_SHORT).show()
        } else {
            val adapter =
                SMSAdapter(this, smsList)
            val smsListView: ListView = findViewById(R.id.smsListView)
            smsListView.adapter = adapter
        }
    }

    private fun getSMSList(contactNumber: String): List<SMSItem> {
        val smsList = mutableListOf<SMSItem>()
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.TYPE, Telephony.Sms.DATE)

        val uri: Uri = Telephony.Sms.CONTENT_URI
        val selection = "address = ? OR address = ?"
        val selectionArgs = arrayOf(contactNumber, contactNumber)
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
            val dateIndex = it.getColumnIndex(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val address = it.getString(addressIndex)
                val body = it.getString(bodyIndex)
                val type = it.getInt(typeIndex)
                val date = it.getLong(dateIndex)

                val smsDirection = if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) "Received" else "Sent"
                val smsDetails = SMSItem("$smsDirection: $address", body, date)
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
                    val contactName = intent.getStringExtra("CONTACT_NAME")
                    val contactNumber = intent.getStringExtra("CONTACT_NUMBER")
                    contactName?.let {
                        contactNumber?.let { number ->
                            displaySMS(it, number)
                        }
                    }
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

data class SMSItem(val address: String, val body: String, val date: Long)

class SMSAdapter(
    context: AppCompatActivity,
    private val smsList: List<SMSItem>
) : ArrayAdapter<SMSItem>(context, 0, smsList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_sms, parent, false)
        }

        val smsItem = smsList[position]
        val address = smsItem.address
        val body = smsItem.body
        val date = smsItem.date

        val addressTextView: TextView = view!!.findViewById(R.id.addressTextView)
        addressTextView.text = address

        val bodyTextView: TextView = view.findViewById(R.id.bodyTextView)
        bodyTextView.text = body

        val dateTextView: TextView = view.findViewById(R.id.dateTextView)
        dateTextView.text = DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR)

        return view
    }
}
