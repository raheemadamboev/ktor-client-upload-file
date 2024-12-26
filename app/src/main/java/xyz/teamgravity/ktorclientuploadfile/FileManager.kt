package xyz.teamgravity.ktorclientuploadfile

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid

class FileManager(
    private val context: Context
) {

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    suspend fun processContentUri(contentUri: Uri): Data {
        return withContext(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(contentUri)?.use { input ->
                input.readBytes()
            } ?: byteArrayOf()
            val name = Uuid.random().toString()
            val mimeType = context.contentResolver.getType(contentUri) ?: ""

            return@withContext Data(
                name = name,
                mimeType = mimeType,
                bytes = bytes
            )
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // MISC
    ///////////////////////////////////////////////////////////////////////////

    class Data(
        val name: String,
        val mimeType: String,
        val bytes: ByteArray
    )
}