package com.thatapp.checklists.ModelClasses

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.util.Pair
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.tasks.Tasks.*
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.thatapp.checklists.ModelClasses.GoogleDriveService.Companion.REQUEST_CODE_SIGN_IN
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
class DriveUploader(private val fileName: java.io.File, private val context: Context) {
    private val mExecutor = Executors.newSingleThreadExecutor()
    private lateinit var mDriveService: Drive
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    fun createFile() {
        try {
            /*          val folderMetadata = File()
                      folderMetadata.name = "CheckList App"


                      folderMetadata.mimeType = "application/vnd.google-apps.folder"
                      Log.e("create", "folder")

                      val googleFile = mDriveService.files().create(folderMetadata).execute()
                              ?: throw IOException("Null result when requesting file creation.")

                      googleFile.id

                      Log.e("create", "folder success" + googleFile.id)
          */
            val request = mDriveService.files().list().setQ(
                    "mimeType='application/vnd.google-apps.folder'").execute()
            for (file in request.getFiles()) {
                Log.e("data ", "file: " + file.getName() + " " + file.getId())
                if (file.getName().equals("CheckList App", ignoreCase = true)) {
                    Log.e("matched ", "file: " + file.getName() + "    " + file.getId())
                    saveFile(file.getId())
                    break
                }

            }


        } catch (e: Exception) {
            Log.e("create", "folder " + e.toString())
        }
    }


    fun saveFile(folderId: String) {

        // Create a File containing any metadata changes.

        val folderId = folderId
        val fileMetadata = File()


        fileMetadata.setName(fileName.name)
        fileMetadata.setParents(Collections.singletonList(folderId))
        val filePath = fileName
        val mediaContent = FileContent("application/pdf", filePath)
        val file = mDriveService.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute()
        Log.e("File ID: ", "  " + file.getId())
/*
        val fileMetadata = File()
        fileMetadata.name = "checklists"
        fileMetadata.mimeType = "application/vnd.google-apps.folder"

        val file = mDriveService.files().create(fileMetadata)
                .setFields(fileId)
                .execute()
        println("Folder ID: " + file.getId())

        val metadata = File().setName(name)

        // Convert content to an AbstractInputStreamContent instance.
        val contentStream = ByteArrayContent.fromString("text/plain", content)

        // Update the metadata and contents.
        mDriveService.files().update(fileId, metadata, contentStream).execute()
*/
    }


    fun checkService() {
        val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE))

        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            credential.selectedAccount = googleAccount.account
            Log.e("login", "true")

            //saveFile(fileName.name, fileName.name, "1234")
            mDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Checklist")
                    .build()
            createFile()
            //            mDriveService.Files().create()

        } else {
            Log.e("login", "false")
        }


    }


    init {
        checkService()
    }

}