package com.thatapp.checklists.ViewClasses

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import com.thatapp.checklists.ModelClasses.DisplayChecklistAdapter
import com.thatapp.checklists.R

class DisplayCheckListsActivity : AppCompatActivity() {

    val downloaded: ArrayList<String> = ArrayList()
    private val TAG = "Downloaded:-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloaded_checklists)
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setTitle("Checklists")
        setSupportActionBar(toolbar)

        loadAllDownloadedFiles();
		setAdapter()
	}

	private fun setAdapter() {
		val rv_list: RecyclerView = findViewById(R.id.rv_downloaded)
		rv_list.layoutManager = LinearLayoutManager(this)
		// Access the RecyclerView Adapter and load the data into it
		rv_list.adapter = DisplayChecklistAdapter(downloaded, this)
	}

	private fun loadAllDownloadedFiles() {
        val files = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).listFiles()
        Log.e("Files", "Size: " + files!!.size)
        for (i in files.indices) {
            Log.e("Files", "FileName:" + files[i].name)
            downloaded.add(files[i].name)
        }
    }

/*    private fun readExcelFile(context: Context, fileName: String) {

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
                rv_list.adapter = DisplayQuestionsAdapter(questions, this)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return
    }
*/
}