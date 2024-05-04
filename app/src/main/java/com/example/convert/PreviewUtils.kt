package com.example.convert

import android.content.Context
import android.net.Uri
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument

suspend fun preview(fileUri: Uri, context: Context): String {
    try {
        // Mở InputStream cho tệp từ URI
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: return "Không thể mở tệp."

        // Kiểm tra loại tệp từ fileUri
        val fileExtension = fileUri.toString().substringAfterLast(".", "").lowercase()

        return when (fileExtension) {
            "pdf" -> {
                // Xem trước nội dung tệp PDF
                val pdDocument = PDDocument.load(inputStream)
                val textStripper = PDFTextStripper()
                val text = textStripper.getText(pdDocument)
                pdDocument.close()
                text.split("\n").firstOrNull() ?: "Không có nội dung"
            }
            "docx" -> {
                // Xem trước nội dung tệp DOCX
                val xwpfDocument = XWPFDocument(inputStream)
                val text = xwpfDocument.paragraphs.firstOrNull()?.text
                xwpfDocument.close()
                text ?: "Không có nội dung"
            }
            else -> {
                // Loại tệp không được hỗ trợ
                "Loại tệp không được hỗ trợ."
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return "Lỗi trong quá trình xem trước: ${e.message}"
    }
}
