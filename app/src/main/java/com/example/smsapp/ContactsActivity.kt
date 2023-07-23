package com.example.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
                val contactName = getContactNameFromNumber(contactNumber)
                if (!contactsList.contains(contactName)) {
                    contactsList.add(contactName)
                }
            }
        }

        cursor?.close()
        return contactsList
    }

    private fun getContactNameFromNumber(contactNumber: String): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(contactNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        var contactName = ""

        val cursor: Cursor? = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                contactName =
                    it.getString(it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))
            }
        }

        cursor?.close()
        return contactName
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
