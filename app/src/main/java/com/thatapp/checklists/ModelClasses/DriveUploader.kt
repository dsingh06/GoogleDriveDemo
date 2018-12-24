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
    var prefManager: PrefManager = PrefManager(context)
    lateinit var temp: String
    fun createFile() {
        try {

            val result = mDriveService.files().list().setSpaces("Drive")
                    .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and sharedWithMe=true")
                    .execute()

            for (file in result.files) {
                if (file.name.equals("CheckList App", true)) {
                    prefManager.rootFolderID = file.id
                }
            }

            val resultF = mDriveService.files().list().setSpaces("Drive")
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and '" + prefManager.rootFolderID.toString() + "' in parents")
                    .execute()

            var folderStatus = false

            for (file in resultF.files) {

                if (file.name.equals(prefManager.dirName, true)) {
                    //        Log.e("ROOT Folder  Found: ", "shared   " + file.name + "   " + file.id)
                    prefManager.folderID = file.id
                    folderStatus = true
                }
            }

            if (!folderStatus) {
                val folderMetadata = File()
                folderMetadata.name = prefManager.dirName

                folderMetadata.mimeType = "application/vnd.google-apps.folder"
                Log.e("creating user", "folder")
                folderMetadata.setParents(Collections.singletonList(prefManager.rootFolderID))
                val googleFile = mDriveService.files().create(folderMetadata).execute()
                        ?: throw IOException("Null result when requesting file creation.")

                prefManager.folderID = googleFile.id

                Log.e("create", "user folder success" + googleFile.id)
            }


            var status = false
            val request = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and '" + prefManager.rootFolderID + "' in parents").setSpaces("Drive").execute()
            for (file in request.getFiles()) {
                Log.e("data ", "folders : " + file.getName() + "  " + file.getId())
                if (file.getName().equals(prefManager.dirName, ignoreCase = true)) {
                    prefManager.folderID = file.id
                    Log.e("matched ", "file: " + file.getName() + "    " + file.getId())
                    saveFile(prefManager.folderID.toString())
                    status = true
                    break
                }
            }

            if (!status) {

                val folderMetadata = File()
                folderMetadata.name = prefManager.dirName


                folderMetadata.mimeType = "application/vnd.google-apps.folder"
                folderMetadata.setParents(Collections.singletonList(prefManager.rootFolderID))
//                Log.e("creating user ", "folder")

                val googleFile = mDriveService.files().create(folderMetadata).execute()
                        ?: throw IOException("Null result when requesting file creation.")

                Log.e("create", "folder success" + googleFile.id)

                var status = false
                val request = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false' and '" + prefManager.rootFolderID + "' in parents").setSpaces("Drive").execute()
                for (file in request.getFiles()) {
                    if (file.getName().equals(prefManager.dirName, ignoreCase = true)) {
                        prefManager.folderID = file.id
                        saveFile(file.getId())
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("create", "folder " + e.toString())
        }
    }

    fun saveFile(rootFolderId: String) {

        val folderId = rootFolderId

        val fileMetadata = File()

        fileMetadata.setName(fileName.name)
        fileMetadata.setParents(Collections.singletonList(folderId))
        val filePath = fileName
        val mediaContent = FileContent("application/pdf", filePath)
        val file = mDriveService.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute()
        Log.e("NEW File ID: ", "  " + file.getId())
    }

    fun checkService() {
        val credential = GoogleAccountCredential.usingOAuth2(
                context, setOf(DriveScopes.DRIVE_FILE))

        val googleAccount = GoogleSignIn.getLastSignedInAccount(context)
        if (googleAccount != null) {
            credential.selectedAccount = googleAccount.account
            Log.e("CheckService running", "true")
            mDriveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Checklist")
                    .build()
            createFile()


        } else {
            Log.e("CheckService running", "false")
        }
    }

    init {
        checkService()
    }

}