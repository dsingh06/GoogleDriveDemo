package com.thatapp.checklist.ModelClasses

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.thatapp.checklist.ViewClasses.MainActivity
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

	private var sizeGeneratedFiles:Int=0

	companion object {
		var lastGoogleSignInAccount:GoogleSignInAccount?=null
		var setOfList_LocalFiles = HashSet<String>()
		var setOfList_DriveFiles = HashSet<String>()
	}

	init {
		initFolders()
		if ((sizeGeneratedFiles>0) && checkService()){
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

		sizeGeneratedFiles = fileGeneratedDestination.listFiles().size
//		Log.e(TAG,"1 DownloadFileSize "+sizeGeneratedFiles.toString())
//		Log.e(TAG,"1 DownloadsDirectory Created? "+d.toString())
//		Log.e(TAG,"1 DestinationDirectory NAME? "+prefManager.dirName)
//		Log.e(TAG,"1 DestinationDirectory Created? "+g.toString())
    }


	private fun checkService(): Boolean {
		val credential = GoogleAccountCredential.usingOAuth2(
				context, setOf(DriveScopes.DRIVE_FILE))

		val googlecurrentSigninAccount = GoogleSignIn.getLastSignedInAccount(context) // context here causes problem.
		if (lastGoogleSignInAccount!=null && googlecurrentSigninAccount!=null){
//			if (lastGoogleSignInAccount==null) Log.e(TAG,"2 LastGoogleAccount: NULL")
//			if (googlecurrentSigninAccount==null) Log.e(TAG,"2 CurrentGoogleAccount: NULL")

			if (lastGoogleSignInAccount!!.email.equals(googlecurrentSigninAccount.email)){
//				Log.e(TAG,"2 LastGoogleEmail: "+ lastGoogleSignInAccount!!.email)
//				Log.e(TAG,"2 CurrentGoogleEmail: "+ googlecurrentSigninAccount.email)
//				Log.e(TAG,"2 Global array cleared")

				setOfList_DriveFiles.clear()
				setOfList_LocalFiles.clear()
				lastGoogleSignInAccount = googlecurrentSigninAccount
			}
		} else {
//			if (lastGoogleSignInAccount==null) Log.e(TAG,"-- LastGoogleAccount: NULL")

			lastGoogleSignInAccount = googlecurrentSigninAccount
		}

		if (googlecurrentSigninAccount != null) {
			credential.selectedAccount = googlecurrentSigninAccount.account
			mDriveService = Drive.Builder(
					AndroidHttp.newCompatibleTransport(),
//					GsonFactory(),
					JacksonFactory(),
					credential)
					.setApplicationName("Checklist App")
					.build()
//			Log.e(TAG,"2 returning TRUE")
			return true
		}
//		Log.e(TAG,"2 returning FALSE")
		return false
	}

    private fun createFolder(): Boolean {
        try {
			checkIfMailIDFolderExists()
            //CREATE A FOLDER INSIDE IF THERE IS ROOT OUTSIDE
			val folderMetadata = File()
//			folderMetadata.name = prefManager.dirName
			folderMetadata.name = prefManager.userName+" (Checklist_App)"
			folderMetadata.mimeType = "application/vnd.google-apps.folder"

			if (rootChecklistFolderExists) {
				if(!mailIDFolder_InChecklistFolder) {
					folderMetadata.setParents(Collections.singletonList(prefManager.rootFolderID))
					val googleFile = mDriveService.files().create(folderMetadata).setFields("id").execute()
							?: throw IOException("Null result when requesting file creation.")
					prefManager.folderID = googleFile.id
					mailIDFolder_InChecklistFolder = true
				}
			} else {
//				Log.e(TAG,"4 mailIDFolder Exists? "+mailIDFolder_InDrive)

				if (!mailIDFolder_InDrive) {
					val googleFile = mDriveService.files().create(folderMetadata).setFields("id").execute()
							?: throw IOException("Null result when requesting file creation.")
					prefManager.folderID = googleFile.id
					mailIDFolder_InDrive = true
//					Log.e(TAG,"4 Mail ID Folder Created")
				}
			}
        } catch (ex: Exception) {
			Crashlytics.logException(ex)
//			Log.e(TAG,"4 EXCEPTION - returning false")
			return false
        }
//		Log.e(TAG,"4 Returning true")
		return true
    }

	private fun checkIfMailIDFolderExists(){
//		Log.e(TAG,"3 Going to check if mailIDfolder exists")
		// GET NAMES OF ALL THE FOLDERS IN THE DRIVE, SHARED AND OTHERS ** but not TRASHED **
		val listOfDriveFolders:FileList
		try {
			listOfDriveFolders = mDriveService.files().list().setSpaces("Drive")
					.setQ("mimeType='application/vnd.google-apps.folder' and 'root' in parents and trashed=false and 'me' in owners")// this will ONLY show shared
					.execute()
		} catch (ex:UserRecoverableAuthIOException){
			Crashlytics.logException(ex)
			if (context is MainActivity) {
				context.startActivityForResult(ex.intent, MainActivity.REQUEST_CODE_SIGN_IN)
				return
			}
			try {
				context.startActivity(ex.intent)
			}catch(ex:Exception){
				Crashlytics.logException(ex)
				return
			}
			return
		}
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
		} else { // This is what I think is the best as others can share the contents of a shared folder
			// Look for folder in drive itself
//			Log.e(TAG,"3 Going to list ALL folder names.....")
			for (folderName in listOfDriveFolders.files) {
//				Log.e(TAG,"3 Found: "+folderName.name)

//				if (folderName.name.equals(prefManager.dirName, true)) {
				if (folderName.name.equals(prefManager.userName+" (Checklist_App)", true)) {
					prefManager.folderID = folderName.id
					mailIDFolder_InDrive = true
//					Log.e(TAG,"3 MATCH FOUND: "+folderName.name)
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
//			Log.e(TAG,"5 GeneratedFound: "+file.name)
		}

		val driveFileList = mDriveService.files()
				.list()
				.setQ("mimeType='application/pdf' and trashed=false and '" + prefManager.folderID + "' in parents")
				.setSpaces("Drive")
				.execute()

		val arrayDriveFileNames:ArrayList<String> = ArrayList()
		driveFileList.files.forEach{file ->
			arrayDriveFileNames.add(file.name)
//			Log.e(TAG,"5 DRIVE Found: "+file.name)
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
//		Log.e(TAG,"5 Files to upload size: "+ filesToUpload.size)


		arrayGeneratedFiles.forEach { file -> if (!filterLocal.contains(file.name)) filesToUpload.remove(file)
//			Log.e(TAG,"5 From Upload Removed: "+file.name)
		}

		filesToUpload.forEach { file ->
//			Log.e(TAG,"5 SAVE: "+file.name)
			saveFile(file)
		}
	}

	private fun saveFile(uploadFile: java.io.File) {
        val fileMetadata = File()
        fileMetadata.setName(uploadFile.name)
        fileMetadata.setParents(Collections.singletonList(prefManager.folderID))
//		Log.e(TAG,"Saving in Folder: "+prefManager.folderID)
        val mediaContent = FileContent("application/pdf", uploadFile)
		try{
			mDriveService.files().create(fileMetadata, mediaContent)
					.setFields("id, parents")
					.execute()
			setOfList_DriveFiles.add(uploadFile.name)
			setOfList_LocalFiles.add(uploadFile.name)
		} catch (ex:Exception){
			Crashlytics.logException(ex)
		}
    }
}