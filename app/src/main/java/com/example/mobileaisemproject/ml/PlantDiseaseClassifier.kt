package com.example.mobileaisemproject.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlin.math.min

data class DiseaseRecord(
    val id: String,
    val name: String,
    val confidence: String,
    val date: String,
    val remedy: String = "",
    val image: Bitmap? = null
)

data class ScanResult(
    val diseaseName: String,
    val confidence: Float,
    val remedy: String,
    val croppedImage: Bitmap
)

/**
 * Decodes a Uri, fixes camera EXIF orientation, and center-crops to a 1:1 square.
 */
fun loadAndCropSquareBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        val uprightBitmap = if (orientation != ExifInterface.ORIENTATION_NORMAL) {
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }

        // Center-crop to 1:1 square matching the CameraScreen preview frame
        val size = min(uprightBitmap.width, uprightBitmap.height)
        val xOffset = (uprightBitmap.width - size) / 2
        val yOffset = (uprightBitmap.height - size) / 2
        Bitmap.createBitmap(uprightBitmap, xOffset, yOffset, size, size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun mmapAsset(context: Context, assetName: String): ByteBuffer {
    return try {
        val fileDescriptor = context.assets.openFd(assetName)
        val inputStream = fileDescriptor.createInputStream()
        val channel = inputStream.channel
        channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    } catch (e: Exception) {
        val tempFile = File(context.cacheDir, "temp_$assetName")
        if (!tempFile.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        val fileChannel = RandomAccessFile(tempFile, "r").channel
        fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
    }
}

class PlantDiseaseClassifier(context: Context) {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var labels: List<String> = emptyList()
    private val remedyManager = RemedyManager(context)
    var initializationError: String? = null
        private set

    init {
        runCatching {
            env = OrtEnvironment.getEnvironment()
            val mappedBuffer = mmapAsset(context, "crop_disease_model.onnx")
            session = env?.createSession(mappedBuffer, OrtSession.SessionOptions())
            labels = context.assets.open("labels.txt").bufferedReader().readLines()
        }.onFailure { e ->
            initializationError = e.localizedMessage ?: "Failed to load CNN model"
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun classifyAndGetResult(squareBitmap: Bitmap): ScanResult {
        val currentSession = session
        val currentEnv = env
        if (currentSession == null || currentEnv == null) {
            val err = initializationError ?: "Classifier non-functional"
            return ScanResult(err, 0.0f, remedyManager.getRemedy(err), squareBitmap)
        }

        return try {
            val targetSize = 224
            val resized = Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
            val inputBuffer = FloatBuffer.allocate(1 * 3 * targetSize * targetSize)

            val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
            val std = floatArrayOf(0.229f, 0.224f, 0.225f)

            val pixels = IntArray(targetSize * targetSize)
            resized.getPixels(pixels, 0, targetSize, 0, 0, targetSize, targetSize)

            for (i in 0 until targetSize) {
                for (j in 0 until targetSize) {
                    val pixel = pixels[i * targetSize + j]
                    val r = (((pixel shr 16) and 0xFF) / 255.0f - mean[0]) / std[0]
                    val g = (((pixel shr 8) and 0xFF) / 255.0f - mean[1]) / std[1]
                    val b = ((pixel and 0xFF) / 255.0f - mean[2]) / std[2]

                    val spatialIdx = i * targetSize + j
                    inputBuffer.put(0 * targetSize * targetSize + spatialIdx, r)
                    inputBuffer.put(1 * targetSize * targetSize + spatialIdx, g)
                    inputBuffer.put(2 * targetSize * targetSize + spatialIdx, b)
                }
            }
            inputBuffer.rewind()

            val inputName = currentSession.inputNames.iterator().next()
            val shape = longArrayOf(1, 3, targetSize.toLong(), targetSize.toLong())
            val inputTensor = OnnxTensor.createTensor(currentEnv, inputBuffer, shape)

            val results = currentSession.run(mapOf(inputName to inputTensor))
            val rawOutput = (results[0].value as Array<FloatArray>)[0]

            val exps = rawOutput.map { exp(it.toDouble()).toFloat() }
            val sumExps = exps.sum()
            val probs = exps.map { it / sumExps }

            val maxIdx = probs.indices.maxByOrNull { probs[it] } ?: 0
            val label = labels.getOrElse(maxIdx) { "Unknown" }
            val confidence = probs[maxIdx] * 100f
            val remedy = remedyManager.getRemedy(label)

            ScanResult(
                diseaseName = label,
                confidence = confidence,
                remedy = remedy,
                croppedImage = squareBitmap
            )
        } catch (e: Exception) {
            val msg = "Inference error: ${e.message}"
            ScanResult(msg, 0.0f, remedyManager.getRemedy(msg), squareBitmap)
        }
    }

    fun close() {
        runCatching {
            session?.close()
            env?.close()
        }
    }
}

class RemedyManager(context: Context) {
    private val remedyMap = mutableMapOf<String, String>()
    private val sanitizedRemedyMap = mutableMapOf<String, String>()

    init {
        try {
            val jsonStr = context.assets.open("remedy-map.json").bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonStr)

            jsonObject.keys().forEach { key ->
                val remedyText = jsonObject.getString(key)
                remedyMap[key] = remedyText
                sanitizedRemedyMap[sanitize(key)] = remedyText
            }
            Log.d("RemedyManager", "Successfully loaded ${remedyMap.size} remedies from JSON.")
        } catch (e: Exception) {
            Log.e("RemedyManager", "CRITICAL ERROR: Failed to load remedy-map.json", e)
        }
    }

    fun getRemedy(diseaseName: String): String {
        if (remedyMap.isEmpty()) {
            return "Error: Could not load remedies asset file. Check logcat for details."
        }

        remedyMap[diseaseName]?.let { return it }

        val cleanInput = sanitize(diseaseName)
        sanitizedRemedyMap[cleanInput]?.let { return it }

        for ((sanitizedKey, remedyText) in sanitizedRemedyMap) {
            if (cleanInput.contains(sanitizedKey) || sanitizedKey.contains(cleanInput)) {
                return remedyText
            }
        }

        val inputTokens = cleanInput.split(" ").filter { it.length > 2 }.toSet()
        var maxOverlap = 0
        var bestMatch: String? = null

        for ((sanitizedKey, remedyText) in sanitizedRemedyMap) {
            val keyTokens = sanitizedKey.split(" ").filter { it.length > 2 }.toSet()
            val overlap = inputTokens.intersect(keyTokens).size
            if (overlap > maxOverlap) {
                maxOverlap = overlap
                bestMatch = remedyText
            }
        }

        if (bestMatch != null && maxOverlap >= 1) {
            return bestMatch
        }

        if (cleanInput.contains("healthy")) {
            return remedyMap["Healthy"]
                ?: remedyMap.entries.firstOrNull { it.key.lowercase().contains("healthy") }?.value
                ?: "Plant appears healthy. Continue regular care and routine monitoring."
        }

        return remedyMap["Default"]
            ?: sanitizedRemedyMap["default"]
            ?: "Prune affected leaves, maintain proper airflow, and apply copper-based fungicide if needed."
    }

    private fun sanitize(text: String): String {
        return text.replace("_", " ")
            .replace("-", " ")
            .replace(Regex("[^a-zA-Z0-9 ]"), "")
            .trim()
            .lowercase()
    }
}