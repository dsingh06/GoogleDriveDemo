package com.thatapp.checklists.ModelClasses

import android.app.Activity
import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.*
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.DisplayCheckListsActivity
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import com.thatapp.checklists.ViewClasses.ViewPdfActivity
import kotlinx.android.synthetic.main.checklist_layout.view.*
import okio.Okio


import java.lang.Exception
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList
import java.io.*
import java.nio.channels.FileChannel


class DisplayChecklistAndPDFAdapter(var downloaded: ArrayList<File>, var context: Context, val type: String) : RecyclerView.Adapter<DisplayChecklistAndPDFAdapter.UserViewHolder>() {

    private val TAG = "MyDisplayAdapter"
    private lateinit var cacheFile: File
    private var file_name = ""
    private lateinit var prefManager: PrefManager
    private lateinit var mPrivateRootDir: File

    override fun getItemCount() = downloaded.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView =
                if (type.equals("xls")) {
                    LayoutInflater.from(context).inflate(R.layout.checklist_layout, parent, false)
                } else {
                    LayoutInflater.from(context).inflate(R.layout.report_layout, parent, false)
                }
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        // lateinit var requestFile: File

        prefManager = PrefManager(context)
        var mResultIntent: Intent = Intent(".ACTION_RETURN_FILE")
        // Get the files/ subdirectory of internal storage
        mPrivateRootDir = context.filesDir
        if (downloaded[position].name.contains(".pdf")) {
            val name: String = downloaded[position].name.substringBefore(":")
            Log.e(TAG, name)
            holder.fileName.text = name

            val timeCreated = downloaded[position].name
                    .substringAfter(":")
                    .substringBefore(".")
                    .split("_")
            holder.updateDateTime.text = "Created: ".plus(timeCreated[1]).plus("   ").plus(timeCreated[0])
            Log.e(TAG, timeCreated[0] + " " + timeCreated[1])
            holder.ivShare.setVisibility(View.VISIBLE)

            holder.ivShare.setOnClickListener {

                prefManager = PrefManager(context)
                file_name = downloaded[position].name
                val requestFile = File(context.filesDir.absolutePath + File.separator + "generated" + File.separator + prefManager.dirName + File.separator, downloaded[position].name)

                val fileUri: Uri? = try {
                    FileProvider.getUriForFile(
                            context,
                            "com.thatapp.checklists.provider",
                            requestFile)

                } catch (e: Exception) {
                    Log.e("File Selector",
                            "The selected file can't be shared: $requestFile")
                    null
                }
                try {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                    intent.type = "application/pdf"
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("inn err", e.toString())
                }
            }

            holder.parentView.setOnClickListener {
                file_name = downloaded[position].name
                val requestFile = File(context.filesDir.absolutePath + File.separator + "generated" + File.separator + prefManager.dirName + File.separator, downloaded[position].name)

                val fileUri: Uri? = try {
                    FileProvider.getUriForFile(
                            context,
                            "com.thatapp.checklists.provider",
                            requestFile)

                } catch (e: Exception) {
                    Log.e("File Selector",
                            "The selected file can't be shared: $requestFile")
                    null
                }
                try {
                    val intent = Intent(context, ViewPdfActivity::class.java)
                    intent.putExtra("fileName", downloaded[position].name)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("inn view", e.toString())
                }
            }
        } else {
            holder.fileName.text = downloaded[position].name.substringBefore(".")
            holder.updateDateTime.text = "Updated: ".plus(convertLongToTime(downloaded[position].lastModified()))
        }



        if (type.equals("xls")) {

            holder.parentView.setOnClickListener {
                val intent = Intent(context, DisplayQuestionsActivity::class.java)
                intent.putExtra("fileName", downloaded[position].name)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                (context as DisplayCheckListsActivity).startActivityForResult(intent,13)
            }
        }
    }


    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm     dd/MM/yyyy")
        return format.format(date)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.ivFileImage
        var fileName: TextView = view.tvFileName
        var updateDateTime: TextView = view.tvDateTime
        var ivShare: ImageView = view.ivShare
        var viewForeground = view.view_foreground
        var viewBackground = view.view_background
        var parentView: View

        init {
            super.itemView
            parentView = itemView
        }
    }

    fun removeItem(position: Int, dir: String) {
        /*
        For undo of delete file this method takes two parameters
        position refers to the item position in adapter
        dir refers to the corresponding file type
        getting the directory where file is to be restored
         */
        prefManager = PrefManager(context)
        var fileName = downloaded[position].name
        lateinit var dir1: File
        if (dir.equals("checklist")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "downloads" + File.separator + prefManager.dirName)
            Log.e("type", "checklist")
        } else if (dir.equals("report")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "generated" + File.separator + prefManager.dirName)
            Log.e("type", "report")
        }
        var tempFile = File(context.filesDir.absolutePath + File.separator + "trash" + File.separator + prefManager.dirName + File.separator + fileName)

        // checking if the directory exists
        if (dir1.isDirectory) {
            //get the file and copy to trash directory and delete the file form original directory
            try {
                File(dir1.absolutePath + File.separator + fileName).copyTo(tempFile, true)   //creating a copy
                File(dir1.absolutePath + File.separator + fileName).delete()   //deleting the original file
            } catch (e: Exception) {
                Log.e("err copy ", e.toString())
            }
            downloaded.removeAt(position)
            notifyItemRemoved(position)
            notifyDataSetChanged()
            Log.e("File ", "Deleted ")
        } else {
//            Log.e("children", "not found")
        }
    }

    fun restoreItem(item: File, position: Int, dir: String) {
        /*
        For restoring the deleted file this method takes two parameters
        position refers to the item position in adapter
        dir refers to the corresponding file type
        getting the directory where file is to be restored
        */

        try {
            prefManager = PrefManager(context)
            lateinit var dir1: File
            var dirTemp = File(context.filesDir.absolutePath + File.separator + "trash" + File.separator + prefManager.dirName)
            if (dir.equals("checklist")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "downloads" + File.separator + prefManager.dirName)
//                Log.e("type", "checklist")
            } else if (dir.equals("report")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "generated" + File.separator + prefManager.dirName)
//                Log.e("type", "generated")
            }

            val tempFile = File(dir1, item.name)

            var t = dir1.mkdirs()
//            Log.e("directory created", " " + t)

            tempFile.createNewFile()

            try {
                File(dirTemp.absolutePath + File.separator + item.name).copyTo(tempFile, true)  //copy file from trash to desired directory
                File(dirTemp.absolutePath + File.separator + item.name).delete() //deleting the file from trash
            } catch (e: FileAlreadyExistsException) {
                Log.e("err temp deleted ", e.toString())
            }

            downloaded.add(position, item)

            notifyItemInserted(position)
            /* this is used to delete the any remaining file from trash*/
            val listOfFiles = dirTemp.listFiles()

            for (i in listOfFiles!!.indices) {
                if (listOfFiles[i].isFile) {
                    File(dirTemp, listOfFiles[i].name).delete()
                } else if (listOfFiles[i].isDirectory) {
                    listOfFiles[i].delete()
                }
            }
        } catch (e: Exception) {

        }
    }
}

