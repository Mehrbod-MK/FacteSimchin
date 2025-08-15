package com.mehrbodmk.factesimchin.models.commands

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.Missions
import com.mehrbodmk.factesimchin.models.Player
import com.mehrbodmk.factesimchin.models.RoleTypes
import kotlinx.parcelize.Parcelize

@Parcelize
data class NightCommand(
    val sourcePlayer: Player,
    val mission: Missions,
    val targetPlayer: Player,
    val natoGuessedRole: RoleTypes?,
    val bombCode: Int?,
) : Parcelable