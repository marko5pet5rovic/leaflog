package com.markopetrovic.leaflog.data.repository
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import android.content.Context
import android.util.Log

class StorageRepository(
    private val cloudinary: Cloudinary
) {
    private val TAG = "StorageRepository"

    suspend fun uploadAvatar(userId: String, uri: Uri, context: Context): String? {
        return uploadFile(
            uri = uri,
            context = context,
            publicId = "users/$userId/avatar",
            folder = "leaflog_avatars"
        )
    }

    suspend fun uploadPlantImage(locationId: String, uri: Uri, context: Context): String? {
        return uploadFile(
            uri = uri,
            context = context,
            publicId = "locations/$locationId/image",
            folder = "leaflog_locations"
        )
    }

    private suspend fun uploadFile(
        uri: Uri,
        context: Context,
        publicId: String,
        folder: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)

                if (inputStream == null) {
                    throw IllegalStateException("Failed to open input stream from Uri.")
                }

                val uploadResult = inputStream.use { stream ->
                    val options = ObjectUtils.asMap(
                        "public_id", publicId,
                        "folder", folder,
                        "overwrite", true
                    )
                    cloudinary.uploader().upload(stream, options)
                }

                uploadResult["secure_url"] as String?

            } catch (e: Exception) {
                Log.e(TAG, "Cloudinary upload failed for $publicId: ${e.message}", e)
                null
            }
        }
    }
}
