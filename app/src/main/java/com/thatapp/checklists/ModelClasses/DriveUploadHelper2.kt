package com.thatapp.checklists.ModelClasses

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.IntentSender.SendIntentException
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings.Global.getString
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.app.ActivityCompat.startIntentSenderForResult
import android.util.Log
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.Drive.getDriveResourceClient
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.MetadataChangeSet
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import java.io.File
import java.util.*

/**
 * Android Drive Quickstart activity. This activity takes a photo and saves it
 * in Google Drive. The user is prompted with a pre-made dialog which allows
 * them to choose the file location.
 */
class DriveUploadHelper2(val fileToUpload: File, val context: Context) : ConnectionCallbacks, OnConnectionFailedListener {

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mBitmapToSave: File = fileToUpload

    /**
     * Create a new file and save it to Drive.
     */
    private fun saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.e(TAG, "Creating new contents.")
   //     createFolder()
        val image = mBitmapToSave
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(ResultCallback { result ->
                    // If the operation was not successful, we cannot do anything
                    // and must
                    // fail.
                    if (!result.status.isSuccess) {
                        Log.i(TAG, "Failed to create new contents.")
                        return@ResultCallback
                    }
                    // Otherwise, we can write our data to the new contents.
                    Log.i(TAG, "New contents created.")
                    // Get an output stream for the contents.
                    val outputStream = result.driveContents.outputStream
                    try {
                        outputStream.write(image.readBytes())
                    } catch (e1: IOException) {
                        Log.i(TAG, "Unable to write file contents.")
                    }

                    // Create the initial metadata - MIME type and title.
                    // Note that the user will be able to change the title later.
                    val metadataChangeSet = MetadataChangeSet.Builder()
                            .setMimeType("application/pdf")
                            .setTitle(fileToUpload.name)
                            .build()
                    // Create an intent for the file chooser, and start it.
                    val intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(result.driveContents)
                            .build(mGoogleApiClient!!)
                    try {
                        startIntentSenderForResult(
                                context as DisplayQuestionsActivity, intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0, null)
                    } catch (e: SendIntentException) {
                        Log.i(TAG, "Failed to launch file chooser.")
                    }
                })
    }

    init {
        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = GoogleApiClient.Builder(context)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient!!.connect()
    }

//	override fun onPause() {
//		if (mGoogleApiClient != null) {
//			mGoogleApiClient!!.disconnect()
//		}
//		super.onPause()
//	}

//	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
//		when (requestCode) {
//			REQUEST_CODE_CAPTURE_IMAGE ->
//				// Called after a photo has been taken.
//				if (resultCode == Activity.RESULT_OK) {
//					// Store the image data as a bitmap for writing later.
//					mBitmapToSave = data.extras!!.get("data") as Bitmap
//				}
//			REQUEST_CODE_CREATOR ->
//				// Called after a file is saved to Drive.
//				if (resultCode == Activity.RESULT_OK) {
//					Log.i(TAG, "Image successfully saved.")
//					mBitmapToSave = null
//					// Just start the camera again for another photo.
//					startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//							REQUEST_CODE_CAPTURE_IMAGE)
//				}
//		}
//	}

    override fun onConnectionFailed(result: ConnectionResult) {
        // Called whenever the API client fails to connect.
        Toast.makeText(context, "Internet connection failed", Toast.LENGTH_LONG).show()
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString())
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(context as Activity, result.errorCode, 0).show()
            return
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(context as Activity, REQUEST_CODE_RESOLUTION)
        } catch (e: SendIntentException) {
            Log.e(TAG, "Exception while starting resolution activity", e)
        }

    }

    override fun onConnected(connectionHint: Bundle?) {
//		Log.i(TAG, "API client connected.")
//		if (mBitmapToSave == null) {
//			// This activity has no UI of its own. Just start the camera.
//			startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE),
//					REQUEST_CODE_CAPTURE_IMAGE)
//			return
//		}


        saveFileToDrive()
    }


    override fun onConnectionSuspended(cause: Int) {
        Toast.makeText(context, "Internet connection problem", Toast.LENGTH_LONG).show()
        Log.i(TAG, "GoogleApiClient connection suspended")
    }



    companion object {

        private val TAG = "drive-quickstart"
        private val REQUEST_CODE_CAPTURE_IMAGE = 1
        private val REQUEST_CODE_CREATOR = 2
        private val REQUEST_CODE_RESOLUTION = 3
    }
}