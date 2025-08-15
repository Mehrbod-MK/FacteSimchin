package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Role(
    val name: String,
    val localName: String,
    val isMafia: Boolean?,
    val type: RoleTypes
) : Parcelable
