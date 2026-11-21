package com.example.mobileaisemproject.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Query("UPDATE users SET phone = :newPhone WHERE id = :userId")
    suspend fun updatePhone(userId: Long, newPhone: String)

    @Query("UPDATE users SET phone = :newPhone, password = :newPasswordHash WHERE id = :userId")
    suspend fun updatePhoneAndPassword(userId: Long, newPhone: String, newPasswordHash: String)
}

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertScan(scan: HistoryEntity): Long

    // Automatically updates HomeScreen whenever a scan is added or deleted
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY date DESC")
    fun observeHistoryForUser(userId: Long): Flow<List<HistoryEntity>>

    // Feeds the "Diseases and Pests Identified" counter on ProfileScreen
    @Query("SELECT COUNT(*) FROM history WHERE userId = :userId")
    fun observeScanCount(userId: Long): Flow<Int>

    @Query("DELETE FROM history WHERE id = :scanId")
    suspend fun deleteScanById(scanId: Long)
}
