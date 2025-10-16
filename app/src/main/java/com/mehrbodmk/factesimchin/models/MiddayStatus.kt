package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.mafia.Bomb
import kotlinx.parcelize.Parcelize

@Parcelize
data class MiddayStatus(
    var wakeDetonator:  Boolean = false,
    var wakeMayor:  Boolean = false,
) : Parcelable
