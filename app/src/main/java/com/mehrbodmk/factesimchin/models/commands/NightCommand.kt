package com.mehrbodmk.factesimchin.models.commands

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.Player
import kotlinx.parcelize.Parcelize

@Parcelize
data class NightCommand(
    val sourcePlayer: Player,
    val mission: Missions,
    val targetPlayer: Player,
) : Parcelable