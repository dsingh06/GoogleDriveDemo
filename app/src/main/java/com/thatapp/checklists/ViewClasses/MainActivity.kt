package com.thatapp.checklists.ViewClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
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
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.AsyncTask
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.thatapp.checklists.ModelClasses.*
import java.io.*


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2


    private var mDriverServiceHelper: DriveServiceHelper?=null
    private var mSignedInAccount: GoogleSignInAccount?=null
    private var mGoogleSignInClient: GoogleSignInClient?=null
    private var mGoogleSignInOptions: GoogleSignInOptions?=null

	companion object {
		val toastSuccessBackground = Color.parseColor("#228B22")
		val toastFailureBackground = Color.parseColor("#B22222")
	}


    private var state = ButtonState.LOGGED_OUT

    lateinit var checklistsOnline: ConstraintLayout
    lateinit var myProfile: ConstraintLayout

    private val PROFILE_ACTIVITY = 33

    private val isUserLoggedin: Boolean = false

    private lateinit var prefManager: PrefManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.thatapp.checklists.R.layout.activity_main)
        prefManager = PrefManager(this)

        if (prefManager.firstRun) prefManager.firstRun = false //app running first time

        linkVarsToViews()

        login.setOnClickListener {requestSignIn()} // Login button


        checklistsOnline.setOnClickListener {
			if (!isNetworkConnected()) {
				showSnack(toastFailureBackground,"NO INTERNET",Snackbar.LENGTH_SHORT)
			} else {
				if (mSignedInAccount != null) {
					startupCheck()
					openFilePicker()
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
				showSnack(toastFailureBackground,"NO INTERNET",Snackbar.LENGTH_SHORT)
			}else {
				logoutUser()
				state = ButtonState.LOGGED_OUT
				setButtons()
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
        view.setOnClickListener {
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
		if (mSignedInAccount===null) {
			state = ButtonState.LOGGED_OUT
			requestSignIn()
		} else {
			state = ButtonState.LOGGED_IN
			initialistDriveHelper(mSignedInAccount!!)
		}
		setButtons()
	}


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            REQUEST_CODE_SIGN_IN -> if (resultCode == Activity.RESULT_OK && resultData != null) {
                handleSignInResult(resultData)
            }

            REQUEST_CODE_OPEN_DOCUMENT -> if (resultCode == Activity.RESULT_OK && resultData != null) {
                val uri = resultData.data
                if (uri != null) {
                    openFileFromFilePicker(uri)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    fun requestSignIn() {
        if (isNetworkConnected()) {
//            Log.e("service", "Requesting sign-in")
            val signInOptions = getSignInOptions()
			mGoogleSignInClient = GoogleSignIn.getClient(this, signInOptions)
            val client = GoogleSignIn.getClient(this, signInOptions)

            // The result of the sign-in Intent is handled in onActivityResult.
            startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)

        } else {
			showSnack(toastFailureBackground, "NO INTERNET", Snackbar.LENGTH_LONG)
		}
    }

	private fun getSignInOptions(): GoogleSignInOptions {
		mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestEmail()
				.requestScopes(Scope(DriveScopes.DRIVE))
				.build()
		return mGoogleSignInOptions!!
	}

	private fun isNetworkConnected(): Boolean {
		val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val activeNetwork = cm.activeNetworkInfo
		return (activeNetwork != null && activeNetwork.isConnectedOrConnecting)
	}


	/**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    @SuppressLint("ServiceCast")
    fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { googleAccount ->
                    // Use the authenticated account to sign in to the Drive service.
                    initialistDriveHelper(googleAccount)
                    prefManager.loginEmail = googleAccount.email
                    prefManager.loginStatus = true
//                    prefManager.userName = googleAccount.displayName
//                    prefManager.dirName = googleAccount.email

                    //  PrefManager.dirName = googleAccount.email.toString().split("@").get(0)
                    prefManager.dirName = googleAccount.email.toString().split("@").get(0)
                    Log.e("name", "  dir " + prefManager.dirName)
                    state = MainActivity.ButtonState.LOGGED_IN
                    startupCheck()
                    setButtons()

                    if (prefManager.rootFolderID!!.length > 5) {
                        val LOAD_ARTWORK_JOB_ID = 101
                        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                        jobScheduler.schedule(JobInfo.Builder(LOAD_ARTWORK_JOB_ID,
                                ComponentName(this, DriveSyncService::class.java))
                                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                                .build())
                    }
                }
                .addOnFailureListener { exception ->
					showSnack(toastFailureBackground,exception.toString(),Snackbar.LENGTH_LONG)
                    prefManager.loginStatus = false
                }
    }

	private fun initialistDriveHelper(googleAccount:GoogleSignInAccount) {
		val credential = GoogleAccountCredential.usingOAuth2(
				this, setOf(DriveScopes.DRIVE_FILE))
		credential.selectedAccount = googleAccount.account

		val googleDriveService = Drive.Builder(
				AndroidHttp.newCompatibleTransport(),
				GsonFactory(),
				credential)
				.setApplicationName("Checklist App")
				.build()

		mDriverServiceHelper = DriveServiceHelper(googleDriveService, this, applicationContext)
	}


	private fun linkVarsToViews() {
        checklistsOnline = findViewById(R.id.downloadAndSyncLayout)
        myProfile = findViewById(R.id.myProfileLayout)
    }

    override fun loggedIn() {

    }

    override fun fileDownloaded(file: File, fileName: String) {
		showSnack(toastSuccessBackground,"File download complete", Snackbar.LENGTH_LONG)
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

    private fun logoutUser() {
			mGoogleSignInClient?.signOut()?.addOnSuccessListener {
				showSnack(toastSuccessBackground, "Logout success!", Snackbar.LENGTH_SHORT)
				prefManager.dirName = ""
				state = ButtonState.LOGGED_OUT
				setButtons()
			}?.addOnCanceledListener {
				showSnack(toastFailureBackground, "Logout cancelled!", Snackbar.LENGTH_SHORT)
			}?.addOnFailureListener {
				showSnack(toastFailureBackground, "Failed to logout. Check internet!", Snackbar.LENGTH_LONG)
			}
    }

	private fun showSnack(backgroundColor:Int, message: String, length:Int) {
		val snack = Snackbar.make(main_layout,message,length)
		val view = snack.getView()
		view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
//		toast.setGravity(Gravity.BOTTOM,0,0)
		snack.show()
	}

	private fun openFilePicker() {
        if (mDriverServiceHelper != null) {
            val pickerIntent = mDriverServiceHelper?.createFilePickerIntent()
            // The result of the SAF Intent is handled in onActivityResult.
            if (pickerIntent!=null) startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT)
        }
    }

    private fun openFileFromFilePicker(uri: Uri) {
        if (mDriverServiceHelper != null) {
            mDriverServiceHelper?.openFileUsingStorageAccessFramework(contentResolver, uri)

                    ?.addOnSuccessListener { namePair ->
                        //                        Log.e("values", namePair.first)
                    }
                    ?.addOnFailureListener({ exception ->
                        requestSignIn()
                    })
        }
    }

    private fun startupCheck() {
        if (mDriverServiceHelper != null) {
            CheckDriveSync(mDriverServiceHelper!!).execute(this)
        }
    }

	private fun setButtons() {
		when (state) {
			ButtonState.LOGGED_OUT -> {
				status.text = getString(R.string.status_logged_out)
				//start.isEnabled = false
				logout.isEnabled = false
				login.isEnabled = true
				login.visibility = View.VISIBLE
				logout.visibility = View.INVISIBLE

			}
			else -> {
				status.text = ("Last logged in as "+"'"+ prefManager.loginEmail.toString().substringBefore("@")+"'")
				//start.isEnabled = true
				logout.isEnabled = true
				login.isEnabled = false
				login.visibility = View.INVISIBLE
				logout.visibility = View.VISIBLE
			}
		}
	}

	class CheckDriveSync(val driveServiceHelper: DriveServiceHelper) : AsyncTask<Context, Void, Boolean>() {

        override fun doInBackground(vararg p0: Context): Boolean? {
            try {
                driveServiceHelper.driveCheck()
            } catch (e: Exception) {
                Log.e("Main-- driveCheck", "" + e.toString())
            }
            return false
        }
    }
}