package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.os.Environment
import android.util.Log
import com.itextpdf.text.Document
import java.io.FileOutputStream

import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class CreatePDF(val questions: ArrayList<QuestionItem>, val context: Context, val filename: String) {

   /* val storageDir = context.getFilesDir().absolutePath
    var fm = filename.replace("[", "").replace("]", "").split(".xls")
    var timeStamp = SimpleDateFormat("dd/MMM/YYYY_HH:mm:ss").format(Date())
    var fileNm = fm[0] + "_" + timeStamp + ".pdf"

    var des = storageDir + "/" + fileNm
    */


    val storageDir = context.getFilesDir()
    var fm = filename.replace("[", "").replace("]", "").split(".xls")
    var timeStamp = SimpleDateFormat("dd-MM-yyyy_HH:mm").format(Date())
    var fileNm = fm[0] + ":" + timeStamp + ".pdf"
    val filep = File(storageDir.getAbsolutePath() + File.separator + "generated")

    var des =filep.absolutePath+"/"+fileNm


    val document = Document()

    fun startPDFCreation() {

        var t = filep.mkdirs()
            Log.e("sss", " " + t)

        try {
            PdfWriter.getInstance(document, FileOutputStream(des))
        } catch (ex: Exception) {
            Log.e("eee", ex.toString())
        }
        document.open()
        val table = PdfPTable(3)
//        table.setWidths(floatArrayOf(0.5f, 3f,2f))
        for (aw in questions) {
            val ques: QuestionItem = aw
            table.addCell(ques.serialNo + " ")
            table.addCell(ques.strQuestion + " ")
            table.addCell(ques.answer + " ")
        }
        document.add(table)
        document.close()
        Log.e("answer ", "closed")
    }

}