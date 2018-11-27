package com.thatapp.checklists

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

class MyCheckList : AppCompatActivity(){

    val questions: ArrayList<QuestionItem> = ArrayList()

    private val TAG = "Listed----"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_checklists)


        var intent:Intent = intent
        var filename:String= intent.getStringExtra("fileName")
        Log.e("file name is ","@   "+filename)
        readExcelFile(this,filename)
    }

    private fun readExcelFile(context: Context, fileName: String) {

//    if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
//      Log.e(FragmentActivity.TAG, "Storage not available or read only")
//      return
//    }

        try {
            // Creating Input Stream
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            // Create a workbook using the File System
            val myWorkBook = WorkbookFactory.create(file)

            // Get the first sheet from workbook
            val mySheet = myWorkBook.getSheetAt(0)
            val rowIter = mySheet.rowIterator()
            var questionsItem: QuestionItem

            val sectionModelArrayList:ArrayList<SectionModel> = ArrayList()
            val itemArrayList:ArrayList<String> = ArrayList()
            //for loop for items


            while (rowIter.hasNext()) {
                val row: Row = rowIter.next()
                val cellIterator: Iterator<Cell> = row.cellIterator()
                while (cellIterator.hasNext()) {
                    val cell: Cell = cellIterator.next()

                    if (row.rowNum >= 0) { //To filter column headings
                        if (cell.columnIndex == 0) {
                            questionsItem = QuestionItem("1", cell.toString(), "Test", "test","heading")

                            questions.add(questionsItem)
//todo display as a section with collapsing function
//  itemArrayList.add(cell.toString())
//                            sectionModelArrayList.add(SectionModel(cell.toString(), itemArrayList))
                        } else {
                            questionsItem = QuestionItem("1", cell.toString(), "Test", "test","question")

                            questions.add(questionsItem)
                        }
                    }
                }
            }

           if(questions.size>1){
                val rv_list: RecyclerView = findViewById(R.id.rc)
                rv_list.layoutManager = LinearLayoutManager(this)


                // Access the RecyclerView Adapter and load the data into it
                rv_list.adapter = CheckListItemAdapter(questions, this)

             //not using  val adapter = SectionAdapter(this, sectionModelArrayList)
             //               rv_list.setAdapter(adapter)

            }

   } catch (e: Exception) {
            e.printStackTrace()
        }

        return
    }
}