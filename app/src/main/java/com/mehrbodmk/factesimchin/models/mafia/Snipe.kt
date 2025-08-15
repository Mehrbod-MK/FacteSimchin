package com.mehrbodmk.factesimchin.models.mafia

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.Player
import kotlinx.parcelize.Parcelize

@Parcelize
data class Snipe(
    var sniper: Player,
) : Parcelable
