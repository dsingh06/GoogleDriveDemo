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
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_main.*
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.support.annotation.NonNull
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


class MainActivity : AppCompatActivity(), ServiceListener {

    enum class ButtonState {
        LOGGED_OUT,
        LOGGED_IN
    }

    private val TAG = "MainActivity----"

    private val REQUEST_CODE_SIGN_IN = 1
    private val REQUEST_CODE_OPEN_DOCUMENT = 2


    private lateinit var mDriverServiceHelper: DriveServiceHelper
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
               //   mDriverServiceHelper.checkLoginStatus()
              } else {
                  Toast.makeText(applicationContext, "No Internet Connection.\nPlease ensure internet connectivity for accessing seamless services", Toast.LENGTH_LONG).show()
              }
      */
        linkVarsToViews()

        //mDriverServiceHelper.checkLoginStatus()
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

            openFilePicker()
            //mDriverServiceHelper.createFilePickerIntent()

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
            Log.e("clicked", " iv3")
            startActivity(intent)

        }
        view.setOnClickListener {
            Log.e("clicked", " VIEW")
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

                    openFileFromFilePicker(uri)
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
                            .setApplicationName("Checklist")
                            .build()

                    mDriverServiceHelper = DriveServiceHelper(googleDriveService,this,applicationContext)

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
        mGoogleSignInClient.signOut().addOnSuccessListener { Log.e("log", "out ") }.addOnCanceledListener { Log.e("log", "failed") }
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)
//        Log.e("acc", account!!.displayName)
    }

    fun signIn() {

        var signInIntent: Intent = mGoogleSignInClient.getSignInIntent()
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN)
    }

    private fun openFilePicker() {
        query()
        //    mDriverServiceHelper.listing()

        if (mDriverServiceHelper != null) {
            Log.e(TAG, "Opening file picker.")

            val pickerIntent = mDriverServiceHelper.createFilePickerIntent()

            // The result of the SAF Intent is handled in onActivityResult.
            startActivityForResult(pickerIntent, REQUEST_CODE_OPEN_DOCUMENT)
        }
    }

    private fun openFileFromFilePicker(uri: Uri) {
        if (mDriverServiceHelper != null) {

            Log.e(TAG, "Opening " + uri.path!!)

            mDriverServiceHelper.openFileUsingStorageAccessFramework(contentResolver, uri)
                    /*.addOnSuccessListener {
                        Log.e("file", "success")


                        var file: DocumentFile? = null
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                                file = DocumentFile.fromSingleUri(this, uri)
                                if (file != null) {
                                    //writeToFile()
                                }
                            }
                    }*/
                    .addOnSuccessListener { namePair ->
                        Log.e("values", namePair.first)

//                        mDriverServiceHelper.downloadFile("");
                        writeToFile(namePair.first!!, namePair.second!!)
                    }

                    .addOnFailureListener({ exception -> Log.e(TAG, "Unable to open file from picker.", exception) })
        }
    }

    fun writeToFile(fileName: String, data: String) {
        // Get the directory for the user's public pictures directory.
        val path = File(getFilesDir().absolutePath + File.separator + "/downloads/" + File.separator + "rishabh")
//        val path = java.io.File(+java.io.File.separator + "/downloads/")

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            val t = path.mkdirs()
            Log.e("path", path.getPath() + "   " + t)
        }
        val file = java.io.File(path, fileName)
        // Save your stream, don't forget to flush() it before closing it.
        try {
            file.createNewFile()
            val fOut = FileOutputStream(file)
            val myOutWriter = PrintWriter(fOut)
            myOutWriter.append(data)
            myOutWriter.close()
            fOut.flush()
            fOut.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }


    }

    fun copy(copy: DocumentFile, uri: Uri): Boolean {
        lateinit var inStream: InputStream
        lateinit var outStream: OutputStream
        val dir = copy//File(filesDir.absolutePath,copy)
        val mime = "application/vnd.ms-excel"
        var dir2 = DocumentFile.fromFile(File(filesDir.absolutePath + File.separator + "downloads", copy.name))
        val copiedFileName = "copied.jpg"
        val copiedMimeType = mime
        inStream = getContentResolver().openInputStream(copy.uri)
        outStream = getContentResolver().openOutputStream(dir2.createFile(copiedMimeType, copiedFileName.replace(".jpg", ""))!!.getUri())
        val DEFAULT_BUFFER_SIZE = 1024 * 4
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytesRead: Int
        do {

            bytesRead = inStream.read(buffer)

            if (bytesRead == null)

                break

            outStream.write(bytesRead)
            Log.e("outtt", "" + bytesRead)

        } while (true)
        Toast.makeText(this@MainActivity, "Succeed to create 'copied.jpg'", Toast.LENGTH_SHORT).show()
        // val copy2 = dir.copyTo(dir2, true)
        /*try {
            inStream = FileInputStream(copy)
            outStream = getContentResolver().openOutputStream(uri)
            val buffer = ByteArray(16384)
            var bytesRead: Int
            do {

                bytesRead = inStream.read(buffer)

                if (bytesRead == null)

                    break

                println(bytesRead)

            } while (true)

            /*  while ((bytesRead = inStream.read(buffer)) != -1) {
                  outStream.write(buffer, 0, bytesRead)
              }*/
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inStream.close()
                outStream.close()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }*/
        return false
    }


    fun query() {
        run({
            if (mDriverServiceHelper != null) {
                Log.e(TAG, "Querying for files.")
//                mDriverServiceHelper.queryA()
//

                mDriverServiceHelper.queryFiles()
                        .addOnSuccessListener({ fileList ->
                            val builder = StringBuilder()
                            for (file in fileList.getFiles()) {
                                builder.append(file.id).append("\t\t\t")
                                builder.append(file.getName()).append("\n")
                            }
                            val fileNames = builder.toString()
                            Log.e("file", fileNames)
//
//                            for (file in result.getFiles()) {
//                                System.out.printf("Found file: %s (%s)\n",
//                                        file.getName(), file.getId())
//                            }
                        })
                        .addOnFailureListener({ exception -> Log.e(TAG, "Unable to query files.", exception) })
            }
        })
    }
}





