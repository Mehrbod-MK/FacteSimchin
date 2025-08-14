package com.mehrbodmk.factesimchin.models

import com.mehrbodmk.factesimchin.utils.Constants

enum class RoleTypes(val roleName: String) {
    GODFATHER(Constants.ROLE_NAME_GODFATHER),
    MAFIA(Constants.ROLE_NAME_MAFIA),
    BOMBER(Constants.ROLE_NAME_BOMBER),
    CITIZEN(Constants.ROLE_NAME_CITIZEN),
    DETECTIVE(Constants.ROLE_NAME_DETECTIVE),
    DOCTOR(Constants.ROLE_NAME_DOCTOR),
    SNIPER(Constants.ROLE_NAME_SNIPER),
    GUNNER(Constants.ROLE_NAME_GUNNER),
    DETONATOR(Constants.ROLE_NAME_DETONATOR),
}