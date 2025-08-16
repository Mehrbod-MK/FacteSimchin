package com.mehrbodmk.factesimchin.models

import com.mehrbodmk.factesimchin.AssignRoleCards
import com.mehrbodmk.factesimchin.utils.Constants

enum class RoleTypes(val roleName: String) {
    GODFATHER(AssignRoleCards.ROLE_NAME_GODFATHER),
    MAFIA(AssignRoleCards.ROLE_NAME_MAFIA),
    BOMBER(AssignRoleCards.ROLE_NAME_BOMBER),
    CITIZEN(AssignRoleCards.ROLE_NAME_CITIZEN),
    DETECTIVE(AssignRoleCards.ROLE_NAME_DETECTIVE),
    DOCTOR(AssignRoleCards.ROLE_NAME_DOCTOR),
    SNIPER(AssignRoleCards.ROLE_NAME_SNIPER),
    GUNNER(AssignRoleCards.ROLE_NAME_GUNNER),
    HARDLIVING(AssignRoleCards.ROLE_NAME_HARDLIVING),
    DETONATOR(AssignRoleCards.ROLE_NAME_DETONATOR),
}