package com.mehrbodmk.factesimchin.player.useCases

import com.mehrbodmk.factesimchin.db.DaoBase
import com.mehrbodmk.factesimchin.models.PlayerPresence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

interface UseCasePlayer {

    fun list(): Flow<List<PlayerPresence>>
    suspend fun addNewPlayer(name: String) : Result<Long>
}

class UseCasePlayerImpl(
    val daoBase: DaoBase
) : UseCasePlayer {

    override fun list(): Flow<List<PlayerPresence>> {
        return daoBase.players().flowOn(Dispatchers.IO)
    }

    override suspend fun addNewPlayer(name: String): Result<Long> {
        return withContext(Dispatchers.IO) {
            runCatching {
                for(index in 1..20) {
                    daoBase.insert(PlayerPresence(name = "$name $index") )
                }
                1
            }
        }
    }
}