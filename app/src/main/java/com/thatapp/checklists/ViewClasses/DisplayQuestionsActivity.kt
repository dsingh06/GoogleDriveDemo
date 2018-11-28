package com.thatapp.checklists.ViewClasses

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.*
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.thatapp.checklists.ModelClasses.DisplayQuestionsAdapter
import com.thatapp.checklists.ModelClasses.CreatePDF
import com.thatapp.checklists.ModelClasses.QuestionItem
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_my_checklists.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File


class DisplayQuestionsActivity : AppCompatActivity() {

    val questions: ArrayList<QuestionItem> = ArrayList()

    private val TAG = "My_LogMainActivity"
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_checklists)

		val toolbar: Toolbar = findViewById(R.id.my_toolbar)

        val intent: Intent = intent
        val filename: String = intent.getStringExtra("fileName")
        Log.e("file name is ", "@   " + filename)

		toolbar.setTitle(filename)
		setSupportActionBar(toolbar)

		val uncheckedQuestionArray = ArrayList<String>() // to collect unchecked question's serialnumber
        btnSubmit.setOnClickListener{
            for (question in questions) {
				if (!question.strQuestion.equals("")){
					if (question.answer.equals("--"))uncheckedQuestionArray.add(question.serialNo)
				}
            }
			if (uncheckedQuestionArray.size>=1){
				val stringOfQuestions = uncheckedQuestionArray.toString().replace("[", "").replace("]", "")
				AlertDialog.Builder(this,android.R.style.Theme_Material_Dialog_Alert)
						.setTitle("Questions skipped")
						.setMessage("The following questions were not answered: \n"+stringOfQuestions)
						.setPositiveButton("Go back", DialogInterface.OnClickListener { _ , _ ->
							// do nothing
						})
						.setNegativeButton("Skip ALL", DialogInterface.OnClickListener { _ , _ ->
							Snackbar.make(btnSubmit,"Creating PDF...", Snackbar.LENGTH_LONG).show()
							CreatePdf(questions).execute(this)
						})
//						.setNeutralButton("Email Report", DialogInterface.OnClickListener { dialog, _ ->
//
//						})
						.setIcon(R.drawable.ic_alert)
						.show()
			}
		}

        loadQuestionsArray(this, filename)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun loadQuestionsArray(context: Context, fileName: String) {

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
//                            Log.e("column", "")
//                            Log.e(TAG, "\n column Value: " + cell.toString())
                            heading += 1
                            question = 0
                            questionsItem = QuestionItem(heading.toString(), cell.toString(), "")
                            questions.add(questionsItem)
                        } else {
//                            Log.e("row", "")
//                            Log.e(TAG, "\t\tCell Value: " + cell.toString())
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
                // Access the RecyclerView Adapter and load the data into it
                rv_list.adapter = DisplayQuestionsAdapter(questions, this)
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

	private fun goBackMethod() =  finish()

	class CreatePdf(val questions:ArrayList<QuestionItem>):AsyncTask<Context,Void,Context>(){

		override fun doInBackground(vararg p0: Context): Context {
			val pdfCreationObject = CreatePDF(questions, p0.get(0))
			pdfCreationObject.startPDFCreation()
			return p0[0]
		}

		override fun onPostExecute(result: Context) {
			super.onPostExecute(result)
			Toast.makeText(result,"PDF created",Toast.LENGTH_SHORT).show()
			(result as DisplayQuestionsActivity).goBackMethod()
		}
	}
}
