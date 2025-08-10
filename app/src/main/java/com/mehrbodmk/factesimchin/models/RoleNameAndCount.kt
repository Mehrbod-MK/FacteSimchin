package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoleNameAndCount(
    val roleName: String,
    var count: Int
) : Parcelable
