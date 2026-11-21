package com.example.mobileaisemproject.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.mobileaisemproject.ml.DiseaseRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageStorage {
    /**
     * Compresses the cropped 224x224 leaf Bitmap to JPEG inside internal storage (context.filesDir)
     * and returns its absolute file path to store in Room's `image` column.
     */
    suspend fun saveCroppedBitmap(context: Context, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val scansDir = File(context.filesDir, "plant_scans").apply { mkdirs() }
        val file = File(scansDir, "scan_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        file.absolutePath
    }

    /**
     * Loads a Bitmap back from a stored file path (returns null if the file is missing).
     */
    fun loadBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        val file = File(path)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    /**
     * Removes the image file from internal storage when the user deletes a scan record.
     */
    fun deleteBitmapFile(path: String) {
        if (path.isBlank()) return
        val file = File(path)
        if (file.exists()) file.delete()
    }
}

object PasswordSecurity {
    /**
     * Produces a 60-character BCrypt hash (fits `varchar(60)`).
     */
    fun hashPassword(plainPassword: String): String {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray())
    }

    /**
     * Verifies a plaintext login attempt against the stored 60-character BCrypt hash.
     */
    fun verifyPassword(plainPassword: String, storedHash: String): Boolean {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash).verified
    }
}

/**
 * Converts a Room `HistoryEntity` into the `DiseaseRecord` model your UI screens expect.
 */
fun HistoryEntity.toDiseaseRecord(): DiseaseRecord {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = formatter.format(Date(this.date))

    return DiseaseRecord(
        id = this.id.toString(),
        name = this.issueName,
        confidence = "Confidence: ${"%.1f".format(this.confidence)}%",
        date = formattedDate,
        remedy = this.remedy,
        image = ImageStorage.loadBitmap(this.image),
        imagePath = this.image
    )
}