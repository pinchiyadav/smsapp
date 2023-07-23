package com.example.smsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smsapp.ContactsActivity
import com.example.smsapp.R

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
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }

    }
}