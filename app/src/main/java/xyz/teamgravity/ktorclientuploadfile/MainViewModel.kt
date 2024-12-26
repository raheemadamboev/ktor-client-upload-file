package xyz.teamgravity.ktorclientuploadfile

import android.net.Uri
import androidx.annotation.FloatRange
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import okio.FileNotFoundException
import java.nio.channels.UnresolvedAddressException

class MainViewModel(
    private val repository: FileRepository
) : ViewModel() {

    var state: State by mutableStateOf(State())
        private set

    private var job: Job? = null

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    fun onUpload(contentUri: Uri) {
        viewModelScope.launch {
            job = repository.uploadFile(contentUri)
                .onStart {
                    state = state.copy(
                        isUploading = true,
                        isUploadingCompleted = false,
                        progress = 0F,
                        errorMessage = null
                    )
                }
                .onEach { data ->
                    state = state.copy(
                        progress = data.sent / data.total.toFloat()
                    )
                }
                .onCompletion { cause ->
                    when (cause) {
                        is CancellationException -> {
                            state = state.copy(
                                isUploading = false,
                                isUploadingCompleted = false,
                                progress = 0F,
                                errorMessage = "The upload was cancelled!"
                            )
                        }

                        null -> {
                            state = state.copy(
                                isUploading = false,
                                isUploadingCompleted = true,
                                progress = 1F,
                                errorMessage = null
                            )
                        }
                    }
                }
                .catch { cause ->
                    val message = when (cause) {
                        is OutOfMemoryError -> "File too large!"
                        is FileNotFoundException -> "File not found!"
                        is UnresolvedAddressException -> "No internet"
                        else -> "Something went wrong!"
                    }
                    state = state.copy(
                        isUploading = false,
                        errorMessage = message
                    )
                }
                .launchIn(this)
        }
    }

    fun onCancel() {
        job?.cancel()
        job = null
    }

    ///////////////////////////////////////////////////////////////////////////
    // MISC
    ///////////////////////////////////////////////////////////////////////////

    data class State(
        val isUploading: Boolean = false,
        val isUploadingCompleted: Boolean = false,
        @FloatRange(from = 0.0, to = 1.0) val progress: Float = 0F,
        val errorMessage: String? = null
    )
}