package com.example.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Send : AppCompatActivity() {
    private lateinit var editTextMobileNumber: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        editTextMobileNumber = findViewById(R.id.editTextMobileNumber)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)

        buttonSend.setOnClickListener {
            val mobileNumber = editTextMobileNumber.text.toString()
            val message = editTextMessage.text.toString()

            if (mobileNumber.isNotEmpty() && message.isNotEmpty()) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.SEND_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.SEND_SMS),
                        SMS_PERMISSION_CODE
                    )
                } else {
                    sendSMS(mobileNumber, message)
                }
            } else {
                Toast.makeText(this, "Please enter a mobile number and message", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun encrypt(input: String, key: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(StandardCharsets.UTF_8), "AES")
        val iv = generateRandomIV()
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(input.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun generateRandomIV(): ByteArray {
        val iv = ByteArray(16)
        val secureRandom = SecureRandom()
        secureRandom.nextBytes(iv)
        return iv
    }

    private fun sendSMS(mobileNumber: String, message: String) {


        var plaintext = message
        var encryptedText = ""

        println("Enter the 16-letter key:")
        val key = generateRandomString(16)

        if (plaintext != null && key != null && key.length == 16) {
            encryptedText = encrypt(plaintext, key)
            println("Encrypted Text: $encryptedText")
        } else {
            println("Invalid input. Make sure you enter both the plaintext and a 16-letter key.")
        }
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(mobileNumber, null, encryptedText, null, null)
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    fun generateRandomString(length: Int): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        val random = SecureRandom()
        val randomString = StringBuilder(length)

        repeat(length) {
            val randomIndex = random.nextInt(charset.length)
            randomString.append(charset[randomIndex])
        }

        return randomString.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val mobileNumber = editTextMobileNumber.text.toString()
                val message = editTextMessage.text.toString()
                sendSMS(mobileNumber, message)
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val SMS_PERMISSION_CODE = 123
    }
}
