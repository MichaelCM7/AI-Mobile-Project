package com.example.mobileaisemproject.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["phone"], unique = true)] // Enforces unique 10-digit phone numbers
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phone: String,
    val password: String // Stores the hashed password
)

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Deletes scan history if the user is deleted
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val image: String,      // Store the saved file path or MediaStore URI string (never raw Bitmap bytes)
    val issueName: String,
    val confidence: Double, // Maps to decimal(4,1)
    val date: Long,         // Epoch millis (System.currentTimeMillis()) for easy chronological sorting
    val remedy: String
)