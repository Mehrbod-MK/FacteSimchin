package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GameSession(
    var players: ArrayList<Player>,
    var round: Int,
    var timerValue: Int = 0,
    var initialTimerValue: Int = 30,
    var isTimerMusicEnabled: Boolean = true
) : Parcelable