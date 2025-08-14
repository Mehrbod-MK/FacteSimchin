package com.mehrbodmk.factesimchin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.commands.SleepOrWakeSomeoneCommand
import com.mehrbodmk.factesimchin.utils.Constants

class SleepOrWakeSomeoneActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sleep_or_wake_anyone)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sleepOrWakeCommand = intent.extras?.getParcelable<SleepOrWakeSomeoneCommand>(Constants.INTENT_SLEEP_OR_WAKE_SOMEONE_COMMAND)!!
        val imageViewSleepCard = findViewById<ImageView>(R.id.imageViewSleepCard)
        val textViewSleepText = findViewById<TextView>(R.id.textViewSleepText)
        val buttonAcceptNightStep = findViewById<AppCompatButton>(R.id.buttonAcceptNightStep)
        imageViewSleepCard.setImageResource(sleepOrWakeCommand.cardImageResId)
        textViewSleepText.text = sleepOrWakeCommand.messageText
        buttonAcceptNightStep.setOnClickListener {
            val acceptIntent = Intent()
            setResult(Activity.RESULT_OK, acceptIntent)
            finish()
        }
    }
}