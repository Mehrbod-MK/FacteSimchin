package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Player(
    var name: String,
    var role: Role,
    var isDead: Boolean = false,
) : Parcelable