package com.thatapp.checklists.ModelClasses

import android.Manifest
import android.app.Activity
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.thatapp.checklists.ViewClasses.MainActivity

import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URISyntaxException
import java.security.GeneralSecurityException
import java.util.HashMap


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DriveSyncService : JobService() {
    lateinit var pref: PrefManager
    internal var count = 0

    override fun onCreate() {
        super.onCreate()
        pref = PrefManager(applicationContext)

        Log.e(TAG, "Service created")
        //schedule();
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "Service destroyed")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // scheduleJob()

        Log.e(TAG, "inside onStartCommand")
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        scheduleJob()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // Stop tracking these job parameters, as we've 'finished' executing.
        Log.e(TAG, "on stop job: " + params.jobId)

        // Return false to drop the job.
        return false
    }


    private fun sendUpdate() {
        //todo location updates
        val strReq = object : StringRequest(Request.Method.POST,
                "url", Response.Listener { response ->
            try {
                val jsonArray = JSONArray(response)
                val jsonObject = jsonArray.getJSONObject(0)

                val error_code = jsonObject.getInt("error_code")
                Log.e("error code", " $error_code")
                if (error_code == 100) {
                    val dataArray = jsonObject.getJSONArray("data")
                    //oldSize = dataArray.length();
                    try {
                        val b = Bundle()
                        val intent = Intent()
                        // intent.action = MY_ACTION
                        intent.putExtra("dataFor", "home")
                        intent.putExtra("oldSize", dataArray.length())
                        //                            b.putSerializable("data",dataArray.toString());
                        //                            intent.putExtras(b);
                        intent.putExtra("data", dataArray.toString())
                        sendBroadcast(intent)


                    } catch (ex: Exception) {
                        Log.e("Broadcast 1", " $ex")
                    }

                    //                        showCancelHolidaysListView(cancelHolidayArray);

                    //                        showCancelHolidaysListView();
                } else if (error_code == 101) {
                    // Toast.makeText(getActivity(), getResources().getString(R.string.error_code_101), Toast.LENGTH_SHORT).show();
                } else if (error_code == 102) {
                    //  Toast.makeText(getActivity(), getResources().getString(R.string.error_code_102), Toast.LENGTH_SHORT).show();
                } else if (error_code == 103) {
                    //  Toast.makeText(getActivity(), getResources().getString(R.string.error_code_103), Toast.LENGTH_SHORT).show();
                } else if (error_code == 104) {
                    //   Toast.makeText(getActivity(), getResources().getString(R.string.error_code_104), Toast.LENGTH_SHORT).show();
                } else {
                    //  Toast.makeText(getActivity(), jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } catch (ex: JSONException) {

                //  Toast.makeText(getActivity(), "Error occurred please try again", Toast.LENGTH_SHORT).show();
                Log.e("Json Exception", " " + ex.toString())

            }
        }, Response.ErrorListener { error ->
            // progressDialog.dismiss();
            // Log.d("ERROR RESPONSE  - ", error.toString());

            Log.e("RESPONSE  - ", error.toString())
            if (error is NoConnectionError) {
                //    Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
                //showResponse(getResources().getString(R.string.no_internet), "Warning", 0);
            } else if (error is NetworkError) {
                // showResponse(getResources().getString(R.string.no_internet), "Warning", 0);
                //  Toast.makeText(getActivity(), getResources().getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
            } else if (error is TimeoutError) {
                //  showResponse(getResources().getString(R.string.timeout_error), "Warning", 0);
                //    Toast.makeText(getActivity(), getResources().getString(R.string.timeout_error), Toast.LENGTH_SHORT).show();
            } else if (error is ServerError) {
                //showResponse(getResources().getString(R.string.serverResponseTimeOut), "Warning", 0);
                //    Toast.makeText(getActivity(), getResources().getString(R.string.serverResponseTimeOut), Toast.LENGTH_SHORT).show();
            } else if (error is ParseError) {
                //showResponse("Unable to get response , Please try again", "Warning", 0);
                //  Toast.makeText(getActivity(), "Unable to get response , Please try again", Toast.LENGTH_SHORT).show();
            } else {
                // showResponse(getResources().getString(R.string.serverResponseTimeOut), "Warning", 0);
                //  Toast.makeText(getActivity(), "Server not responding please try again", Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["loc"] = "update"
                Log.e("Params ", " " + params.toString())
                return params
            }
        }

        val requestQueue = Volley.newRequestQueue(applicationContext)
        requestQueue.add(strReq)
        requestQueue.cancelAll(strReq)
        strReq.setShouldCache(false)
        strReq.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
    }

    fun scheduleJob() {

        val builder = JobInfo.Builder(1,
                ComponentName(packageName,
                        DriveSyncService::class.java.name))
        builder.setMinimumLatency((30 * 1000).toLong())
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)


        val mJobScheduler: JobScheduler
        mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e("error", " calling service from service")
        } else {
            driveSync()
            Log.e("Success ", " calling service from service ")
        }
    }

    private fun driveSync() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {

            val credential = GoogleAccountCredential.usingOAuth2(
                    this, setOf(DriveScopes.DRIVE))
            credential.selectedAccount = account.account
            var driveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory(),
                    credential)
                    .setApplicationName("Checklist")
                    .build()

            mDriveService = DriveSyncHelper(driveService, this)
            CheckDriveFileSync(mDriveService).execute(this)       }

    }

    fun checkService() {
        try {

            val account = GoogleSignIn.getLastSignedInAccount(this)

            if (account != null) {

                val credential = GoogleAccountCredential.usingOAuth2(
                        this, setOf(DriveScopes.DRIVE))
                credential.selectedAccount = account.account
                var driveService = Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                GsonFactory(),
                                credential)
                                .setApplicationName("Checklist")
                                .build()

                mDriveService = DriveSyncHelper(driveService, this)
//CheckDriveFileSync(mDriveService).execute(this)
//                mDriveService.driveSync()
            } else {

                val credential = GoogleAccountCredential.usingOAuth2(
                        this, setOf(DriveScopes.DRIVE))

                val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
                if (googleAccount != null) {
                    credential.selectedAccount = googleAccount.account
                    Log.e("login", "true")

                    //saveFile(fileName.name, fileName.name, "1234")
                    var driveService = Drive.Builder(
                            AndroidHttp.newCompatibleTransport(),
                            GsonFactory(),
                            credential)
                            .setApplicationName("Checklist")
                            .build()

                    mDriveService = DriveSyncHelper(driveService, this)

                } else {
                    Log.e("login", "false")
                }
            }
        } catch (ex: java.lang.Exception) {
            Log.e("service error ", ex.toString())
        }
    }


    companion object {
        private val TAG = DriveSyncService::class.java.simpleName
        lateinit var mDriveService: DriveSyncHelper
    }
    class CheckDriveFileSync(val driveServiceHelper: DriveSyncHelper) : AsyncTask<Context, Void, Boolean>() {

        override fun doInBackground(vararg p0: Context): Boolean? {

            try {

                driveServiceHelper.driveSync()
            } catch (e: Exception) {
                Log.e("create", "" + e.toString())
            }

            return false
        }


        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)

            Log.e("postExecute", "res  " + result)
        }
    }

}