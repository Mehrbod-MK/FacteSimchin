package com.mehrbodmk.factesimchin.models

data class GameSession(
    var players: MutableList<Player>,
    var round: Int
)