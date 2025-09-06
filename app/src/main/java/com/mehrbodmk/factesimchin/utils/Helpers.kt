package com.mehrbodmk.factesimchin.utils

import android.app.AlertDialog
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
            {
                soundPlayer?.stop()
                soundPlayer?.release()
            }
            soundPlayer = MediaPlayer.create(context, soundResourceId)
            soundPlayer?.setOnCompletionListener {
                it.release()
                soundPlayer = null
            }
            soundPlayer?.start()
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
            try {
                if(mainMenuSoundPlayer?.isPlaying == true)
                {
                    mainMenuSoundPlayer?.stop()
                    mainMenuSoundPlayer?.release()
                }
            }
            catch (ex: IllegalStateException) { /* Do nothing. */ }
        }

        fun askUserYesNo(context:Context, theme: Int, title: String, message: String,
                         positiveButtonText: String, negativeButtonText: String,
                         dialogOpenSoundId: Int, dialogCloseSoundId: Int,
                         onYes: () -> Unit, onNo: () -> Unit)
        {
            val builder = AlertDialog.Builder(context, theme)
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(positiveButtonText, { dialog, _ ->
                dialog.dismiss()
                playSoundEffect(context, dialogCloseSoundId)
                onYes.invoke()
            })
            builder.setNegativeButton(negativeButtonText, { dialog, _ ->
                dialog.dismiss()
                playSoundEffect(context, dialogCloseSoundId)
                onNo.invoke()
            })
            playSoundEffect(context, dialogOpenSoundId)
            builder.show()
        }
    }
}