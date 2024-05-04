package com.example.convert

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ConverterApp()
            }
        }
    }
}

@Composable
fun ConverterApp() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var resultMessage by remember { mutableStateOf("") }

    // Tạo launcher để chọn tệp
    val openFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            fileUri = uri
        }
    )

    // Tạo launcher để lưu tệp
    val saveFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch {
                    val success = saveConvertedFile(uri, context, fileUri)
                    if (success) {
                        Toast.makeText(context, "Tệp đã được lưu thành công.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Lưu tệp thất bại.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chuyển đổi PDF sang DOCX và ngược lại")

        Spacer(modifier = Modifier.height(16.dp))

        // Nút để chọn tệp PDF hoặc DOCX
        Button(onClick = {
            // Mở cửa sổ chọn tệp
            openFilePickerLauncher.launch(arrayOf("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        }) {
            Text("Chọn tệp PDF hoặc DOCX")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nút để bắt đầu quá trình chuyển đổi
        Button(onClick = {
            if (fileUri == null) {
                Toast.makeText(context, "Vui lòng chọn tệp để chuyển đổi.", Toast.LENGTH_SHORT).show()
                return@Button
            }

            coroutineScope.launch {
                val success: Boolean
                if (fileUri.toString().endsWith(".pdf", ignoreCase = true)) {
                    // Chuyển đổi PDF sang DOCX
                    success = convertPdfToDocx(fileUri, context)
                } else {
                    // Chuyển đổi DOCX sang PDF
                    success = convertDocxToPdf(fileUri, context)
                }

                if (success) {
                    resultMessage = "Chuyển đổi thành công."
                    // Mở cửa sổ lưu tệp
                    saveFilePickerLauncher.launch(if (fileUri.toString().endsWith(".pdf", ignoreCase = true)) "converted.docx" else "converted.pdf")
                } else {
                    resultMessage = "Chuyển đổi thất bại."
                }

                Toast.makeText(context, resultMessage, Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Chuyển đổi")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(resultMessage)
    }
}


@Preview
@Composable
fun Show() {
    ConverterApp()
}