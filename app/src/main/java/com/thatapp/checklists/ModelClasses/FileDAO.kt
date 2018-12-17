package com.thatapp.checklists.ModelClasses

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface DaoAccess {
    @Insert
    fun insertTask(note: FileModel): Long

    @Query("SELECT * FROM files ORDER BY created_at desc")
    fun fetchAllTasks(): LiveData<List<FileModel>>

    @Query("SELECT * FROM files WHERE id =:taskId")
    fun getTask(taskId: Int): LiveData<FileModel>

    @Update
    fun updateTask(note: FileModel)

    @Delete
    fun deleteTask(note: FileModel)
}