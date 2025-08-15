package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NightAction(
    val roleLocalName: String,
    val verbString: String,
    val candidateSourcePlayers: ArrayList<Player>,
    val missions: ArrayList<Missions>,
    val candidateTargetPlayers: ArrayList<Player>,
) : Parcelable
