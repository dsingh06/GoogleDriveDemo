package com.thatapp.checklists.ViewClasses

import android.app.Activity
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
import com.thatapp.checklists.ModelClasses.GoogleDriveConfig
import com.thatapp.checklists.ModelClasses.GoogleDriveService
import com.thatapp.checklists.R
import com.thatapp.checklists.ModelClasses.ServiceListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.content.Context
import android.support.annotation.NonNull
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
import com.thatapp.checklists.ModelClasses.DriveServiceHelper


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2


    private lateinit var mDriveService: DriveServiceHelper
    private var state = ButtonState.LOGGED_OUT

    lateinit var downloadAndSync: ConstraintLayout
    lateinit var myProfile: ConstraintLayout

    private val PROFILE_ACTIVITY = 33
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val isUserLoggedin: Boolean = false
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

        var gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        /*      val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

              val activeNetwork = cm.activeNetworkInfo
              val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
              if (isConnected) {
               //   mDriveService.checkLoginStatus()
              } else {
                  Toast.makeText(applicationContext, "No Internet Connection.\nPlease ensure internet connectivity for accessing seamless services", Toast.LENGTH_LONG).show()
              }
      */
        linkVarsToViews()

        //mDriveService.checkLoginStatus()
        if (!isUserLoggedin) {
            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                requestSignIn()
            } else {
                Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e("login ", "" + isUserLoggedin)
        }

        login.setOnClickListener {

            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                requestSignIn()
            } else {
                Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_SHORT).show()
            }
        }
        downloadAndSync.setOnClickListener {

            mDriveService.createFilePickerIntent()

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
            val intent = Intent(this, DisplayCheckListsActivity::class.java)
            startActivity(intent)
        }
        view.setOnClickListener {
            val intent = Intent(this, DisplayCheckListsActivity::class.java)
            startActivity(intent)
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
                    //  openFileFromFilePicker(uri)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData)
    }

    fun requestSignIn() {
        Log.e("service", "Requesting sign-in")
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))

                .build()

        val client = GoogleSignIn.getClient(this, signInOptions)

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.signInIntent, REQUEST_CODE_SIGN_IN)
    }


    /**
     * Handles the `result` of a completed sign-in activity initiated from [ ][.requestSignIn].
     */
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
                            .setApplicationName("Drive API Migration")
                            .build()

                    mDriveService = DriveServiceHelper(googleDriveService)

                    state = MainActivity.ButtonState.LOGGED_IN
                    setButtons()
                }
                .addOnFailureListener { exception -> Log.e(TAG, "Unable to sign in.", exception) }
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
        mGoogleSignInClient.signOut().addOnSuccessListener { Log.e("log","out ") }.addOnCanceledListener { Log.e("log","failed") }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        Log.e("acc", account!!.displayName)
    }

    fun signIn() {

        var signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }
}

