package com.thatapp.checklists.ModelClasses

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.os.AsyncTask

class FilesRepository(context: Context) {
    private val DB_NAME = "checklist_app"
    private val fileDatabase: FileDatabase
    val tasks: LiveData<List<FileModel>>
        get() {
            return fileDatabase.daoAccess().fetchAllTasks()
        }

    init {
        fileDatabase = Room.databaseBuilder(context, FileDatabase::class.java, DB_NAME).build()
    }

    @JvmOverloads
    fun insertTask(title: String,
                   description: String) {
        val file = FileModel()
        file.fileName = title
        file.dirName = "aaa"
        file.createdBy = "rr"
        file.createdAt = "sda"
        file.modifiedAt = "dddadf"
        insertTask(file)
    }

    fun insertTask(file: FileModel) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                fileDatabase.daoAccess().insertTask(file)
                return null
            }
        }.execute()
    }

    fun updateTask(file: FileModel) {
        file.modifiedAt ="1121"
        object : AsyncTask<Void, Void, Void>() {
            protected override fun doInBackground(vararg voids: Void): Void? {
                fileDatabase.daoAccess().updateTask(file)
                return null
            }
        }.execute()
    }

    fun deleteTask(id: Int) {
        val task = getTask(id)
        if (task != null) {
            object : AsyncTask<Void, Void, Void>() {
                protected override fun doInBackground(vararg voids: Void): Void? {
                    fileDatabase.daoAccess().deleteTask(task.getValue()!!)
                    return null
                }
            }.execute()
        }
    }

    fun deleteTask(file: FileModel) {
        object : AsyncTask<Void, Void, Void>() {
            protected override fun doInBackground(vararg voids: Void): Void? {
                fileDatabase.daoAccess().deleteTask(file)
                return null
            }
        }.execute()
    }

    fun getTask(id: Int): LiveData<FileModel> {
        return fileDatabase.daoAccess().getTask(id)
    }
}