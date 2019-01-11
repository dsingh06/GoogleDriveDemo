package com.thatapp.checklists.ViewClasses

import android.app.*
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_main.*
import android.net.ConnectivityManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.OpenableColumns
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import com.thatapp.checklists.ModelClasses.*
import java.io.*

class MainActivity : AppCompatActivity(), ServiceListener {

	enum class ButtonState {
		LOGGED_OUT,
		LOGGED_IN
	}

	private val TAG = "From MainActivity: "
	private val REQUEST_CODE_SIGN_IN = 1
	private val REQUEST_CODE_DOWNLOAD_DOCUMENTS = 2
	private var mSignedInAccount: GoogleSignInAccount? = null
	private var mGoogleSignInClient: GoogleSignInClient? = null
	private var mGoogleSignInOptions: GoogleSignInOptions? = null
	private var state = ButtonState.LOGGED_OUT
	lateinit var checklistsOnline: ConstraintLayout
	lateinit var myProfile: ConstraintLayout
	private val PROFILE_ACTIVITY = 33
	private lateinit var prefManager: PrefManager

	val UPLOAD_JOB_SCHEDULER_ID = 31

	companion object {
		val toastSuccessBackground = Color.parseColor("#228B22")
		val toastFailureBackground = Color.parseColor("#B22222")
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(com.thatapp.checklists.R.layout.activity_main)
		prefManager = PrefManager(this)

		//For future use
		if (prefManager.firstRun) prefManager.firstRun = false //app running first time

		linkVarsToViews() // Link class to views layouts

		if (mSignedInAccount != null && mGoogleSignInClient != null) {
			startupCheck()
		} else {
			requestSignIn()
		}

		login.setOnClickListener {
			requestSignIn()
		} // Login button
		checklistsOnline.setOnClickListener {
			if (!isNetworkConnected()) {
				showSnack(toastFailureBackground, "NO INTERNET", Snackbar.LENGTH_SHORT)
			} else {
				if (mSignedInAccount != null && mGoogleSignInClient != null) {
					openSAFFilePicker()
				} else {
					requestSignIn()
				}
			}
		}
		myProfile.setOnClickListener {
			startActivityForResult(Intent(this, ProfileActivity::class.java), PROFILE_ACTIVITY)
		}
		logout.setOnClickListener {
			if (!isNetworkConnected()) {
				showSnack(toastFailureBackground, "NO INTERNET", Snackbar.LENGTH_SHORT)
			} else {
				logoutUser()
			}
		}
		imageView3.setOnClickListener {
			if (prefManager.jobTitle!!.length < 3 || prefManager.companyName!!.length < 3) {
				Toast.makeText(applicationContext, "Please Complete Your Profile", Toast.LENGTH_SHORT).show()
				startActivity(Intent(this, ProfileActivity::class.java))
			} else {
				val intent = Intent(this, DisplayCheckListsActivity::class.java)
				startActivity(intent)

			}
		}
		customBackgroundIV.setOnClickListener {
			if (prefManager.jobTitle!!.length < 3 || prefManager.companyName!!.length < 3) {
				Toast.makeText(applicationContext, "Please Complete Your Profile", Toast.LENGTH_SHORT).show()
				startActivity(Intent(this, ProfileActivity::class.java))
			} else {
				val intent = Intent(this, DisplayCheckListsActivity::class.java)
				startActivity(intent)
			}
		}
	}


	override fun onResume() {
		super.onResume()
		mSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
		if (mSignedInAccount === null) {
			state = ButtonState.LOGGED_OUT
			requestSignIn()
		} else {
			state = ButtonState.LOGGED_IN
			mGoogleSignInClient = getSignInClient()
			prefManager.loginEmail = mSignedInAccount!!.email
			prefManager.loginStatus = true
			prefManager.dirName = mSignedInAccount!!.email.toString().split("@").get(0)
			if (!isJobServiceOn()) setJobScheduler()
			startupCheck()
		}
		setButtons()
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
		super.onActivityResult(requestCode, resultCode, resultData)

		when (requestCode) {
			REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && resultData != null) {
				Log.e(TAG, "Going to Handle Signin result")
				handleSignInResult(resultData) // Where jobScheduler is set
			} else {
				showSnack(toastFailureBackground, "Unable to Sign-In", Snackbar.LENGTH_SHORT)
			}
			REQUEST_CODE_DOWNLOAD_DOCUMENTS -> if (resultCode == Activity.RESULT_OK && resultData != null) {
				val uri = resultData.data
				if (uri != null) {
					downloadFileFromFilePicker(uri)
				}
			}
		}
	}

	/**
	 * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
	 */
