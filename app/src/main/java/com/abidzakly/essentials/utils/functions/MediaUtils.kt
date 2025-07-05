package com.abidzakly.essentials.utils.functions
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.canhub.cropper.CropImageView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class for media-related operations including image handling,
 * file preview, and media type detection.
 */
object MediaUtils {

    private const val TAG = "MediaUtils"
    private const val JPEG_QUALITY = 100
    private const val PNG_QUALITY = 100

    // Supported image file extensions
    private val SUPPORTED_IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "tiff", "svg"
    )

    /**
     * Checks if a file is an image based on its extension.
     *
     * @param filename The name of the file to check
     * @return true if the file is an image, false otherwise
     */
    fun isImageFile(filename: String): Boolean {
        val fileExtension = filename.substringAfterLast('.', "").lowercase()
        return fileExtension in SUPPORTED_IMAGE_EXTENSIONS
    }

    /**
     * Retrieves the display name of a file from its URI.
     *
     * @param context Application context
     * @param uri The URI of the file
     * @return The display name of the file, or empty string if not found
     */
    fun getFileName(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    cursor.getString(nameIndex) ?: ""
                } else {
                    ""
                }
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file name: ${e.message}", e)
            ""
        }
    }

    /**
     * Converts a Bitmap to a ByteArray in JPEG format.
     *
     * @param bitmap The bitmap to convert
     * @param quality The compression quality (0-100)
     * @return ByteArray representation of the bitmap, or null if conversion fails
     */
    fun bitmapToByteArray(
        bitmap: Bitmap?,
        quality: Int = JPEG_QUALITY
    ): ByteArray? {
        return bitmap?.let {
            try {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                outputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, "Error converting bitmap to byte array: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Opens a file for preview using the appropriate application.
     *
     * @param context Application context
     * @param file The file to preview (can be String path/URL or Bitmap)
     * @param onNoAppFound Callback when no application can handle the file
     */
    fun previewFile(
        context: Context,
        file: Any,
        onNoAppFound: () -> Unit = {}
    ) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        when (file) {
            is String -> handleStringFile(context, file, intent, onNoAppFound)
            is Bitmap -> handleBitmapFile(context, file, intent, onNoAppFound)
            else -> {
                showUnsupportedFileError(context)
                onNoAppFound()
                return
            }
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No application found to handle file: ${e.message}")
            onNoAppFound()
        }
    }

    /**
     * Extracts file path from a Firebase Storage URL.
     *
     * @param url The Firebase Storage URL
     * @return The decoded file path, or null if extraction fails
     */
    fun getFilePathFromUrl(url: String): String? {
        return try {
            val encodedPath = url.substringAfter("/o/").substringBefore("?")
            java.net.URLDecoder.decode(encodedPath, "UTF-8")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting file path from URL: ${e.message}", e)
            null
        }
    }

    /**
     * Extracts a cropped image from CropImageView result.
     *
     * @param resolver ContentResolver instance
     * @param result The crop result from CropImageView
     * @return Cropped bitmap, or null if extraction fails
     */
    fun getCroppedImage(
        resolver: ContentResolver,
        result: CropImageView.CropResult
    ): Bitmap? {
        if (!result.isSuccessful) {
            Log.w(TAG, "Crop result was not successful")
            return null
        }

        val uri = result.uriContent ?: run {
            Log.w(TAG, "Crop result URI is null")
            return null
        }

        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(resolver, uri)
            } else {
                val source = ImageDecoder.createSource(resolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cropped image: ${e.message}", e)
            null
        }
    }

    // Private helper methods

    private fun handleStringFile(
        context: Context,
        file: String,
        intent: Intent,
        onNoAppFound: () -> Unit
    ) {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(file)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)

        if (mimeType == null) {
            showUnsupportedFileError(context)
            onNoAppFound()
            return
        }

        intent.setDataAndType(Uri.parse(file), mimeType)
    }

    private fun handleBitmapFile(
        context: Context,
        bitmap: Bitmap,
        intent: Intent,
        onNoAppFound: () -> Unit
    ) {
        try {
            val fileDir = File(context.cacheDir, "images").apply { mkdirs() }
            val imageFile = File(fileDir, "preview_${System.currentTimeMillis()}.png")

            FileOutputStream(imageFile).use { fileOutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, fileOutputStream)
            }

            val imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                imageFile
            )

            intent.setDataAndType(imageUri, "image/png")
        } catch (e: IOException) {
            Log.e(TAG, "Error processing bitmap for preview: ${e.message}", e)
            Toast.makeText(context, "Error processing image", Toast.LENGTH_SHORT).show()
            onNoAppFound()
        }
    }

    private fun showUnsupportedFileError(context: Context) {
        Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show()
    }
}