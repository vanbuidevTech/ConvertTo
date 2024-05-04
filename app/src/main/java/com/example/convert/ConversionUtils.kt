package com.example.convert


import android.content.Context
import android.net.Uri
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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

suspend fun convertDocxToPdf(fileUri: Uri?, context: Context): Boolean {
    try {
        // Mở tệp DOCX từ URI
        val inputStream = context.contentResolver.openInputStream(fileUri!!) ?: return false
        val xwpfDocument = XWPFDocument(inputStream)

        // Tạo tệp PDF mới
        val pdfFile = File(context.cacheDir, "converted.pdf")
        val pdDocument = PDDocument()

        // Tạo trang mới trong tệp PDF
        val page = pdDocument.addPage()

        // Tạo content stream để viết nội dung vào trang
        val contentStream = PDPageContentStream(pdDocument, page)

        // Thiết lập font chữ và cỡ chữ
        contentStream.setFont(PDType1Font.HELVETICA, 12f)

        // Di chuyển điểm bắt đầu văn bản
        contentStream.beginText()
        contentStream.newLineAtOffset(25f, 700f)

        // Đọc nội dung từ tệp DOCX và viết vào trang PDF
        xwpfDocument.paragraphs.forEach { paragraph ->
            val paragraphText = paragraph.text
            contentStream.showText(paragraphText)
            contentStream.newLine() // Chuyển sang dòng mới sau mỗi đoạn văn
        }

        // Kết thúc văn bản
        contentStream.endText()

        // Đóng content stream và lưu tài liệu PDF
        contentStream.close()

        FileOutputStream(pdfFile).use { fos ->
            pdDocument.save(fos)
        }

        // Đóng tài liệu PDF
        pdDocument.close()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
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
        val inputStream = FileInputStream(fileToSave)
        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        return true
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}