//    @SuppressLint("ServiceCast")
	private fun handleSignInResult(result: Intent) {
		GoogleSignIn.getSignedInAccountFromIntent(result)
				.addOnSuccessListener { googleAccount ->
					mSignedInAccount = googleAccount
					// Use the authenticated account to sign in to the Drive service.
					prefManager.loginEmail = mSignedInAccount!!.email
					prefManager.loginStatus = true
					prefManager.dirName = googleAccount.email.toString().split("@").get(0)
					state = MainActivity.ButtonState.LOGGED_IN
					startupCheck()
					setButtons()
					if (!isJobServiceOn()) setJobScheduler()
				}
				.addOnFailureListener { exception ->
					showSnack(toastFailureBackground, exception.toString(), Snackbar.LENGTH_LONG)
					prefManager.loginStatus = false
				}
	}


	private fun requestSignIn() {
		if (isNetworkConnected()) {
			mGoogleSignInClient = getSignInClient()
			mGoogleSignInClient?.silentSignIn()
					?.addOnSuccessListener {
						mSignedInAccount = it
						//createDriveClients(googleSignInAccount);
					}
					?.addOnFailureListener {
						// Silent sign-in failed, display account selection prompt
						startActivityForResult(
								mGoogleSignInClient?.getSignInIntent(), REQUEST_CODE_SIGN_IN);
					}
		} else {
			showSnack(toastFailureBackground, "NO INTERNET", Snackbar.LENGTH_LONG)
		}
	}

	private fun getSignInClient(): GoogleSignInClient {
		mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.requestScopes(Scope(DriveScopes.DRIVE))
				.build()
		return GoogleSignIn.getClient(this, mGoogleSignInOptions!!)
	}

	private fun isNetworkConnected(): Boolean {
		val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetwork = cm.activeNetworkInfo
		return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)
	}

	private fun setJobScheduler() {
		Log.e(TAG,"1 Job Scheduling Setup")
		val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		jobScheduler.schedule(JobInfo.Builder(UPLOAD_JOB_SCHEDULER_ID,
				ComponentName(this, DriveSyncService::class.java))
				.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
				.build())
	}

	private fun isJobServiceOn(): Boolean {
		val scheduler: JobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
		var hasBeenScheduled = false

		for (jobInfo in scheduler.allPendingJobs) {
			if (jobInfo.getId() == UPLOAD_JOB_SCHEDULER_ID) {
				hasBeenScheduled = true;
				break
			}
		}
		return hasBeenScheduled
	}


	private fun linkVarsToViews() {
		checklistsOnline = findViewById(R.id.downloadAndSyncLayout)
		myProfile = findViewById(R.id.myProfileLayout)
	}

	override fun loggedIn() {}

	override fun fileDownloaded(file: File, fileName: String) {
		showSnack(toastSuccessBackground, "File download complete", Snackbar.LENGTH_LONG)
		val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.cancel(1);
	}

	override fun cancelled() {
		showSnack(toastFailureBackground, "User cancelled", Snackbar.LENGTH_LONG)
	}


	override fun handleError(exception: Exception) {
		if (exception.message === "Sign-in failed.") setButtons()
		val errorMessage = getString(R.string.status_error, exception.message)
		val snack = Snackbar.make(main_layout, errorMessage, Snackbar.LENGTH_LONG)
		val view = snack.view
		view.setBackgroundColor(toastFailureBackground)
		snack.show()
	}

	override fun fileDownloading(fileName: String) = showNotification(fileName)

	private fun logoutUser() {
		mGoogleSignInClient?.signOut()
				?.addOnSuccessListener {
					showSnack(toastSuccessBackground, "Logout success!", Snackbar.LENGTH_SHORT)
					prefManager.dirName = ""
					state = MainActivity.ButtonState.LOGGED_OUT
					setButtons()
				}
				?.addOnCanceledListener {
					showSnack(toastFailureBackground, "Logout cancelled!", Snackbar.LENGTH_SHORT)
				}
				?.addOnFailureListener {
					showSnack(toastFailureBackground, "Failed to logout. Check internet!", Snackbar.LENGTH_LONG)
				}
	}

	private fun showSnack(backgroundColor: Int, message: String, length: Int) {
		val snack = Snackbar.make(main_layout, message, length)
		val view = snack.getView()
		view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
		snack.show()
	}

	// Good to Go
	private fun openSAFFilePicker() { // OPEN STORAGE ACCESS FRAMEWORK TO PICK FILES
		// SAF based file picker intent
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
		intent.addCategory(Intent.CATEGORY_OPENABLE) // to avoid receiving virtual files
		intent.type = "*/*"
		val mimeTypes = arrayOf("application/vnd.ms-excel")
		intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
		startActivityForResult(intent, REQUEST_CODE_DOWNLOAD_DOCUMENTS)
	}

	//Good to go
	private fun downloadFileFromFilePicker(uri: Uri) {
		// Retrieve the metadata as a File object.
		try {
			contentResolver?.query(uri, null, null, null, null)!!.use { cursor ->
				if (cursor.moveToFirst()) {
					val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
					val fileName = cursor.getString(nameIndex)
					fileDownloading(fileName) // For notification
					val inputStream: InputStream? = contentResolver.openInputStream(uri) //FileNotFound Exception could be thrown
					val storageDir = getFilesDir()
					prefManager = PrefManager(this)
					val filep = java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads")
					val des = java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName)
					des.setWritable(true, false)

					val outputStream = FileOutputStream(des)
					val buffer = ByteArray(1024)
					var length = inputStream!!.read(buffer)
					while (length > 0) {
						outputStream.write(buffer, 0, length)
						length = inputStream.read(buffer)
					}
					outputStream.close()
					inputStream.close()
					fileDownloaded(des, "")
				}
			}
		} catch (e: Exception) {
			handleError(e)
		}
	}


	private fun startupCheck() {
		CheckDriveSync().execute(this)
	}

	private fun setButtons() {
		when (state) {
			MainActivity.ButtonState.LOGGED_OUT -> {
				status.text = getString(R.string.status_logged_out)
				//start.isEnabled = false
				logout.isEnabled = false
				login.isEnabled = true
				login.visibility = View.VISIBLE
				logout.visibility = View.INVISIBLE

			}
			else -> {
				status.text = ("Last logged in as " + "'" + prefManager.loginEmail.toString().substringBefore("@") + "'")
				//start.isEnabled = true
				logout.isEnabled = true
				login.isEnabled = false
				login.visibility = View.INVISIBLE
				logout.visibility = View.VISIBLE
			}
		}
	}

	private fun showNotification(title: String) {
		var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		var notificationId = 1
		var channelId = "channel-01";
		var channelName = "checkList";
		var importance = NotificationManager.IMPORTANCE_HIGH;

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			var mChannel = NotificationChannel(
					channelId, channelName, importance);
			notificationManager.createNotificationChannel(mChannel);
		}

		var mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
				.setContentTitle("CheckList App")
				.setContentText(title + " is Downloading")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notify))

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			mBuilder.setSmallIcon(getNotificationIcon(mBuilder))

		} else {
			mBuilder.setSmallIcon(R.drawable.ic_notify)
		}

		notificationManager.notify(notificationId, mBuilder.build())
	}

	private fun getNotificationIcon(notificationBuilder: NotificationCompat.Builder): Int {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			var color = resources.getColor(R.color.primary_dark_material_dark)
			notificationBuilder.setColor(color)
			return R.drawable.ic_notify
		}
		return R.drawable.ic_notify
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class CheckDriveSync() : AsyncTask<Context, Void, Void>() {
        override fun doInBackground(vararg p0: Context): Void? {
			Log.e("From MainActivity: ","In Async Task")
			DriveUploader(p0[0])
			return null
        }
    }
}