package com.example.pdftow


import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

suspend fun convertPdfToDocx(fileUri: Uri?, context: Context): Boolean {
    try {
        // Mở tệp PDF từ URI
        val inputStream = context.contentResolver.openInputStream(fileUri!!) ?: return false
        val pdDocument = PDDocument.load(inputStream)

        // Sử dụng PDFTextStripper để lấy văn bản từ tệp PDF
        val textStripper = PDFTextStripper()
        val text = textStripper.getText(pdDocument)

        // Đóng tệp PDF sau khi sử dụng
        pdDocument.close()

        // Tạo tệp DOCX và viết văn bản vào nó
        val docxFile = File(context.cacheDir, "converted.docx")
        val xwpfDocument = XWPFDocument()
        val paragraph = xwpfDocument.createParagraph()
        val run = paragraph.createRun()
        run.setText(text)

        // Lưu tệp DOCX
        FileOutputStream(docxFile).use { fos ->
            xwpfDocument.write(fos)
        }

        // Đóng tệp DOCX
        xwpfDocument.close()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}


fun convertDocxToPdf(fileUri: Uri?, context: Context): Boolean {
    if (fileUri == null) {
        return false
    }

    try {
        // Mở tệp DOCX từ URI
        val inputStream: InputStream = context.contentResolver.openInputStream(fileUri) ?: return false
        val docxDocument = XWPFDocument(inputStream)

        // Tạo tệp PDF mới
        val pdfDocument = PDDocument()

        // Lặp qua các đoạn trong tệp DOCX và thêm vào tệp PDF
        docxDocument.paragraphs.forEach { paragraph ->
            // Tạo trang mới trong tệp PDF
            val page = PDPage()
            pdfDocument.addPage(page)

            // Thêm nội dung đoạn văn bản vào trang mới
            PDPageContentStream(pdfDocument, page).use { contentStream ->
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA, 12f)
                contentStream.newLineAtOffset(100f, 700f)
                contentStream.showText(paragraph.text)
                contentStream.endText()
            }
        }

        // Lưu tệp PDF vào thư mục lưu trữ của ứng dụng
        val pdfFile = File(context.getExternalFilesDir(null), "output.pdf")
        pdfDocument.save(FileOutputStream(pdfFile))

        // Đóng tệp PDF và tệp DOCX
        pdfDocument.close()
        docxDocument.close()

        // Chuyển đổi thành công
        return true

    } catch (e: IOException) {
        e.printStackTrace()
        // Chuyển đổi thất bại
        return false
    }
}


suspend fun saveConvertedFile(uri: Uri, context: Context, fileUri: Uri?): Boolean {
    try {
        // Mở đầu ra tệp từ URI
        val outputStream = context.contentResolver.openOutputStream(uri) ?: return false

        // Xác định tệp cần lưu dựa trên tệp ban đầu
        val fileToSave = if (fileUri.toString().endsWith(".pdf", ignoreCase = true)) {
            File(context.cacheDir, "converted.docx")
        } else {
            File(context.cacheDir, "converted.pdf")
        }

        // Mở đầu vào tệp từ tệp cần lưu
        val inputStream = withContext(Dispatchers.IO) {
            FileInputStream(fileToSave)
        }
        inputStream.copyTo(outputStream)

        withContext(Dispatchers.IO) {
            inputStream.close()
        }
        withContext(Dispatchers.IO) {
            outputStream.close()
        }

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}


