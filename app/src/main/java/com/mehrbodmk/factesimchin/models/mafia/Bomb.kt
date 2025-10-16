package com.mehrbodmk.factesimchin.models.mafia

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.Player
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bomb(
    var bomber: Player,
    var target: Player,
    var bombCode: Int,
    var detonator: Player? = null,
    var detonationCode: Int? = null,
) : Parcelable
