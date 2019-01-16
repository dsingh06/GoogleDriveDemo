package com.thatapp.checklists.ModelClasses

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
import android.support.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class DriveSyncService : JobService() {
    lateinit var pref: PrefManager

    override fun onCreate() {
        super.onCreate()
        pref = PrefManager(applicationContext)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {
        scheduleJob()
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        // Stop tracking these job parameters, as we've 'finished' executing.
		// Log.e(TAG, "on stop job: " + params.jobId)
        // Return false to drop the job.
        return false
    }

    private fun scheduleJob() {
        val builder = JobInfo.Builder(1,
                ComponentName(packageName,
                        DriveSyncService::class.java.name))
        builder.setMinimumLatency((60 * 2 * 1000).toLong())
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

        val mJobScheduler: JobScheduler
        mJobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (mJobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS) driveSync()
    }

    private fun driveSync() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            CheckDriveFileSync().execute(this)
        }
    }

    class CheckDriveFileSync() : AsyncTask<Context, Void, Boolean>() {

        override fun doInBackground(vararg p0: Context): Boolean? {
            DriveUploader(p0[0])
            return false
        }
    }
}