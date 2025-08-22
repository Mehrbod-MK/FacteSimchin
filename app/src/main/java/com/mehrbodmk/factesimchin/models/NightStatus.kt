package com.mehrbodmk.factesimchin.models

import android.os.Parcelable
import com.mehrbodmk.factesimchin.models.mafia.Snipe
import kotlinx.parcelize.Parcelize

@Parcelize
data class NightStatus(
    var isShotByGodfather: Boolean = false,
    var isNatoed: Boolean = false,
    var guessedNatoRole: RoleTypes? = null,
    var isSavedByDoctor: Boolean = false,
    var snipedBy: Snipe? = null,
    var hasDummyBullet: Boolean = false,
    var hasWarBullet: Boolean = false,
    var isTalkedIntoMafia: Boolean = false,
    var isDrunk: Boolean = false,
) : Parcelable
