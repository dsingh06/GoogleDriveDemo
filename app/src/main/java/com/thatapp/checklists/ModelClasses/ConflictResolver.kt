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
    fun resolve() {
        // [START drive_android_resolve_conflict]
        // A new DriveResourceClient should be created to handle each new CompletionEvent since each
        // event is tied to a specific user account. Any DriveFile action taken must be done using
        // the correct account.
        val signInOptionsBuilder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
        if (mConflictedCompletionEvent.getAccountName() != null)
        {
            signInOptionsBuilder.setAccountName(mConflictedCompletionEvent.getAccountName())
        }
        val signInClient = GoogleSignIn.getClient(mContext, signInOptionsBuilder.build())
        signInClient.silentSignIn()
                .continueWith(mExecutorService,
                        {
                            mDriveResourceClient = Drive.getDriveResourceClient(
                                    mContext, it.getResult())
                            mBaseContent = ConflictUtil.getStringFromInputStream(
                                    mConflictedCompletionEvent.getBaseContentsInputStream()!!)
                            mModifiedContent = ConflictUtil.getStringFromInputStream(
                                    mConflictedCompletionEvent
                                            .getModifiedContentsInputStream()!!)
                            null } as Continuation<GoogleSignInAccount, Void>)
                .continueWithTask(this.mExecutorService,
                        {
                            val driveId = mConflictedCompletionEvent.driveId
                            this.mDriveResourceClient.openFile(
                                    driveId.asDriveFile(), DriveFile.MODE_READ_ONLY) })
                .continueWithTask(mExecutorService,
                        {
                            mDriveContents = it.getResult()
                            val serverInputStream = it.getResult().getInputStream()
                            mServerContent = ConflictUtil.getStringFromInputStream(serverInputStream)
                            mDriveResourceClient.reopenContentsForWrite(mDriveContents) })
                .continueWithTask(mExecutorService,
                        {
                            val contentsForWrite = it.getResult()
                            mResolvedContent = ConflictUtil.resolveConflict(
                                    mBaseContent, mServerContent, mModifiedContent)
                            val outputStream = contentsForWrite.getOutputStream()
                            OutputStreamWriter(outputStream).use({ writer-> writer.write(mResolvedContent) })
                            // It is not likely that resolving a conflict will result in another
                            // conflict, but it can happen if the file changed again while this
                            // conflict was resolved. Since we already implemented conflict
                            // resolution and we never want to miss user data, we commit here
                            // with execution options in conflict-aware mode (otherwise we would
                            // overwrite server content).
                            val executionOptions = ExecutionOptions.Builder()
                                    .setNotifyOnCompletion(true)
                                    .setConflictStrategy(
                                            ExecutionOptions
                                                    .CONFLICT_STRATEGY_KEEP_REMOTE)
                                    .build()
                            // Commit resolved contents.
                            val modifiedMetadataChangeSet = mConflictedCompletionEvent.getModifiedMetadataChangeSet()
                            mDriveResourceClient.commitContents(contentsForWrite,
                                    modifiedMetadataChangeSet, executionOptions) })
                .addOnSuccessListener({
                    mConflictedCompletionEvent.dismiss()
                    Log.d(TAG, "resolved list")
                    sendResult(mModifiedContent) })
                .addOnFailureListener({ e->
                    // The contents cannot be reopened at this point, probably due to
                    // connectivity, so by snoozing the event we will get it again later.
                    Log.d(TAG, "Unable to write resolved content, snoozing completion event.",
                            e)
                    mConflictedCompletionEvent.snooze()
                    if (mDriveContents != null)
                    {
                        mDriveResourceClient.discardContents(mDriveContents)
                    } })
        // [END drive_android_resolve_conflict]
    }
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