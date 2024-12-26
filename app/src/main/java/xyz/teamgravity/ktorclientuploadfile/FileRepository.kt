package xyz.teamgravity.ktorclientuploadfile

import android.net.Uri
import kotlinx.coroutines.flow.Flow

class FileRepository(
    private val api: FileApi,
    private val manager: FileManager
) {

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    suspend fun uploadFile(contentUri: Uri): Flow<FileApi.Progress> {
        return api.postFile(manager.processContentUri(contentUri))
    }
}