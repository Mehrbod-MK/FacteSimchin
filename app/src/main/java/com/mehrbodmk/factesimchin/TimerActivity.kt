package com.mehrbodmk.factesimchin

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.models.GameSession
import com.mehrbodmk.factesimchin.utils.Constants
import com.mehrbodmk.factesimchin.utils.Helpers

class TimerActivity : AppCompatActivity() {

    private lateinit var gameSession: GameSession

    private var countdownTimer: CountDownTimer? = null

    private lateinit var textViewTimerValue: TextView
    private lateinit var buttonTimerAddTime: AppCompatImageButton
    private lateinit var buttonTimerSubtractTime: AppCompatImageButton
    private lateinit var buttonTimerMusicOnOff: AppCompatImageButton
    private lateinit var buttonTimerStartPause: AppCompatImageButton
    private lateinit var buttonTimerReset: AppCompatImageButton

    private var isTimerRunning: Boolean = false
    private var hasSetInitialTimeValue: Boolean = false

    private var mediaPlayerAlarm: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        gameSession = intent.extras?.getParcelable(Constants.INTENT_GAME_SESSION)!!

        textViewTimerValue = findViewById(R.id.textViewTimerValue)
        buttonTimerAddTime = findViewById(R.id.buttonTimerAddTime)
        buttonTimerSubtractTime = findViewById(R.id.buttonTimerSubtractTime)
        buttonTimerMusicOnOff = findViewById(R.id.buttonTimerMusic)
        buttonTimerStartPause = findViewById(R.id.buttonTimerStartPause)
        buttonTimerReset = findViewById(R.id.buttonTimerReset)

        attachEvents()
        updateUI()
        updateButtons()

        // When user presses back button, do not dispose activity, instead, return modified game session.
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                countdownTimer?.cancel()
                mediaPlayerAlarm?.stop()
                val resultIntent = Intent().apply {
                    putExtra(Constants.INTENT_GAME_SESSION, gameSession)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(callback)
    }

    private fun attachEvents()
    {
        textViewTimerValue.setOnClickListener {
            if(!isTimerRunning && !hasSetInitialTimeValue)
            {
                gameSession.timerValue = 0
                updateUI()
            }
        }
        buttonTimerAddTime.setOnClickListener {
            gameSession.timerValue += 5
            updateUI()
            refreshTimer()
            Helpers.playSoundEffect(this@TimerActivity, R.raw.click_object)
            stopAlarmSound()
        }
        buttonTimerSubtractTime.setOnClickListener {
            if(gameSession.timerValue - 5 >= 0)
            {
                gameSession.timerValue -= 5
                updateUI()
                refreshTimer()
                Helpers.playSoundEffect(this@TimerActivity, R.raw.click_object)
            }
            else if(gameSession.timerValue > 0)
            {
                gameSession.timerValue = 0
                updateUI()
                refreshTimer()
                Helpers.playSoundEffect(this@TimerActivity, R.raw.click_object)
            }
            else
            {
                Helpers.playSoundEffect(this@TimerActivity, R.raw.event_bad)
            }
        }
        buttonTimerMusicOnOff.setOnClickListener {
            if(isTimerRunning)
                return@setOnClickListener
            gameSession.isTimerMusicEnabled = !gameSession.isTimerMusicEnabled
            if(gameSession.isTimerMusicEnabled)
            {

                Helpers.playSoundEffect(this@TimerActivity, R.raw.checkbox_on)
            }
            else
            {
                Helpers.playSoundEffect(this@TimerActivity, R.raw.checkbox_off)
            }
            updateButtons()
        }
        buttonTimerStartPause.setOnClickListener {
            if(!hasSetInitialTimeValue)
            {
                gameSession.initialTimerValue = gameSession.timerValue
                hasSetInitialTimeValue = true
            }
            isTimerRunning = !isTimerRunning
            if(isTimerRunning)
            {
                startTimer(gameSession.timerValue)
                Helpers.playSoundEffect(this@TimerActivity, R.raw.checkbox_on)
                buttonTimerStartPause.setImageResource(R.drawable.icon_pause)
            }
            else
            {
                countdownTimer!!.cancel()
                Helpers.playSoundEffect(this@TimerActivity, R.raw.checkbox_off)
                buttonTimerStartPause.setImageResource(R.drawable.icon_play)
                stopAlarmSound()
            }
        }
        buttonTimerReset.setOnClickListener {
            hasSetInitialTimeValue = false
            countdownTimer?.cancel()
            isTimerRunning = false
            buttonTimerStartPause.setImageResource(R.drawable.icon_play)
            gameSession.timerValue = gameSession.initialTimerValue
            stopAlarmSound()
            updateUI()
            Helpers.playSoundEffect(this@TimerActivity, R.raw.click_object)
        }
    }

    private fun startTimer(numSeconds: Int)
    {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer(numSeconds * 1000L + 999L, 1000L)
        {
            override fun onTick(millisUntilFinished: Long) {
                val totalSecondsUntilFinish = millisUntilFinished / 1000
                gameSession.timerValue = totalSecondsUntilFinish.toInt()
                updateUI()
            }

            override fun onFinish() {
                playAlarmSound()
            }
        }
        countdownTimer!!.start()
    }

    private fun refreshTimer()
    {
        if(isTimerRunning)
        {
            startTimer(gameSession.timerValue)
        }
    }

    private fun updateUI()
    {
        val numSeconds: Int = gameSession.timerValue % 60
        val numMinutes: Int = gameSession.timerValue / 60

        textViewTimerValue.text = getString(R.string.timer_value, numMinutes, numSeconds)
    }

    private fun updateButtons()
    {
        if(gameSession.isTimerMusicEnabled)
        {
            buttonTimerMusicOnOff.setImageResource(R.drawable.icon_music)
        }
        else
        {
            buttonTimerMusicOnOff.setImageResource(R.drawable.icon_no_music)
        }
    }

    private fun playAlarmSound()
    {
        if(!gameSession.isTimerMusicEnabled)
            return

        mediaPlayerAlarm = MediaPlayer.create(this@TimerActivity, R.raw.event_attention)
        mediaPlayerAlarm!!.isLooping = true
        mediaPlayerAlarm!!.start()
    }

    private fun stopAlarmSound() {
        if(mediaPlayerAlarm == null)
            return
        mediaPlayerAlarm!!.stop()
        mediaPlayerAlarm!!.release()
        mediaPlayerAlarm = null
    }
}