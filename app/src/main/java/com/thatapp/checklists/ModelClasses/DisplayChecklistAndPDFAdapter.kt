package com.thatapp.checklists.ModelClasses

import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.view.menu.MenuPopupHelper
import android.util.Log
import android.view.*
import android.widget.*
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.checklist_layout.view.*
import okio.Okio
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList


class DisplayChecklistAndPDFAdapter(var downloaded: ArrayList<File>, var context: Context, val type: String) : RecyclerView.Adapter<DisplayChecklistAndPDFAdapter.UserViewHolder>() {

    private val TAG = "MyDisplayAdapter"

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


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

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

        } else {
            holder.fileName.text = downloaded[position].name.substringBefore(".")
            holder.updateDateTime.text = "Updated: ".plus(convertLongToTime(downloaded[position].lastModified()))
        }


        if (type.equals("xls")) {

            holder.parentView.setOnClickListener {
                     val intent = Intent(context, DisplayQuestionsActivity::class.java)
                     intent.putExtra("fileName", downloaded[position].name)
                     context.startActivity(intent)

/*
                val popup = PopupMenu(context, holder.parentView)
                popup.inflate(R.menu.menu_popup)
                popup.gravity = Gravity.CENTER
                popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {

                    override fun onMenuItemClick(item: MenuItem): Boolean {
                        when (item.itemId) {
                            R.id.action_showPdf -> {
                                val intent = Intent(context, DisplayQuestionsActivity::class.java)
                                intent.putExtra("fileName", downloaded[position].name)
                                context.startActivity(intent)
                                return true
                            }

                            R.id.action_share -> {
                                //handle menu2 click
                                return true
                            }
                            R.id.action_delete -> {
                                //handle menu3 click
                                return true
                            }
                            else -> return false
                        }
                    }
                })
                popup.show()
*/
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
        var viewForeground = view.view_foreground
        var viewBackground = view.view_background
        var parentView: View

        init {
            super.itemView
            parentView = itemView
        }
    }

    fun removeItem(position: Int, dir: String) {
//        remove(position:downloaded)
        // download!!.removeAt(position)
        var fileName = downloaded[position].name

        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
//        Log.e("file", fileName)
        lateinit var dir1: File
        if (dir.equals("checklist")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "downloads")
            Log.e("type","checklist")
        } else  if (dir.equals("report")) {
            dir1 = File(context.filesDir.absolutePath + File.separator + "generated")
            Log.e("type","report")
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

//        notifyDataSetChanged()
    }

    fun restoreItem(item: File, position: Int, dir: String) {

        try {

            lateinit var dir1: File
            if (dir.equals("checklist")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "downloads")
                Log.e("type","checklist")
            } else  if (dir.equals("report")) {
                dir1 = File(context.filesDir.absolutePath + File.separator + "generated")
                Log.e("type","generated")
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

