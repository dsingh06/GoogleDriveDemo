package com.thatapp.checklists.ModelClasses

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "files")
data class FileModel(@PrimaryKey(autoGenerate = true)
                     var id: Int = 0,
                     var fileName: String = "",
                     var dirName: String = "",
                     var createdBy: String = "",
                     @ColumnInfo(name = "created_at")
                     var createdAt: String = "",
                     @ColumnInfo(name = "modified_at")
                     var modifiedAt: String = "",
                     var driveFolderID: String = "",
                     var driveRootFolderID: String = "",
                     var driveFileID: String = "",
                     var syncStatus: Boolean
)