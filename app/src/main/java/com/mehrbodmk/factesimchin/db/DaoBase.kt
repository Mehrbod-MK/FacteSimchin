package com.mehrbodmk.factesimchin.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mehrbodmk.factesimchin.models.PlayerPresence
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoBase {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(player : PlayerPresence) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(player : PlayerPresence)

    @Query("SELECT * from players")
    fun players() : Flow<List<PlayerPresence>>

    @Query("SELECT * from players WHERE id = :id")
    fun player(id : Long) : Flow<PlayerPresence>

    @Query("SELECT * from players WHERE name like '%' || :name || '%'")
    fun player(name : String) : Flow<PlayerPresence>
}