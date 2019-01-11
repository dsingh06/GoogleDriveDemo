package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
class DriveUploader(val context: Context) {

    private lateinit var mDriveService: Drive
    var prefManager: PrefManager = PrefManager(context)
	private val TAG ="From DriveUploader:  "

	private lateinit var fileDownloadDestination:java.io.File
	private lateinit var fileGeneratedDestination:java.io.File

	var rootChecklistFolderExists = false
	var mailIDFolder_InChecklistFolder = false
	var mailIDFolder_InDrive = false

	companion object {
		var lastGoogleSignInAccount:GoogleSignInAccount?=null
		var setOfList_LocalFiles = HashSet<String>()
		var setOfList_DriveFiles = HashSet<String>()
	}

	init {
		initFolders()
		if (checkService()){
			if (createFolder()){
				getListOfFilesAndSync()
			}
		}
	}


	private fun initFolders() {
        val storageDir:java.io.File = context.getFilesDir();
        prefManager = PrefManager(context);
        fileDownloadDestination = java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads");
        val d = fileDownloadDestination.mkdirs()
		fileGeneratedDestination = java.io.File(storageDir.getAbsolutePath() + java.io.File.separator
				+ "generated" + java.io.File.separator + prefManager.dirName)
		val g = fileGeneratedDestination.mkdirs()

		Log.e(TAG,"1 DownloadsDirectory Created? "+d.toString())
		Log.e(TAG,"1 DestinationDirectory NAME? "+prefManager.dirName)
		Log.e(TAG,"1 DestinationDirectory Created? "+g.toString())
    }


	private fun checkService(): Boolean {
		val credential = GoogleAccountCredential.usingOAuth2(
				context, setOf(DriveScopes.DRIVE_FILE))

		val googlecurrentSigninAccount = GoogleSignIn.getLastSignedInAccount(context)
		if (lastGoogleSignInAccount!=null && googlecurrentSigninAccount!=null){
			if (lastGoogleSignInAccount==null) Log.e(TAG,"2 LastGoogleAccount: NULL")
			if (googlecurrentSigninAccount==null) Log.e(TAG,"2 CurrentGoogleAccount: NULL")

			if (lastGoogleSignInAccount!!.email.equals(googlecurrentSigninAccount.email)){
				Log.e(TAG,"2 LastGoogleEmail: "+ lastGoogleSignInAccount!!.email)
				Log.e(TAG,"2 CurrentGoogleEmail: "+ googlecurrentSigninAccount.email)
				Log.e(TAG,"2 Global array cleared")

				setOfList_DriveFiles.clear()
				setOfList_LocalFiles.clear()
				lastGoogleSignInAccount = googlecurrentSigninAccount
			}
		} else {
			if (lastGoogleSignInAccount==null) Log.e(TAG,"-- LastGoogleAccount: NULL")

			lastGoogleSignInAccount = googlecurrentSigninAccount
		}

		if (googlecurrentSigninAccount != null) {
			credential.selectedAccount = googlecurrentSigninAccount.account
			mDriveService = Drive.Builder(
					AndroidHttp.newCompatibleTransport(),
					GsonFactory(),
					credential)
					.setApplicationName("Checklist App")
					.build()
			Log.e(TAG,"2 returning TRUE")
			return true
		}
		Log.e(TAG,"2 returning FALSE")
		return false
	}

    private fun createFolder(): Boolean {
        try {
			checkIfMailIDFolderExists()
            //CREATE A FOLDER INSIDE IF THERE IS ROOT OUTSIDE
			val folderMetadata = File()
			folderMetadata.name = prefManager.dirName
			folderMetadata.mimeType = "application/vnd.google-apps.folder"

			if (rootChecklistFolderExists) {
				if(!mailIDFolder_InChecklistFolder) {
					folderMetadata.setParents(Collections.singletonList(prefManager.rootFolderID))
					val googleFile = mDriveService.files().create(folderMetadata).execute()
							?: throw IOException("Null result when requesting file creation.")
					prefManager.folderID = googleFile.id
					mailIDFolder_InChecklistFolder = true
				}
			} else {
				Log.e(TAG,"4 mailIDFolder Exists? "+mailIDFolder_InDrive)

				if (!mailIDFolder_InDrive) {
					val googleFile = mDriveService.files().create(folderMetadata).setFields("Id").execute()
							?: throw IOException("Null result when requesting file creation.")
					prefManager.folderID = googleFile.id
					mailIDFolder_InDrive = true
					Log.e(TAG,"4 Mail ID Folder Created")
				}
			}
        } catch (e: Exception) {
			Log.e(TAG,"4 EXCEPTION - returning false")
			e.printStackTrace()
			return false
        }
		Log.e(TAG,"4 Returning true")
		return true
    }

