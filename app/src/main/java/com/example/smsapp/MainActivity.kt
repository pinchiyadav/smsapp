package com.example.smsapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var btnSend: Button
    private lateinit var btnContacts: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSend = findViewById(R.id.button)
        btnContacts = findViewById(R.id.button2)

        btnSend.setOnClickListener {
            val intent = Intent(this, Send::class.java)
            startActivity(intent)
        }

        btnContacts.setOnClickListener {
            showKeyInputDialog()
        }
    }

    private fun showKeyInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_key_input, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Enter Key")

        val alertDialog = dialogBuilder.create()

        val editTextKey = dialogView.findViewById<EditText>(R.id.editTextKey)
        val buttonSubmit = dialogView.findViewById<Button>(R.id.buttonSubmit)

        buttonSubmit.setOnClickListener {
            val key = editTextKey.text.toString().trim()
            rcs.keys.aeskey = key
            if (key.isNotEmpty()) {
                // Save the key to a preference or global variable for later use
                // For example, you can use SharedPreferences to save and access the key in other parts of the app
                alertDialog.dismiss()
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
            } else {
                editTextKey.error = "Key cannot be empty"
            }
        }

        alertDialog.show()
    }
}
