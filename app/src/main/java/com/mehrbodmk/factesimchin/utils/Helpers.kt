package com.mehrbodmk.factesimchin.utils

import android.content.Context
import android.media.MediaPlayer
import com.mehrbodmk.factesimchin.R

class Helpers {

    companion object
    {
        private var soundPlayer: MediaPlayer? = null
        private var mainMenuSoundPlayer: MediaPlayer? = null

        fun playSoundEffect(context: Context, soundResourceId: Int)
        {
            if(soundPlayer?.isPlaying == true)
                soundPlayer?.stop()
            soundPlayer = MediaPlayer.create(context, soundResourceId)
            soundPlayer!!.start()
        }

        fun playMainMenuMusic(context: Context)
        {
            if(mainMenuSoundPlayer?.isPlaying == true)
                mainMenuSoundPlayer?.stop()
            mainMenuSoundPlayer = MediaPlayer.create(context, R.raw.main_menu)
            mainMenuSoundPlayer!!.isLooping = true
            mainMenuSoundPlayer!!.start()
        }

        fun stopMainMenuMusic()
        {
            if(mainMenuSoundPlayer?.isPlaying == true)
                mainMenuSoundPlayer?.stop()
        }

        fun resumeMainMenuMusic()
        {
            if(mainMenuSoundPlayer?.isPlaying == false)
                mainMenuSoundPlayer?.start()
        }
    }
}