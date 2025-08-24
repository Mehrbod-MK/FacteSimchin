package com.mehrbodmk.factesimchin

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mehrbodmk.factesimchin.utils.Helpers

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var textViewAppVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewAppVersion = findViewById(R.id.textViewAppVersion)
        textViewAppVersion.text = getString(R.string.mordad_1404_with_version, getAppVersionString())

        animateMafiaIcon()
        Helpers.playMainMenuMusic(this@MainActivity)

        val buttonStartGame = findViewById<AppCompatButton>(R.id.buttonStartGame)
        buttonStartGame.setOnClickListener {
            Helpers.playSoundEffect(this@MainActivity, R.raw.button)
            startActivity(Intent(this@MainActivity, PlayersActivity::class.java))
        }
    }

    private fun getAppVersionString(): String
    {
        val packageManager = this.packageManager
        val packageName = this.packageName
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        return versionName!!
    }

    private fun animateMafiaIcon()
    {
        val imageViewMafiaIcon = findViewById<ImageView>(R.id.imageViewMafiaIcon)
        val animator = ObjectAnimator.ofFloat(imageViewMafiaIcon, "rotationY", 0f, 360f)
        animator.duration = 3000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.start()
    }
}