package xyz.teamgravity.ktorclientuploadfile

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn

class FileApi(
    private val client: HttpClient
) {

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun postFile(data: FileManager.Data): Flow<Progress> = channelFlow {
        client.submitFormWithBinaryData(
            url = "https://dlptest.com/https-post/",
            formData = formData {
                append(
                    key = "description",
                    value = "Test"
                )
                append(
                    key = "the_file",
                    value = data.bytes,
                    headers = Headers.build {
                        append(
                            key = HttpHeaders.ContentType,
                            value = data.mimeType
                        )
                        append(
                            key = HttpHeaders.ContentDisposition,
                            value = "filename=${data.name}"
                        )
                    }
                )
            },
            block = {
                onUpload { bytesSentTotal, contentLength ->
                    if (contentLength > 0L) {
                        send(
                            Progress(
                                sent = bytesSentTotal,
                                total = contentLength
                            )
                        )
                    }
                }
            }
        )
    }.flowOn(Dispatchers.IO)

    ///////////////////////////////////////////////////////////////////////////
    // MISC
    ///////////////////////////////////////////////////////////////////////////

    data class Progress(
        val sent: Long,
        val total: Long
    )
}