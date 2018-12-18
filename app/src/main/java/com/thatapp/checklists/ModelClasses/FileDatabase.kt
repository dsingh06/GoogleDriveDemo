package com.thatapp.checklists.ModelClasses

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [FileModel::class], version = 1)
abstract class FileDatabase: RoomDatabase() {
     abstract fun daoAccess():DaoAccess
}