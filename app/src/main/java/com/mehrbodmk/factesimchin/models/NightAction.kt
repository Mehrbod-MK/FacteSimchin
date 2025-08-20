package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NightAction(
    val cardResId: Int,
    val roleLocalName: String,
    val roleType: RoleTypes,
    val verbString: String,
    val candidateSourcePlayers: List<Player>,
    val missions: ArrayList<Missions>,
    val candidateTargetPlayers: List<Player>,
) : Parcelable
