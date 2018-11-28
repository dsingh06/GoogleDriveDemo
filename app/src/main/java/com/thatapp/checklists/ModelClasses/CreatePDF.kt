package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.os.Environment
import android.util.Log
import com.itextpdf.text.Document
import java.io.FileOutputStream

import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter

class CreatePDF(val questions:ArrayList<QuestionItem>, val context: Context) {

	val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
	var fileName = "test.pdf"
	var des = storageDir.absolutePath+"/"+fileName
	val document = Document()

	fun startPDFCreation(){
		PdfWriter.getInstance(document, FileOutputStream(des))
		document.open()
		val table = PdfPTable(3)
//        table.setWidths(floatArrayOf(0.5f, 3f,2f))
		for (aw in questions) {
			val ques: QuestionItem = aw
			table.addCell(ques.serialNo+" ")
			table.addCell(ques.strQuestion+" ")
			table.addCell(ques.answer+" ")
		}
		document.add(table)
		document.close()
		Log.e("answer ", "closed")
	}

}