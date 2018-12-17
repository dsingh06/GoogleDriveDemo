package com.thatapp.checklists.ModelClasses

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import java.sql.Date

@Entity(tableName = "files")
 data class FileModel  (  @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var fileName: String = "",
    var dirName: String = "",
    var driveFolderID: String = "",
    var driveRootFolderID: String = "",
    var driveFileID: String = "",
    var createdBy: String = "",
    @ColumnInfo(name = "created_at")
    var createdAt: String = "",
    @ColumnInfo(name = "modified_at")
    var modifiedAt: String = ""
)