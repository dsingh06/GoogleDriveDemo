package com.thatapp.checklists

import android.content.Context
import android.content.Intent
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfName
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_my_checklists.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

import java.io.FileOutputStream
import java.io.IOException


class MyCheckList : AppCompatActivity() {

    val questions: ArrayList<QuestionItem> = ArrayList()

    private val TAG = "Listed----"
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_checklists)
        var toolbar: Toolbar = findViewById(R.id.my_toolbar)

        toolbar.setTitle("MY CHECKLIST")
        setSupportActionBar(toolbar)

        val intent: Intent = intent
        val filename: String = intent.getStringExtra("fileName")
        Log.e("file name is ", "@   " + filename)
        btnSubmit.setOnClickListener(View.OnClickListener {
            var ques: QuestionItem
            for (i in questions) {
                ques = i
                Log.e("answer ", ques.serialNo + "   " + ques.answer)
            };
          CreatePdf(questions).execute(this)
		})
        readExcelFile(this, filename)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun readExcelFile(context: Context, fileName: String) {

        try {
            // Creating Input Stream
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            // Create a workbook using the File System
            val myWorkBook = WorkbookFactory.create(file)

            // Get the first sheet from workbook
            val mySheet = myWorkBook.getSheetAt(0)
            val rowIter = mySheet.rowIterator()

            var questionsItem: QuestionItem
            var heading = 0
            var question = 0
            while (rowIter.hasNext()) {
                val row: Row = rowIter.next()
                val cellIterator: Iterator<Cell> = row.cellIterator()
                while (cellIterator.hasNext()) {
                    val cell: Cell = cellIterator.next()

                    if (row.rowNum >= 0) { //To filter column headings
                        if (cell.columnIndex == 0) {// To match column index
                            Log.e("column", "")
                            Log.e(TAG, "\n column Value: " + cell.toString())
                            heading += 1
                            question = 0
                            questionsItem = QuestionItem(heading.toString(), cell.toString(), "")
                            questions.add(questionsItem)
                        } else {
                            Log.e("row", "")
                            Log.e(TAG, "\t\tCell Value: " + cell.toString())
                            question += 1
                            questionsItem = QuestionItem("" + heading + "." + question, "", cell.toString())
                            questions.add(questionsItem)
                        }
                    }
                }
            }

            if (questions.size > 1) {
                val rv_list: RecyclerView = findViewById(R.id.rc)
                rv_list.layoutManager = LinearLayoutManager(this)

                // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
//        rv_animal_list.layoutManager = GridLayoutManager(this, 2)

                // Access the RecyclerView Adapter and load the data into it
                rv_list.adapter = CheckListItemAdapter(questions, this)
/*                rv_list.addOnScrollListener(object:RecyclerView.OnScrollListener() {

                   override fun onScrolled(recyclerView:RecyclerView, dx:Int, dy:Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        if (dy > 1 && textView.getVisibility() === View.VISIBLE)
                        {
                            btnSubmit.setVisibility(View.GONE)
                            // textDate.setVisibility(View.GONE);
                        }
                        else if (dy < 0 && textView.getVisibility() !== View.VISIBLE)
                        {
                            //mFloatingActionButton.show();
                            btnSubmit.setVisibility(View.VISIBLE)
                            // textDate.setVisibility(View.VISIBLE);
                        }
                    }
                })*/
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return
    }

	class CreatePdf(val questions:ArrayList<QuestionItem>):AsyncTask<Context,Void,Void>(){

		override fun doInBackground(vararg p0: Context): Void? {
			val pdfCreationObject = CreatePDF(questions, p0.get(0))
			pdfCreationObject.startPDFCreation()
			return null
		}

	}
}
