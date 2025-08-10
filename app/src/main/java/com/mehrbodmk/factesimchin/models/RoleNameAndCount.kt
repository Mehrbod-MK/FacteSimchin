package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoleNameAndCount(
    val roleName: String,
    val count: Int
) : Parcelable
