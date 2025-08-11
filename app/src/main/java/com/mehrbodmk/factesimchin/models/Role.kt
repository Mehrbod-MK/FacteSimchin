package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Role(
    var name: String,
    var isMafia: Boolean?
) : Parcelable
