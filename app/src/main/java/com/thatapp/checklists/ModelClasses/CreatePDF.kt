package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.util.Log
import java.io.FileOutputStream

import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.itextpdf.text.*
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.*
import com.thatapp.checklists.R

import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter


class CreatePDF(val questions: ArrayList<QuestionItem>, val context: Context, val filename: String, val additionalDetails: String,val workOrderNumber: String) : PdfPageEventHelper() {

    var prefManager = PrefManager(context)
    val storageDir = context.getFilesDir()
    var fm = filename.replace("[", "").replace("]", "").split(".xls")
    val timeStamp = SimpleDateFormat("dd-MM-yyyy_HH:mm").format(Date())
    var fileNm = fm[0] + ":" + timeStamp + ".pdf"
    val filep = File(storageDir.getAbsolutePath() + File.separator + "generated" + File.separator + prefManager.dirName)

    var des = filep.absolutePath + "/" + fileNm
	lateinit var writer:PdfWriter
    val document = Document()

	var iconImage:Image?=null

    fun startPDFCreation() {

        try {
            writer = PdfWriter.getInstance(document, FileOutputStream(des))
			writer.setPageEvent(HeaderFooterPageEvent())
		} catch (ex:Exception) {
            Log.e("eee", ex.toString())
            return
        }
        document.pageSize = PageSize.A4
		document.setMargins(15f, 15f, 15f, 55f)
        document.open()

        var table = PdfPTable(2) // two columns
        table.setWidths(floatArrayOf(1f, 1f)) // of equal width
		table.widthPercentage = 100f
        table.horizontalAlignment = Element.ALIGN_CENTER

        var topCell = PdfPCell(Paragraph(
				"CheckList" + "\n\nJob Title              " + prefManager.jobTitle.toString()
				+ "\n\nCompany             " + prefManager.companyName.toString()
				+ "\n\nDate Time            " + timeStamp.toString()
				+ "\n\nGenerated By      " + prefManager.userName.toString()))

		topCell.border = PdfPCell.NO_BORDER
		topCell.setPadding(10f)
		table.keepTogether = true
        table.addCell(topCell)

		 //Need to add company logo/picture from profile here
        var logo: Drawable? = null
        try {
            logo = ContextCompat.getDrawable(context, com.thatapp.checklists.R.drawable.cloudcheck)
            val bitDw = logo as BitmapDrawable
            val bmp = bitDw.bitmap
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val image = Image.getInstance(stream.toByteArray())
            image.scaleToFit(50f, 50f)
            image.alignment = Element.ALIGN_RIGHT
            topCell = PdfPCell(image)
			topCell.horizontalAlignment = PdfPCell.ALIGN_RIGHT
			topCell.verticalAlignment = PdfPCell.ALIGN_MIDDLE
            table.addCell(topCell).setBorder(PdfPCell.NO_BORDER)
        } catch (ex: Exception) {

        }

        document.add(table)

        //FIRST SECTION
        table = PdfPTable(3)
        table.horizontalAlignment = Element.ALIGN_RIGHT
        table.widthPercentage = 23f
        table.setWidths(floatArrayOf(1f, 1f, 1f))

        var cellTwo = PdfPCell(Phrase("Yes"))
        cellTwo.backgroundColor = BaseColor.GREEN
        cellTwo.setPadding(5f)
        cellTwo.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(cellTwo)

        cellTwo = PdfPCell(Phrase("No"))
        cellTwo.backgroundColor = BaseColor.RED
        cellTwo.horizontalAlignment = Element.ALIGN_CENTER
        cellTwo.setPadding(5f)
        table.addCell(cellTwo)

        cellTwo = PdfPCell(Phrase("N/A"))
        cellTwo.horizontalAlignment = Element.ALIGN_CENTER
        cellTwo.setPadding(5f)
        table.addCell(cellTwo)

        document.add(table)

        table = PdfPTable(5)
        table.horizontalAlignment = Element.ALIGN_LEFT
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(0.4f, 2.6f, 0.3f, 0.3f, 0.3f))
        //  table.keepTogether = true
        // table.isLockedWidth = true
        for (aw in questions) {
            val ques: QuestionItem = aw
            if (ques.strHeading.length > 3) {

                var cell = PdfPCell(Phrase(ques.serialNo))
                setPadding(cell, 10f, 5f, 10f)
                cell.backgroundColor = BaseColor(220, 220, 220)
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                cell.horizontalAlignment = Element.ALIGN_LEFT
                table.addCell(cell)

                cell = PdfPCell(Phrase(ques.strHeading))
                setPadding(cell, 10f, 5f, 10f)
                cell.backgroundColor = BaseColor(220, 220, 220)
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                cell.colspan = 4
                table.addCell(cell)

            } else {
                var cell = PdfPCell(Phrase(ques.serialNo))
                cell.horizontalAlignment = Element.ALIGN_CENTER
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cell)

                cell = PdfPCell(Phrase(ques.strQuestion))
                setPadding(cell, 5f, 10f, 5f, 5f)
                cell.horizontalAlignment = Element.ALIGN_LEFT
                cell.verticalAlignment = Element.ALIGN_MIDDLE
                table.addCell(cell)

				// Assigning drawable resource
                var d: Drawable? = null
                try {
                    d = ContextCompat.getDrawable(context, com.thatapp.checklists.R.drawable.ic_yes)
                    val bitDw = d as BitmapDrawable
                    val bmp = bitDw.bitmap
                    val stream = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val image = Image.getInstance(stream.toByteArray())
                    image.scaleAbsolute(15f, 18f)

                    cell = PdfPCell(image)

                    cell.horizontalAlignment = Element.ALIGN_CENTER
                    cell.verticalAlignment = Element.ALIGN_MIDDLE

                    if (ques.answer.equals("YES", true)) {
                        table.addCell(cell)
                        table.addCell("")
                        table.addCell("")
                    } else if (ques.answer.equals("NO", true)) {

                        table.addCell("")
                        table.addCell(cell)
                        table.addCell("")

                    } else if (ques.answer.equals("--", true)) {

                        table.addCell("")
                        table.addCell("")
                        table.addCell("")

                    } else {

                        table.addCell("")
                        table.addCell("")
                        table.addCell(cell)
                    }


                } catch (ex: Exception) {
                    Log.e("exxx", "111   " + ex.toString())
                    return
                }

            }

        }

		var cell = PdfPCell(Phrase("Additional Notes: \n" + additionalDetails))
        cell.colspan = 5
        cell.setPadding(5f)

        table.addCell(cell)

        var table1 = PdfPTable(4)
        table1.setWidths(floatArrayOf(2f, 2f, 2f, 3f))

        var cell1 = PdfPCell(Phrase("Operator Name \n"))
        cell1.colspan = 1
        cell1.rowspan = 2
        cell1.setPadding(5f)
        cell1.horizontalAlignment = Element.ALIGN_CENTER

        table1.addCell(cell1)

        cell1 = PdfPCell(Phrase("Vicky Singh"))
        cell1.colspan = 1
        cell1.rowspan = 2
        cell1.setPadding(5f)
        cell1.horizontalAlignment = Element.ALIGN_CENTER
        table1.addCell(cell1)

        cell1 = PdfPCell(Phrase("Signature"))
        cell1.colspan = 1
        cell1.rowspan = 2
        cell1.horizontalAlignment = Element.ALIGN_CENTER
        cell1.setPadding(5f)
        table1.addCell(cell1)
        cell1 = PdfPCell(Phrase("#"))
        cell1.colspan = 1
        cell1.rowspan = 2
        cell1.horizontalAlignment = Element.ALIGN_CENTER
        cell1.setPadding(5f)

        table1.addCell(cell1)

        cell = PdfPCell(table1)
        cell.colspan = 5
        cell.rowspan = 2
        cell.setPadding(5f)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(cell)


        var table2 = PdfPTable(4)
        table2.setWidths(floatArrayOf(2f, 2f, 2f, 3f))
        cell = PdfPCell(Phrase("Work Order Number"))
        cell.colspan = 1
        cell.rowspan = 2
        cell.setPadding(5f)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table2.addCell(cell)

        cell = PdfPCell(Phrase(workOrderNumber)) //this to take from user_input
        cell.colspan = 1
        cell.rowspan = 2
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table2.addCell(cell)

        cell = PdfPCell(Phrase("Date"))
        cell.colspan = 1
        cell.rowspan = 2
        table2.addCell(cell)

        cell = PdfPCell(Phrase(timeStamp))
        cell.colspan = 1
        cell.rowspan = 2
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table2.addCell(cell)

        cell = PdfPCell(table2)
        cell.colspan = 5
        cell.rowspan = 2
        cell.setPadding(5f)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        table.addCell(cell)

        try {
            document.add(table)
            document.close()
        } catch (e: Exception) {
            Log.e("exception ", e.toString())
            return
        }
    }

    private fun setPadding(cell: PdfPCell, topPadding: Float, leftPadding: Float, bottomPadding: Float, rightPadding: Float) {
        cell.paddingBottom = bottomPadding
        cell.paddingTop = topPadding
        cell.paddingLeft = leftPadding
        cell.paddingRight = rightPadding
    }

    private fun setPadding(cell: PdfPCell, topPadding: Float, leftPadding: Float, bottomPadding: Float) {
        cell.paddingBottom = bottomPadding
        cell.paddingTop = topPadding
        cell.paddingLeft = leftPadding
    }


	inner class HeaderFooterPageEvent : PdfPageEventHelper() {
		lateinit var total: PdfTemplate


		override fun onOpenDocument(writer: PdfWriter?, document: Document?) {
			total = writer!!.directContent.createTemplate(30f,16f)
		}


		override fun onStartPage(writer: PdfWriter, document: Document) {

			if (document.pageNumber>1){
				//FIRST SECTION
				val table = PdfPTable(3)
				table.horizontalAlignment = Element.ALIGN_RIGHT
				table.widthPercentage = 23f
				table.setWidths(floatArrayOf(1f, 1f, 1f))

				var cellTwo = PdfPCell(Phrase("Yes"))
				cellTwo.backgroundColor = BaseColor.GREEN
				cellTwo.setPadding(5f)
				cellTwo.horizontalAlignment = Element.ALIGN_CENTER
				table.addCell(cellTwo)

				cellTwo = PdfPCell(Phrase("No"))
				cellTwo.backgroundColor = BaseColor.RED
				cellTwo.horizontalAlignment = Element.ALIGN_CENTER
				cellTwo.setPadding(5f)
				table.addCell(cellTwo)

				cellTwo = PdfPCell(Phrase("N/A"))
				cellTwo.horizontalAlignment = Element.ALIGN_CENTER
				cellTwo.setPadding(5f)
				table.addCell(cellTwo)

				document.add(table)
			}
		}

/*		private fun headerSitePicture(writer: PdfWriter, document: Document) {
//			if (document.pageNumber>1) {
//				if (GlobalSiteRecords.profileList!!.size>0) {
//					if (profile!!.imagePath.length > 5) {
//						val img = getImg(profile!!.imagePath, 45)
//						val height = img.plainHeight
//						var width = img.plainWidth
//						val ratio = height/width
//						if(ratio>1) width = (45 * width)/height
//						if (ratio<=1) width =  width / (ratio*4)
//
//						img.backgroundColor = BaseColor.WHITE
//						img.scaleToFit(Dimensions.logoImageWidth, Dimensions.logoImageHeight)
//						img.setAbsolutePosition(
//								(PageSize.A4.width-Dimensions.leftSideMargin- width),
//								(Dimensions.topLogoStart))
//						document.add(img)
//					}
//				}
//			}
		}
		private fun headerText(writer: PdfWriter, document: Document){
//			if (document.pageNumber>1){
//				val phraseName:Phrase
//				val phraseJobCompany:Phrase
//				if (GlobalSiteRecords.profileList!=null && GlobalSiteRecords.profileList!!.size>0){
//					phraseName = Phrase("Site: ${site.name}")
//					phraseJobCompany = Phrase("Company: ${site.company}")
//				} else{
//					phraseName = Phrase("*Profile missing*")
//					phraseJobCompany = Phrase("*Profile missing*")
//				}
//				ColumnText.showTextAligned(writer.directContent,
//						Element.ALIGN_LEFT,
//						phraseName,
//						Dimensions.leftSideMargin,
//						document.pageSize.height-Dimensions.topMargin,
//						0f)
//				ColumnText.showTextAligned(writer.directContent,
//						Element.ALIGN_LEFT,
//						phraseJobCompany,
//						Dimensions.leftSideMargin,
//						document.pageSize.height-Dimensions.topMargin-Dimensions.topMarginPlusExtra,
//						0f)
//				val canvas = writer.directContent
//				canvas.setRGBColorStroke(255,85,0)
//				canvas.moveTo(Dimensions.leftSideMargin,document.pageSize.height-Dimensions.topMarginLine)
//				canvas.lineTo(document.pageSize.width-Dimensions.rightSideMargin,document.pageSize.height-Dimensions.topMarginLine)
//				canvas.stroke()
//			}
		}
*/

		override fun onEndPage(writer: PdfWriter, document: Document) {
			if (true) {
				val table = PdfPTable(2)
				try{
					table.setTotalWidth(floatArrayOf(24f,2f))
					table.totalWidth = 100f
					table.isLockedWidth = true
					table.defaultCell.fixedHeight = 20f
					table.defaultCell.border = Rectangle.NO_BORDER
					table.defaultCell.horizontalAlignment = Element.ALIGN_RIGHT
					table.addCell(String.format("Page %d of",writer.pageNumber))
					val cell = PdfPCell(Image.getInstance(total))
					cell.border = Rectangle.NO_BORDER
					table.addCell(cell)
					table.writeSelectedRows(0,-1,document.right(115f),document.bottom(3f),writer.directContent)
				} catch (e:DocumentException){

				}

				if (iconImage==null) {
					val i = BitmapFactory.decodeResource(context.resources, R.drawable.cloudcheck)
					val stream = ByteArrayOutputStream()
					i.compress(Bitmap.CompressFormat.JPEG, 100, stream)
					iconImage = Image.getInstance(stream.toByteArray())
					iconImage!!.scaleToFit(40f, 40f)
				}
				iconImage!!.setAbsolutePosition(15f,15f)
				document.add(iconImage)
				ColumnText.showTextAligned(writer.directContent, Element.ALIGN_LEFT, Phrase("CHECKLIST"),15f+40+5,40f,0f)
				ColumnText.showTextAligned(writer.directContent, Element.ALIGN_LEFT, Phrase("(on Android)"),15f+40+5,25f,0f)
			}
		}

		override fun onCloseDocument(writer: PdfWriter?, document: Document?) {
			ColumnText.showTextAligned(total,Element.ALIGN_LEFT, Phrase((writer!!.currentPageNumber-1).toString()),2f, 2f, 0f)
		}
	}
}