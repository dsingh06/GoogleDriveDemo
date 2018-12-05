package com.thatapp.checklists.ModelClasses

import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.*
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import com.thatapp.checklists.ViewClasses.ViewPdfActivity
import kotlinx.android.synthetic.main.checklist_layout.view.*


import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList


class DisplayChecklistAndPDFAdapter(var downloaded: ArrayList<File>, var context: Context, val type: String) : RecyclerView.Adapter<DisplayChecklistAndPDFAdapter.UserViewHolder>() {

    private val TAG = "MyDisplayAdapter"
    private lateinit  var cacheFile: File
    private var file_name = ""

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
     var  mResultIntent : Intent = Intent(".ACTION_RETURN_FILE")
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


                file_name = downloaded[position].name
                val requestFile = File(context.filesDir.absolutePath + File.separator + "generated" +File.separator +"awasrishabh@gmail.com"+ File.separator, downloaded[position].name)

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
                }catch(e:Exception) {
                    Log.e("inn err",e.toString())
                }


            }



            holder.parentView.setOnClickListener {
                file_name = downloaded[position].name
                val requestFile = File(context.filesDir.absolutePath + File.separator + "generated" +File.separator +"awasrishabh@gmail.com"+ File.separator, downloaded[position].name)

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

                    /*                   val intent = Intent(android.content.Intent.ACTION_VIEW)
                                       intent.setDataAndType(fileUri, "application/pdf")
                                       intent.putExtra(Intent.EXTRA_STREAM, fileUri)
                                       intent.putExtra(Intent.FLAG_ACTIVITY_CLEAR_TOP,true)*/
                    context.startActivity(intent)

                }catch(e:Exception) {
                    Log.e("inn view",e.toString())
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
                context.startActivity(intent)
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
        var fileName = downloaded[position].name
        lateinit var dir1: File
        if (dir.equals("checklist")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "downloads"+File.separator +"awasrishabh@gmail.com")
            Log.e("type", "checklist")
        } else if (dir.equals("report")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "generated"+File.separator +"awasrishabh@gmail.com")
            Log.e("type", "report")
        }

        if (dir1.isDirectory) {
            val children = dir1.list()
            Log.e("children", "" + children.size)

            val listOfFiles = dir1.listFiles()

            for (i in listOfFiles!!.indices) {
                if (listOfFiles[i].isFile) {
                    Log.e("File ", listOfFiles[i].name)
                    Log.e("file to del", fileName)
                    if (fileName.equals(listOfFiles[i].name)) {
                        File(dir1, listOfFiles[i].name).delete()
                        downloaded.removeAt(position)
                        notifyItemRemoved(position)
                        Log.e("File ", "Deleted ")
                    }

                } else if (listOfFiles[i].isDirectory) {
                    Log.e("Directory ", listOfFiles[i].name)
                }
            }

        } else {
            Log.e("children", "not found")

        }


    }

    fun restoreItem(item: File, position: Int, dir: String) {

        try {

            lateinit var dir1: File
            if (dir.equals("checklist")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "downloads"+File.separator +"awasrishabh@gmail.com")
                Log.e("type", "checklist")
            } else if (dir.equals("report")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "generated"+File.separator +"awasrishabh@gmail.com")
                Log.e("type", "generated")
            }

            val tempFile = File(dir1, item.name)

            var t = dir1.mkdirs()
            Log.e("directory created", " " + t)

            tempFile.createNewFile()

            downloaded.add(position, item)

            notifyItemInserted(position)
//        notifyDataSetChanged()
        } catch (e: Exception) {

        }
    }

/*    private var mContext: Context? = null
    private var download: MutableList<File>? = null


    init {
        this.mContext = context
        this.download = ArrayList<File>(downloaded)

    }
*/



}

