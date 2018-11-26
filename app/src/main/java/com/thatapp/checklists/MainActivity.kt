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
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentActivity
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
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

    val config = GoogleDriveConfig(
        getString(R.string.source_google_drive),
        GoogleDriveService.documentMimeTypes
    )
    googleDriveService = GoogleDriveService(this, config)
    googleDriveService.serviceListener = this
    googleDriveService.checkLoginStatus()

    login.setOnClickListener {
      googleDriveService.auth()
    }
    start.setOnClickListener {
      googleDriveService.pickFiles(null)
    }
    logout.setOnClickListener {
      googleDriveService.logout()
      state = ButtonState.LOGGED_OUT
      setButtons()
    }
    setButtons()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    googleDriveService.onActivityResult(requestCode, resultCode, data)
  }

  override fun loggedIn() {
    state = ButtonState.LOGGED_IN
    setButtons()
  }

  override fun fileDownloaded(file: File, fileName:String) {
//    val intent = Intent(Intent.ACTION_VIEW)
//    val apkURI = FileProvider.getUriForFile(
//        this,
//        applicationContext.packageName + ".provider",
//        file)
//    val uri = Uri.fromFile(file)
      readExcelFile(this,fileName)
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
//      val myInput = FileInputStream(file)



      // Create a POIFSFileSystem object
//      val myFileSystem = POIFSFileSystem(myInput)

      // Create a workbook using the File System
        val myWorkBook = WorkbookFactory.create(file)
//        if (fileName.substringAfter(".")==="xls"){
//            myWorkBook = HSSFWorkbook(myFileSystem)
//        } else {
//            myWorkBook = XSSFWorkbook(myInput)
//        }

      // Get the first sheet from workbook
      val mySheet = myWorkBook.getSheetAt(0)
      val rowIter = mySheet.rowIterator()

      while (rowIter.hasNext()) {
        val row: Row = rowIter.next();
        val cellIterator: Iterator<Cell> = row.cellIterator();
        while (cellIterator.hasNext()) {
          val cell: Cell = cellIterator.next();

          if (row.getRowNum() >= 0) { //To filter column headings
            if (cell.getColumnIndex() == 0) {// To match column index
              Log.e("column", "")
              Log.e(TAG, "\n column Value: " + cell.toString())
            } else {
              Log.e("row", "")
              Log.e(TAG, "\t\tCell Value: " + cell.toString())
            }
          }
        }
      }//

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
