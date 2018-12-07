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

package com.thatapp.checklists.ModelClasses

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.common.data.DataBuffer
import com.google.android.gms.drive.*
import com.google.android.gms.drive.Drive.getDriveResourceClient
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.drive.widget.DataBufferAdapter
import com.google.android.gms.tasks.Task
import okio.Okio
import java.io.File
import java.util.*
import com.google.android.gms.drive.Drive
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.provider.Settings.Global.getString
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.drive.Drive.getDriveClient


class GoogleDriveService(private val activity: Activity, private val config: GoogleDriveConfig) {

    companion object {
        private val SCOPES = setOf<Scope>(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
        val documentMimeTypes = arrayListOf(
//        "application/pdf",
//            "application/vnd.google-apps.file",
//            "application/vnd.google-apps.spreadsheet",
//            "application/msword",
//            "text/plain",
//            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel"
//           "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
//      "xls" =>'application/vnd.ms-excel',
//      "xlsx" =>'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
//      "xml" =>'text/xml',
//      "ods"=>'application/vnd.oasis.opendocument.spreadsheet',
//      "csv"=>'text/plain',
//      "tmpl"=>'text/plain',
//      "pdf"=> 'application/pdf',
//      "php"=>'application/x-httpd-php',
//      "jpg"=>'image/jpeg',
//      "png"=>'image/png',
//      "gif"=>'image/gif',
//      "bmp"=>'image/bmp',
//      "txt"=>'text/plain',
//      "doc"=>'application/msword',
//      "js"=>'text/js',
//      "swf"=>'application/x-shockwave-flash',
//      "mp3"=>'audio/mpeg',
//      "zip"=>'application/zip',
//      "rar"=>'application/rar',
//      "tar"=>'application/tar',
//      "arj"=>'application/arj',
//      "cab"=>'application/cab',
//      "html"=>'text/html',
//      "htm"=>'text/html',
//      "default"=>'application/octet-stream',
//      "folder"=>'application/vnd.google-apps.folder'
        )

        const val REQUEST_CODE_OPEN_ITEM = 100
        const val REQUEST_CODE_SIGN_IN = 101
        const val TAG = "GoogleDriveService"
    }

    var serviceListener: ServiceListener? = null

    private var driveClient: DriveClient? = null
    private var driveResourceClient: DriveResourceClient? = null
    private var signInAccount: GoogleSignInAccount? = null

    private val googleSignInClient: GoogleSignInClient by lazy {
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        for (scope in SCOPES) {
            builder.requestScopes(scope)
        }
        val signInOptions = builder.build()
        GoogleSignIn.getClient(activity, signInOptions)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> {
                if (data != null) {
                    handleSignIn(data)
                } else {
                    serviceListener?.cancelled()
                }
            }

            REQUEST_CODE_OPEN_ITEM -> {
                if (data != null) {
                    openItem(data)
                } else {
                    serviceListener?.cancelled()
                }
            }
        }
    }

    /**
     * Handle the activity result when signing in
     */
    private fun handleSignIn(data: Intent) {
        val getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
        if (getAccountTask.isSuccessful) {
            initializeDriveClient(getAccountTask.result!!)
        } else {
            serviceListener?.handleError(Exception("Sign-in failed.", getAccountTask.exception))
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        driveClient = Drive.getDriveClient(activity.applicationContext, signInAccount)
        driveResourceClient = Drive.getDriveResourceClient(activity.applicationContext, signInAccount)
        serviceListener?.loggedIn()
    }

    private fun openItem(data: Intent) {
        val driveId = data.getParcelableExtra<DriveId>(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
        downloadFile(driveId)
    }

    private fun downloadFile(data: DriveId?) {


        if (data == null) {
            Log.e(TAG, "downloadFile data is null")
            return
        }
//    Log.d(TAG+"-----",data.resourceId)
        val drive: DriveFile = data.asDriveFile()
        var fileName = "test.pdf"

        driveResourceClient?.getMetadata(drive)?.addOnSuccessListener {
            fileName = it.title
            Log.d(TAG + ">>", fileName)
        }

        val openFileTask = driveResourceClient?.openFile(drive, DriveFile.MODE_READ_ONLY)
        openFileTask?.continueWithTask { task ->
            val contents = task.result
            contents!!.inputStream.use {
                try {
                    //This is the app's download directory, not the phones
                    val storageDir = activity.getFilesDir()

                    val filep = File(storageDir.getAbsolutePath() + File.separator + "downloads" + File.separator + "awasrishabh@gmail.com")
//
                    val tempFile = File(filep, fileName)

                    var t = filep.mkdirs()
                    Log.e("sss", " " + t)

                    tempFile.createNewFile()
                    val sink = Okio.buffer(Okio.sink(tempFile))
                    sink.writeAll(Okio.source(it))
                    sink.close()

                    serviceListener?.fileDownloaded(tempFile, fileName)
                } catch (e: Exception) {
                    Log.e(TAG, "Problems saving file", e)
                    serviceListener?.handleError(e)
                }
            }
            driveResourceClient?.discardContents(contents)
        }?.addOnFailureListener { e ->
            // Handle failure
            Log.e(TAG, "Unable to read contents", e)
            serviceListener?.handleError(e)
        }
    }

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    fun pickFiles(driveId: DriveId?) {
        val builder = OpenFileActivityOptions.Builder()
        if (config.mimeTypes != null) {
            builder.setMimeType(config.mimeTypes)
        } else {
            builder.setMimeType(documentMimeTypes)
        }
        if (config.activityTitle != null && config.activityTitle.isNotEmpty()) {
            builder.setActivityTitle(config.activityTitle)
        }
        if (driveId != null) {
            builder.setActivityStartFolder(driveId)
        }
        val openOptions = builder.build()
        pickItem(openOptions)
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private fun pickItem(openOptions: OpenFileActivityOptions) {
        val openTask = driveClient?.newOpenFileActivityIntentSender(openOptions)
        openTask!!.let {
            openTask.continueWith { task ->
                ActivityCompat.startIntentSenderForResult(activity, task.result!!, REQUEST_CODE_OPEN_ITEM,
                        null, 0, 0, 0, null)


            }
        }
    }

    /**
     * Initialize signInAccount if user has signed in and no new scope
     */
    fun checkLoginStatus(): Boolean {
        val requiredScopes = HashSet<Scope>(2)
        requiredScopes.add(Drive.SCOPE_FILE)
        requiredScopes.add(Drive.SCOPE_APPFOLDER)
        signInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        val containsScope = signInAccount?.grantedScopes?.containsAll(requiredScopes)
        if (signInAccount != null && containsScope == true) {
            initializeDriveClient(signInAccount!!)
            return true
        }
        return false
    }

    /**
     * Starts the sign-in process.
     */
    fun auth() {
        activity.startActivityForResult(googleSignInClient.signInIntent, REQUEST_CODE_SIGN_IN)
    }

    fun logout() {
        googleSignInClient.signOut()
        signInAccount = null
    }

}
