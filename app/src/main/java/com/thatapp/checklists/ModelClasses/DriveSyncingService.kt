package com.thatapp.checklists.ModelClasses

import android.util.Log

import com.google.android.gms.drive.events.CompletionEvent
import com.google.android.gms.drive.events.DriveEventService

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// [START drive_android_on_completion]
class DriveSyncingService : DriveEventService() {
    private var mExecutorService: ExecutorService? = null

    override fun onCreate() {
        super.onCreate()
        // [START_EXCLUDE]
        mExecutorService = Executors.newSingleThreadExecutor()
        // [END_EXCLUDE]
    }

    @Synchronized
    override fun onDestroy() {
        super.onDestroy()
        // [START_EXCLUDE]
        mExecutorService!!.shutdown()
        // [END_EXCLUDE]
    }

    override fun onCompletion(event: CompletionEvent) {
        var eventHandled = false
        when (event.status) {
            CompletionEvent.STATUS_SUCCESS -> {
                // Commit completed successfully.
                // Can now access the remote resource Id
                // [START_EXCLUDE]
                val resourceId = event.driveId.resourceId
                Log.d(TAG, "Remote resource ID: " + resourceId!!)
                eventHandled = true
            }
            CompletionEvent.STATUS_FAILURE ->
                // Handle failure....
                // Modified contents and metadata failed to be applied to the server.
                // They can be retrieved from the CompletionEvent to try to be applied later.
                // [START_EXCLUDE]
                // CompletionEvent is only dismissed here. In a real world application failure
                // should be handled before the event is dismissed.
                eventHandled = true
            CompletionEvent.STATUS_CONFLICT -> {
                // Handle completion conflict.
                // [START_EXCLUDE]
                val conflictResolver = mExecutorService?.let { ConflictResolver(event, this, it) }
                if (conflictResolver != null) {
                    conflictResolver.resolve()
                }
                eventHandled = false // Resolver will snooze or dismiss
            }
        }// [END_EXCLUDE]
        // [END_EXCLUDE]
        // [END_EXCLUDE]

        if (eventHandled) {
            event.dismiss()
        }
    }

    companion object {
        private val TAG = "DriveSyncingService"
    }
}