package com.mehrbodmk.factesimchin

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.utils.Helpers
import kotlin.system.exitProcess

class UnhandledExceptionActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_unhandled_exception)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val error = intent.getStringExtra("Error")
        val software = intent.getStringExtra("Software")
        val date = intent.getStringExtra("Date")

        val textViewExceptionDetails = findViewById<TextView>(R.id.textViewUnhandledExceptionDetails)
        textViewExceptionDetails.text =  "Exception details:\n${error}\n\nSoftware info:\n${software}\n\nDate:\n${date}"

        findViewById<AppCompatButton>(R.id.buttonCopyExceptionDetailsToClipboard).setOnClickListener {
            try {
                Helpers.setClipboard(this@UnhandledExceptionActivity,
                    textViewExceptionDetails.text as String
                )
                Toast.makeText(this@UnhandledExceptionActivity, R.string.clipboard_write_success, Toast.LENGTH_SHORT).show()
            }
            catch (ex: Exception)
            {
                Toast.makeText(this@UnhandledExceptionActivity, R.string.clipboard_write_failure, Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<AppCompatButton>(R.id.buttonTerminateGame).setOnClickListener {
            finish()
        }
    }
}