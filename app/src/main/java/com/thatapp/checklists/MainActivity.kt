/*
 * Copyright (c) 2018 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.thatapp.checklists

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.webkit.MimeTypeMap
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.widget.Toast
import com.thatapp.checklists.GoogleDriveConfig
import com.thatapp.checklists.GoogleDriveService
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private lateinit var googleDriveService: GoogleDriveService
    private var state = ButtonState.LOGGED_OUT

    lateinit var downloadAndSync: ConstraintLayout
    lateinit var myProfile: ConstraintLayout

    private val PROFILE_ACTIVITY = 33

    val questions: ArrayList<QuestionItem> = ArrayList()


    private fun setButtons() {
        when (state) {
            ButtonState.LOGGED_OUT -> {
                status.text = getString(R.string.status_logged_out)
                start.isEnabled = false
                logout.isEnabled = false
                login.isEnabled = true
            }

            else -> {
                status.text = getString(R.string.status_logged_in)
                start.isEnabled = true
                logout.isEnabled = true
                login.isEnabled = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        linkVarsToViews()
        val config = GoogleDriveConfig(
                getString(R.string.source_google_drive),
                GoogleDriveService.documentMimeTypes
        )
        googleDriveService = GoogleDriveService(this, config)
        googleDriveService.serviceListener = this
        val isUserLoggedin: Boolean = googleDriveService.checkLoginStatus()
        if (!isUserLoggedin) googleDriveService.auth() // Automatic login screen

        downloadAndSync.setOnClickListener {
            googleDriveService.pickFiles(null)
        }
        myProfile.setOnClickListener {
            startActivityForResult(Intent(this, ProfileActivity::class.java), PROFILE_ACTIVITY)
        }
        logout.setOnClickListener {
            googleDriveService.logout()
            state = ButtonState.LOGGED_OUT
            setButtons()
        }
//        setButtons()
    }

    private fun linkVarsToViews() {
        downloadAndSync = findViewById(R.id.downloadAndSyncLayout)
        myProfile = findViewById(R.id.myProfileLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleDriveService.onActivityResult(requestCode, resultCode, data)
    }

    override fun loggedIn() {
        state = MainActivity.ButtonState.LOGGED_IN
        setButtons()
    }

    override fun fileDownloaded(file: File, fileName: String) {
//    val intent = Intent(Intent.ACTION_VIEW)
//    val apkURI = FileProvider.getUriForFile(
//        this,
//        applicationContext.packageName + ".provider",
//        file)
//    val uri = Uri.fromFile(file)
        readExcelFile(this, fileName)
        return
//    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
//    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
//    intent.setDataAndType(apkURI, mimeType)
//    intent.flags = FLAG_GRANT_READ_URI_PERMISSION
//    if (intent.resolveActivity(packageManager) != null) {
//      startActivity(intent)
//    } else {
//      Snackbar.make(main_layout, R.string.not_open_file, Snackbar.LENGTH_LONG).show()
//    }
    }

    override fun cancelled() {
        Snackbar.make(main_layout, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
    }

    override fun handleError(exception: Exception) {
        val errorMessage = getString(R.string.status_error, exception.message)
        Snackbar.make(main_layout, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    // found online the following function
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
            while (rowIter.hasNext()) {
                val row: Row = rowIter.next()
                val cellIterator: Iterator<Cell> = row.cellIterator()
                while (cellIterator.hasNext()) {
                    val cell: Cell = cellIterator.next()

                    if (row.rowNum >= 0) { //To filter column headings
                        if (cell.columnIndex == 0) {// To match column index
                            Log.e("column", "")
                            Log.e(TAG, "\n column Value: " + cell.toString())
                            questionsItem = QuestionItem("1", cell.toString(), "Test", "test")

                            questions.add(questionsItem)
                        } else {
                            Log.e("row", "")
                            Log.e(TAG, "\t\tCell Value: " + cell.toString())
                            questionsItem = QuestionItem("1", cell.toString(), "Test", "test")

                            questions.add(questionsItem)
                        }
                    }
                }
            }


            if(questions.size>1){
                val intent = Intent(this@MainActivity, MyCheckList::class.java)
                intent.putExtra("fileName", fileName)
                startActivity(intent)

                // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
//        rv_animal_list.layoutManager = GridLayoutManager(this, 2)

                // Access the RecyclerView Adapter and load the data into it

            }


            /** We now need something to iterate through the cells. */
            /* val rowIter = mySheet.rowIterator()

             while (rowIter.hasNext()) {
               val myRow = rowIter.next() as Row//as HSSFRow
               val cellIter = myRow.cellIterator()
               while (cellIter.hasNext()) {
                 val myCell = cellIter.next() as Cell//as HSSFCell
                 Log.d(TAG, "Cell Value: " + myCell.toString())
       //          Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show()
               }
             }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return
    }
}