	private fun checkIfMailIDFolderExists(){
		Log.e(TAG,"3 Going to check if mailIDfolder exists")
		// GET NAMES OF ALL THE FOLDERS IN THE DRIVE, SHARED AND OTHERS ** but not TRASHED **
		val listOfDriveFolders = mDriveService.files().list().setSpaces("Drive")
				.setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")// and sharedWithMe=true") this will ONLY show shared
				.execute()

		// 1st Choice if Checklist App folder Exist
		rootChecklistFolderExists = checkIfRootChecklistExists(listOfDriveFolders)
		rootChecklistFolderExists = false //For now lets just create own folders

		if (rootChecklistFolderExists) { // Will never be true for now
			//Get ALL FILES INSIDE THE ROOT FOLDER FOUND ABOVE
			val folderListInsideRoot = mDriveService.files().list().setSpaces("Drive")
					.setQ("mimeType = 'application/vnd.google-apps.folder' and trashed=false and '" + prefManager.rootFolderID.toString() + "' in parents")
					.execute()

			// CHECK IF 'USERNAME' FOLDER EXISTS INSIDE THE ROOT FOLDER
			for (folderName:File in folderListInsideRoot.getFiles()) {
				if (folderName.name.equals(prefManager.dirName, true)) {
					prefManager.folderID = folderName.id
					mailIDFolder_InChecklistFolder = true
					return
				}
			}
		} else { // This is what I think is the best as others can share the contants of a shared folder
			// Look for folder in drive itself
			Log.e(TAG,"3 Going to list ALL folder names.....")
			for (folderName in listOfDriveFolders.files) {
				Log.e(TAG,"3 Found: "+folderName.name)

				if (folderName.name.equals(prefManager.dirName, true)) {
					prefManager.folderID = folderName.id
					mailIDFolder_InDrive = true
					Log.e(TAG,"3 MATCH FOUND: "+folderName.name)
					return
				}
			}
		}
	}

	private fun checkIfRootChecklistExists(listOfDriveFolders: FileList): Boolean {
		for (folderName in listOfDriveFolders.files) {
			if ((folderName.name).equals("CheckList App", true)) {
				prefManager.rootFolderID = folderName.id
				return true
			}
		}
		return false
	}

	private fun getListOfFilesAndSync() {
		val arrayGeneratedFiles:Array<java.io.File> = fileGeneratedDestination.listFiles()
		val arrayGeneratedFileNames:ArrayList<String> = ArrayList()
		arrayGeneratedFiles.forEach { file ->
			arrayGeneratedFileNames.add(file.name)
			Log.e(TAG,"5 GeneratedFound: "+file.name)
		}

		val driveFileList = mDriveService.files()
				.list()
				.setQ("mimeType='application/pdf' and trashed=false and '" + prefManager.folderID + "' in parents")
				.setSpaces("Drive")
				.execute()

		val arrayDriveFileNames:ArrayList<String> = ArrayList()
		driveFileList.files.forEach{file ->
			arrayDriveFileNames.add(file.name)
			Log.e(TAG,"5 DRIVE Found: "+file.name)
		}

		val filterLocal = HashSet<String>()
		val filterDrive = HashSet<String>()
		filterLocal.addAll(arrayGeneratedFileNames)
		filterDrive.addAll(arrayDriveFileNames)
		filterLocal.removeAll(filterDrive)

		setOfList_DriveFiles.clear()
		setOfList_LocalFiles.clear()
		setOfList_LocalFiles.addAll(arrayGeneratedFileNames)
		setOfList_DriveFiles.addAll(arrayDriveFileNames)

		val filesToUpload = ArrayList<java.io.File>(arrayGeneratedFiles.toList())
		Log.e(TAG,"5 Files to upload size: "+ filesToUpload.size)


		arrayGeneratedFiles.forEach { file -> if (!filterLocal.contains(file.name)) filesToUpload.remove(file)
			Log.e(TAG,"5 From Upload Removed: "+file.name)
		}

		filesToUpload.forEach { file ->
			Log.e(TAG,"5 SAVE: "+file.name)
			saveFile(file)
		}
	}

	private fun saveFile(uploadFile: java.io.File) {
        val fileMetadata = File()
        fileMetadata.setName(uploadFile.name)
        fileMetadata.setParents(Collections.singletonList(prefManager.folderID))
        val mediaContent = FileContent("application/pdf", uploadFile)
        mDriveService.files().create(fileMetadata, mediaContent)
				.setFields("id, parents")
                .execute()
		setOfList_DriveFiles.add(uploadFile.name)
		setOfList_LocalFiles.add(uploadFile.name)
    }
}