package com.example.smsapp

import android.Manifest
import android.content.Intent
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

class ContactsActivity : AppCompatActivity() {
    private val requestSmsPermission = 123
    private val requestContactsPermission = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

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
            displayContactsWithSMS()
        }
    }

    private fun displayContactsWithSMS() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                requestContactsPermission
            )
        } else {
            val contactsList = getContactsList()
            if (contactsList.isEmpty()) {
                Toast.makeText(this, "No contacts with SMS found.", Toast.LENGTH_SHORT).show()
            } else {
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contactsList)
                val contactListView: ListView = findViewById(R.id.contactListView)
                contactListView.adapter = adapter
                contactListView.setOnItemClickListener { _, _, position, _ ->
                    val selectedContactNumber = contactsList[position]
                    openSMSActivity(selectedContactNumber)
                }
            }
        }
    }

    private fun getContactsList(): List<String> {
        val contactsList = mutableListOf<String>()
        val projection = arrayOf(Telephony.Sms.ADDRESS)

        val uri: Uri = Telephony.Sms.CONTENT_URI
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC" // Sort by date in descending order
        )

        cursor?.use {
            val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS)
            while (it.moveToNext()) {
                val contactNumber = it.getString(addressIndex)
                if (!contactsList.contains(contactNumber)) {
                    contactsList.add(contactNumber)
                }
            }
        }

        cursor?.close()
        return contactsList
    }

    private fun openSMSActivity(contactNumber: String) {
        val intent = Intent(this, SMSActivity::class.java)
        intent.putExtra("CONTACT_NUMBER", contactNumber)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestSmsPermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayContactsWithSMS()
                } else {
                    Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
                }
            }

            requestContactsPermission -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayContactsWithSMS()
                } else {
                    Toast.makeText(this, "Contacts permission denied.", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }
}
