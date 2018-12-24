package com.thatapp.checklists.ViewClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.arch.lifecycle.LiveData
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.common.data.DataBuffer
import com.google.android.gms.drive.DriveResource
import com.google.android.gms.drive.MetadataBuffer
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Task
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_main.*
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.DocumentsContract
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.support.v4.provider.DocumentFile
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import com.thatapp.checklists.ModelClasses.*
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2


    public lateinit var mDriverServiceHelper: DriveServiceHelper
    private var state = ButtonState.LOGGED_OUT

    lateinit var downloadAndSync: ConstraintLayout
    lateinit var myProfile: ConstraintLayout

    private val PROFILE_ACTIVITY = 33
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val isUserLoggedin: Boolean = false
    private lateinit var prefManager: PrefManager

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
        setContentView(com.thatapp.checklists.R.layout.activity_main)
        prefManager = PrefManager(this)
//        Log.e("Login", "status is: " + prefManager.firstRun)

        if (prefManager.firstRun) {
            prefManager.firstRun = false
//            Log.e("Login", "status is: " + prefManager.firstRun)
        }


        if (!prefManager.loginStatus) {
            requestSignIn()
        }


        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        linkVarsToViews()

        if (!isUserLoggedin) {
            requestSignIn()
        }

        login.setOnClickListener {
            requestSignIn()
        }
        downloadAndSync.setOnClickListener {

            val account = GoogleSignIn.getLastSignedInAccount(this)

            if (account != null) {
                startupCheck()

                openFilePicker()
            } else {
                Toast.makeText(applicationContext, "Please Login to continue", Toast.LENGTH_SHORT).show()
                requestSignIn()
            }


        }
        myProfile.setOnClickListener {
            startActivityForResult(Intent(this, ProfileActivity::class.java), PROFILE_ACTIVITY)
        }

        logout.setOnClickListener {
            logoutUser()
            state = ButtonState.LOGGED_OUT
            setButtons()
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

        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        if (isConnected) {

            Log.e("service", "Requesting sign-in")
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestScopes(Scope(DriveScopes.DRIVE))
                    .build()

            val client = GoogleSignIn.getClient(this, signInOptions)

            // The result of the sign-in Intent is handled in onActivityResult.
            startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)

        } else {
            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT).show()
        }


    }


    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
    @SuppressLint("ServiceCast")
    fun handleSignInResult(result: Intent) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener { googleAccount ->
                    Log.e(TAG, "Signed in as " + googleAccount.email!!)

                    // Use the authenticated account to sign in to the Drive service.
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
                    prefManager.loginEmail = googleAccount.email
                    prefManager.loginStatus = true
                    prefManager.userName = googleAccount.displayName
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
                    Log.e(TAG, "Unable to sign in.", exception)
                    prefManager.loginStatus = false
                }
    }


    private fun linkVarsToViews() {
        downloadAndSync = findViewById(R.id.downloadAndSyncLayout)
        myProfile = findViewById(R.id.myProfileLayout)
    }

    override fun loggedIn() {

    }

    override fun fileDownloaded(file: File, fileName: String) {
        Snackbar.make(main_layout, "File download complete", Snackbar.LENGTH_LONG).show()
    }

    override fun cancelled() {
        Snackbar.make(main_layout, R.string.status_user_cancelled, Snackbar.LENGTH_LONG).show()
    }

    override fun handleError(exception: Exception) {
        if (exception.message === "Sign-in failed.") setButtons()
        val errorMessage = getString(R.string.status_error, exception.message)
        Snackbar.make(main_layout, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    private fun logoutUser() {
        mGoogleSignInClient.signOut().addOnSuccessListener {
            Log.e("log", "out ")
            prefManager.userName = ""
            prefManager.companyName = ""
            prefManager.dirName = ""
            prefManager.jobTitle = ""
        }.addOnCanceledListener { Log.e("log", "failed") }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
    }


    private fun openFilePicker() {
        if (mDriverServiceHelper != null) {
            val pickerIntent = mDriverServiceHelper.createFilePickerIntent()

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT)
        }
    }

    private fun openFileFromFilePicker(uri: Uri) {
        if (mDriverServiceHelper != null) {
            mDriverServiceHelper.openFileUsingStorageAccessFramework(contentResolver, uri)

                    .addOnSuccessListener { namePair ->
                        //                        Log.e("values", namePair.first)
                    }
                    .addOnFailureListener({ exception ->
                        requestSignIn()
                    })
        }
    }

    fun startupCheck() {
        if (mDriverServiceHelper != null) {
//            Log.e(TAG, "Querying for files.")
            CheckDriveSync(mDriverServiceHelper).execute(this)
            if (prefManager.jobTitle!!.length < 3 || prefManager.companyName!!.length < 3) {
                Toast.makeText(applicationContext, "Please Complete Your Profile", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ProfileActivity::class.java))
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

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
//            Log.e("Main-- onPEx", "res  " + result)
        }
    }
}