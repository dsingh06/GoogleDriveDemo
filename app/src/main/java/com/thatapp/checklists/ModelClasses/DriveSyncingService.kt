package com.thatapp.checklists.ModelClasses

import android.util.Log
import com.google.android.gms.drive.events.ChangeEvent
import com.google.android.gms.drive.events.DriveEventService


class MyDriveEventService : DriveEventService() {

    override fun onChange(event: ChangeEvent) {
        Log.e("Sync", event.toString())

    }

}