package com.mehrbodmk.factesimchin.utils

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore.Audio.Media

class helpers {

    companion object
    {
        private var soundPlayer: MediaPlayer? = null

        fun playSoundEffect(context: Context, soundResourceId: Int)
        {
            if(soundPlayer?.isPlaying == true)
                soundPlayer?.stop()
            soundPlayer = MediaPlayer.create(context, soundResourceId)
            soundPlayer!!.start()
        }
    }
}