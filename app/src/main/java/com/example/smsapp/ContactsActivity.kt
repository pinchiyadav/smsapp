package com.example.smsapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
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
                val adapter = ContactAdapter(this, contactsList)
                val contactListView: ListView = findViewById(R.id.contactListView)
                contactListView.adapter = adapter
                contactListView.setOnItemClickListener { _, _, position, _ ->
                    val selectedContactNumber = contactsList[position].second
                    val selectedContactName = contactsList[position].first
                    openSMSActivity(selectedContactName, selectedContactNumber)
                }
            }
        }
    }

    private fun getContactsList(): List<Pair<String, String>> {
        val contactsList = mutableListOf<Pair<String, String>>()
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
                if (!contactsList.any { pair -> pair.second == contactNumber }) {
                    contactsList.add(Pair(contactName, contactNumber))
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

    private fun openSMSActivity(contactName: String, contactNumber: String) {
        val intent = Intent(this, SMSActivity::class.java)
        intent.putExtra("CONTACT_NAME", contactName)
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

class ContactAdapter(
    context: Context,
    private val contactsList: List<Pair<String, String>>
) : ArrayAdapter<Pair<String, String>>(context, 0, contactsList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        }

        val contactItem = contactsList[position]
        val contactName = contactItem.first
        val contactNumber = contactItem.second

        val textView: TextView = view as TextView
        textView.text = "$contactName ($contactNumber)"

        return view
    }
}
