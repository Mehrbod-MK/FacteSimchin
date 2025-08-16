package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RoleTypeAndCount(
    val roleType: RoleTypes,
    var count: Int
) : Parcelable
