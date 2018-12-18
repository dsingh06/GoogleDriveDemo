package com.thatapp.checklists.ModelClasses

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface DaoAccess {

    @Insert
    fun insert(file: FileModel)


    @Query("SELECT * FROM files")
    fun getAll(): LiveData<FileModel>

    @Insert
    fun insertAll(vararg files: FileModel)

    @Delete
    fun delete(file: FileModel)

}