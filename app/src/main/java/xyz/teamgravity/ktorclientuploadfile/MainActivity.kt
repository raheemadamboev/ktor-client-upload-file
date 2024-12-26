package xyz.teamgravity.ktorclientuploadfile

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.teamgravity.ktorclientuploadfile.ui.theme.KtorClientUploadFileTheme
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KtorClientUploadFileTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    val viewmodel = viewModel {
                        MainViewModel(
                            repository = FileRepository(
                                api = FileApi(
                                    client = HttpClientProvider.VALUE
                                ),
                                manager = FileManager(
                                    context = applicationContext
                                )
                            )
                        )
                    }
                    val state = viewmodel.state
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent(),
                        onResult = { contentUri ->
                            contentUri?.let(viewmodel::onUpload)
                        }
                    )

                    LaunchedEffect(
                        key1 = state.errorMessage,
                        block = {
                            state.errorMessage?.let { message ->
                                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    LaunchedEffect(
                        key1 = state.isUploadingCompleted,
                        block = {
                            if (state.isUploadingCompleted) {
                                Toast.makeText(applicationContext, "Uploading is completed!", Toast.LENGTH_LONG).show()
                            }
                        }
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        if (state.isUploading) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val progressText by remember(state.progress) { derivedStateOf { (state.progress * 100).roundToInt().toString() } }
                                val progress by animateFloatAsState(
                                    targetValue = state.progress,
                                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                                    label = "progress"
                                )

                                LinearProgressIndicator(
                                    progress = { progress },
                                    gapSize = (-15).dp,
                                    drawStopIndicator = {},
                                    modifier = Modifier.fillMaxWidth(0.8F)
                                )
                                Text(
                                    text = progressText
                                )
                                Button(
                                    onClick = viewmodel::onCancel
                                ) {
                                    Text(
                                        text = "Cancel"
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    launcher.launch("*/*")
                                }
                            ) {
                                Text(
                                    text = "Pick a file"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}