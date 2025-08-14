package com.mehrbodmk.factesimchin.models.commands

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SleepOrWakeSomeoneCommand(
    val cardImageResId: Int,
    val messageText: String,
) : Parcelable
