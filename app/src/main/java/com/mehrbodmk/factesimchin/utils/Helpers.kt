package com.mehrbodmk.factesimchin.utils

import android.app.AlertDialog
import android.content.ClipData
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.text.ClipboardManager
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

        fun displaySimplePopup(context:Context, theme: Int, title: String, message: String,
                               okButtonText: String,
                               dialogOpenSoundId: Int, dialogCloseSoundId: Int,
                               onOk: () -> Unit)
        {
            val builder = AlertDialog.Builder(context, theme)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(okButtonText, { dialog, _ ->
                    dialog.dismiss()
                    playSoundEffect(context, dialogCloseSoundId)
                    onOk.invoke()
                })
            playSoundEffect(context, dialogOpenSoundId)
            builder.show()
        }

        fun setClipboard(context: Context, text: String) {
            val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
        }
    }
}