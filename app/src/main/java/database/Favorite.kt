package com.example.wheremybus.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val busId: String,    // unique ID for the bus
    val busName: String   // display name of the bus
)