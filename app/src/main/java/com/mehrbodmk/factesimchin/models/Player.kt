package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.mafia.Snipe
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    var name: String,
    var role: Role,
    var isDead: Boolean = false,
    var nightStatus: NightStatus = NightStatus(),
    var showRole: Boolean = false,
    var playerState: PlayerStates = PlayerStates.NORMAL
) : Parcelable