package com.example.wheremybus.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface FavoriteDao {

    @Insert
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorites")
    suspend fun getAllFavorites(): List<Favorite>

    @Query("SELECT * FROM favorites WHERE busId = :busId LIMIT 1")
    suspend fun getFavoriteByBusId(busId: String): Favorite?
}