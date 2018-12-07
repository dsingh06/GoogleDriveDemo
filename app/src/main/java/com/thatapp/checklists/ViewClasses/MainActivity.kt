package com.thatapp.checklists.ViewClasses

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
import android.widget.Toast


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private lateinit var googleDriveService: GoogleDriveService
    private var state = ButtonState.LOGGED_OUT

    lateinit var downloadAndSync: ConstraintLayout
    lateinit var myProfile: ConstraintLayout

    private val PROFILE_ACTIVITY = 33


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
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
        if (isConnected) {
//            googleDriveService.auth()
        } else {
            Toast.makeText(applicationContext,"No Internet Connection.\nPlease ensure internet connectivity for accessing seamless services",Toast.LENGTH_LONG).show()
        }

        linkVarsToViews()
        val config = GoogleDriveConfig(
                getString(R.string.source_google_drive),
                GoogleDriveService.documentMimeTypes
        )
        googleDriveService = GoogleDriveService(this, config)
        googleDriveService.serviceListener = this
        val isUserLoggedin: Boolean = googleDriveService.checkLoginStatus()
        if (!isUserLoggedin) {
            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                googleDriveService.auth()
            } else {
                Toast.makeText(applicationContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            }
        }

        login.setOnClickListener {

            val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = cm.activeNetworkInfo
            val isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting
            if (isConnected) {
                googleDriveService.auth()
            } else {
                Toast.makeText(applicationContext,"No Internet Connection",Toast.LENGTH_SHORT).show()
            }
        }
        downloadAndSync.setOnClickListener {

            googleDriveService.pickFiles(null)

        }
        myProfile.setOnClickListener {
            startActivityForResult(Intent(this, ProfileActivity::class.java), PROFILE_ACTIVITY)
        }
        logout.setOnClickListener {
            googleDriveService.logout()
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

    private fun linkVarsToViews() {
        downloadAndSync = findViewById(R.id.downloadAndSyncLayout)
        myProfile = findViewById(R.id.myProfileLayout)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        googleDriveService.onActivityResult(requestCode, resultCode, data)
    }

    override fun loggedIn() {
        state = ButtonState.LOGGED_IN
        setButtons()
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


}
