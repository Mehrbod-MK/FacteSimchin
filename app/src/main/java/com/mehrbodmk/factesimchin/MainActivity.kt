package com.mehrbodmk.factesimchin

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.PowerManager
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.utils.helpers
import kotlin.concurrent.thread
import kotlin.time.Duration

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        animateMafiaIcon()
        playMainMenuMusic()

        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStartGame)
        buttonStartGame.setOnClickListener {
            helpers.playSoundEffect(this@MainActivity, R.raw.button)
        }
    }

    private fun animateMafiaIcon()
    {
        val imageViewMafiaIcon = findViewById<ImageView>(R.id.imageViewMafiaIcon)
        val animator = ObjectAnimator.ofFloat(imageViewMafiaIcon, "rotationY", 0f, 360f)
        animator.duration = 3000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()
    }

    override fun onPause() {
        super.onPause()
        if(mediaPlayer.isPlaying)
            mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        if(!mediaPlayer.isPlaying)
            mediaPlayer.start()
    }

    private fun playMainMenuMusic()
    {
        mediaPlayer = MediaPlayer.create(this, R.raw.main_menu)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }
}