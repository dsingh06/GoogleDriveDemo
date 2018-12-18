package com.thatapp.checklists.ModelClasses

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.os.AsyncTask
import android.util.Log

class FileRepository(context: Context) {
    private val DB_NAME = "checklist_app"
    private val fileDatabase: FileDatabase
  /*  val tasks: LiveData<FileModel>
        get() {
            return fileDatabase.daoAccess().fetchAllTasks()
        }
*/
    init {
        fileDatabase = Room.databaseBuilder(context, FileDatabase::class.java, DB_NAME).build()
    }

    @JvmOverloads
    fun insertTask(fileName: String,
                   dirName: String, createdBy: String, createdAt: String, modifiedAt: String, driveFolderID: String, rootFolderID: String, fileId: String, syncStatus: Boolean) {
        val file = FileModel(0, fileName, dirName, createdBy, createdAt, modifiedAt, driveFolderID, rootFolderID, fileId, syncStatus)
        /* file.fileName = fileName
         file.dirName = dirName
         file.createdBy = createdBy
         file.createdAt = createdAt
         file.modifiedAt = modifiedAt
         file.syncStatus = syncStatus
         file.driveFolderID = driveFolderID
         file.driveRootFolderID = rootFolderID
         file.driveFileID*/
        insertTask(file)
    }

    fun insertTask(file: FileModel) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg voids: Void): Void? {
                fileDatabase.daoAccess().insert(file)
                Log.e("insert", "iiin")
                return null
            }
        }.execute()
    }

    fun updateTask(file: FileModel) {
        file.modifiedAt = "1121"
        object : AsyncTask<Void, Void, Void>() {
            protected override fun doInBackground(vararg voids: Void): Void? {
                //  fileDatabase.daoAccess().update(file)
                return null
            }
        }.execute()
    }

    /*  fun deleteTask(id: Int) {
          val task = getTask(id)
          if (task != null) {
              object : AsyncTask<Void, Void, Void>() {
                  protected override fun doInBackground(vararg voids: Void): Void? {
                  //    fileDatabase.daoAccess().deleteTask(task.getValue()!!)
                      return null
                  }
              }.execute()
          }
      }
  */

    fun deleteTask(file: FileModel) {
        object : AsyncTask<Void, Void, Void>() {
            protected override fun doInBackground(vararg voids: Void): Void? {
                // fileDatabase.daoAccess().deleteTask(file)
                return null
            }
        }.execute()
    }

    /*
        fun getTask(id: Int): LiveData<FileModel> {
            return fileDatabase.daoAccess().findByName(id)
        }

    */
    fun getAll(): LiveData<FileModel> {
        return fileDatabase.daoAccess().getAll()
    }
}