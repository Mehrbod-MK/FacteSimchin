package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayerPresence(
    var playerName: String,
    var isPresent: Boolean = true,
) : Parcelable
