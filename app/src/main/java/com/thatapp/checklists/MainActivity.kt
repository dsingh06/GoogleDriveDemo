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
import android.util.Log
import android.view.View
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

  lateinit var downloadAndSync:ConstraintLayout
  lateinit var myProfile:ConstraintLayout

  private val PROFILE_ACTIVITY = 33


  private fun setButtons() {
    when (state) {
      ButtonState.LOGGED_OUT -> {
        status.text = getString(R.string.status_logged_out)
        start.isEnabled = false
        logout.isEnabled = false
        login.isEnabled = true
          login.visibility = View.VISIBLE
        logout.visibility = View.INVISIBLE

      }

      else -> {
        status.text = getString(R.string.status_logged_in)
        start.isEnabled = true
        logout.isEnabled = true
        login.isEnabled = false
          login.visibility = View.INVISIBLE
        logout.visibility = View.VISIBLE
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
        val isUserLoggedin:Boolean = googleDriveService.checkLoginStatus()
        if (!isUserLoggedin)googleDriveService.auth() // Automatic login screen

      login.setOnClickListener {
          googleDriveService.auth()
      }
        downloadAndSync.setOnClickListener {
            googleDriveService.pickFiles(null)
        }
        myProfile.setOnClickListener {
            startActivityForResult(Intent(this,ProfileActivity::class.java),PROFILE_ACTIVITY)
        }
        logout.setOnClickListener {
            googleDriveService.logout()
            state = ButtonState.LOGGED_OUT
            setButtons()
        }
        imageView3.setOnClickListener{
//          val intent = Intent(this,DownloadedCheckListsActivity::class.java)
//          startActivity(intent)
        }
	    view.setOnClickListener{
//          val intent = Intent(this,DownloadedCheckListsActivity::class.java)
//          startActivity(intent)
        }
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

  override fun fileDownloaded(file: File, fileName:String) {
      Snackbar.make(main_layout,"File download completed", Snackbar.LENGTH_LONG).show()
  }

  override fun cancelled() {
    Snackbar.make(main_layout, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
  }

  override fun handleError(exception: Exception) {
      if(exception.message==="Sign-in failed.") setButtons()
    val errorMessage = getString(R.string.status_error, exception.message)
    Snackbar.make(main_layout, errorMessage, Snackbar.LENGTH_LONG).show()
  }
}
