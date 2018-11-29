package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.tasks.Task
import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.Continuation
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import java.io.File
import android.support.v4.app.ActivityCompat.startIntentSenderForResult
import android.content.IntentSender
import com.google.android.gms.drive.CreateFileActivityOptions
import com.google.android.gms.drive.MetadataChangeSet
import java.io.ByteArrayOutputStream
import java.io.IOException

class DriveUploadHelper (val fileToUpload: File, val context: Context){

	private val REQUEST_CODE_CREATOR = 13
	private val TAG = "MyDriveHelper"

	val mDriveClient = Drive.getDriveClient(context as DisplayQuestionsActivity, GoogleSignIn.getLastSignedInAccount(context)!!);
	val mDriveResourceClient = Drive.getDriveResourceClient(context as DisplayQuestionsActivity, GoogleSignIn.getLastSignedInAccount(context)!!);

	/** Create a new file and save it to Drive.  */

	fun saveFileToDrive() {
		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.")
		mDriveResourceClient
				.createContents()
				.continueWithTask(
						object : Continuation<DriveContents, Task<Void>> {
							@Throws(Exception::class)
							override fun then(task: Task<DriveContents>): Task<Void> {
								return createFileIntentSender(task.result!!, fileToUpload)
							}
						})
				.addOnFailureListener { e -> Log.w(TAG, "Failed to create new contents.", e) }
	}

	private fun createFileIntentSender(driveContents: DriveContents, image: File): Task<Void> {
		Log.i(TAG, "New contents created.")
		// Get an output stream for the contents.
		val outputStream = driveContents.outputStream
		// Write the bitmap data from it.
		try {
			outputStream.write(image.readBytes())
		} catch (e: IOException) {
			Log.w(TAG, "Unable to write file contents.", e)
		}

		// Create the initial metadata - MIME type and title.
		// Note that the user will be able to change the title later.
		val metadataChangeSet = MetadataChangeSet.Builder()
				.setMimeType("application/pdf")
				.setTitle(fileToUpload.name)
				.build()
		// Set up options to configure and display the create file activity.
		val createFileActivityOptions = CreateFileActivityOptions.Builder()
				.setInitialMetadata(metadataChangeSet)
				.setInitialDriveContents(driveContents)
				.build()

		return mDriveClient
				.newCreateFileActivityIntentSender(createFileActivityOptions)
				.continueWith{ task ->
							startIntentSenderForResult(context as DisplayQuestionsActivity,task.getResult()!!, REQUEST_CODE_CREATOR, null, 0, 0, 0,null)
							null
						}
	}
}