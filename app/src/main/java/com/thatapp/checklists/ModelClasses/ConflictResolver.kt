package com.thatapp.checklists.ModelClasses


import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.ExecutionOptions
import com.google.android.gms.drive.events.CompletionEvent
import com.google.android.gms.tasks.Continuation

import java.io.OutputStreamWriter
import java.util.concurrent.ExecutorService

/**
 * ConflictResolver handles a CompletionEvent with a conflict status.
 */
 class ConflictResolver(conflictedCompletionEvent:CompletionEvent, context:Context,
                                   executorService:ExecutorService) {
    private var mBroadcaster:LocalBroadcastManager
    private var mConflictedCompletionEvent:CompletionEvent
    private var mContext:Context
    private lateinit var mDriveResourceClient:DriveResourceClient
    private lateinit var mDriveContents:DriveContents
    private lateinit var mBaseContent:String
    private lateinit var mModifiedContent:String
    private lateinit var mServerContent:String
    private lateinit var mResolvedContent:String
    private var mExecutorService:ExecutorService
    init{
        this.mConflictedCompletionEvent = conflictedCompletionEvent
        mBroadcaster = LocalBroadcastManager.getInstance(context)
        mContext = context
        mExecutorService = executorService
    }
    /**
     * Initiate the resolution process by connecting the GoogleApiClient.
     */
    fun resolve() { }
    /**
     * Notify the UI that the list should be updated.
     *
     * @param resolution Resolved grocery list.
     */
    private fun sendResult(resolution:String) {
        val intent = Intent(CONFLICT_RESOLVED)
        intent.putExtra("conflictResolution", resolution)
        mBroadcaster.sendBroadcast(intent)
    }
    companion object {
        private val TAG = "ConflictResolver"
        val CONFLICT_RESOLVED = "com.google.android.gms.drive.sample.conflict.CONFLICT_RESOLVED"
    }
}