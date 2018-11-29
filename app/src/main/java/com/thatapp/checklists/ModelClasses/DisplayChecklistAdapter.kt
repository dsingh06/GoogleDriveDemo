package com.thatapp.checklists.ModelClasses

import android.content.ClipData
import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.system.Os.remove
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.checklist_layout.view.*
import org.openxmlformats.schemas.presentationml.x2006.main.STPlaceholderType.DT
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList


class DisplayChecklistAdapter(var downloaded: ArrayList<File>, var context: Context) : RecyclerView.Adapter<DisplayChecklistAdapter.UserViewHolder>() {


    override fun getItemCount() = downloaded.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.checklist_layout, parent, false)
        return UserViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        holder.fileName.setText(downloaded[position].name.substringBefore("."))

        holder.updateDateTime.setText("Updated: ".plus(convertLongToTime(downloaded[position].lastModified())))


        holder.parentView.setOnClickListener {
            val intent = Intent(context, DisplayQuestionsActivity::class.java)
            intent.putExtra("fileName", downloaded[position].name)
            context.startActivity(intent)
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
        var viewForeground: RelativeLayout = view.view_foreground
        var viewBackground: RelativeLayout = view.view_background
        var parentView: View

        init {
            super.itemView
            parentView = itemView
        }
    }

    fun removeItem(position: Int) {
//        remove(position:downloaded)
       // download!!.removeAt(position)
        downloaded.removeAt(position)
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position)
//        notifyDataSetChanged()
    }

    fun restoreItem(item: File, position: Int) {
        downloaded.add(position,item)
        // notify item added by position
        notifyItemInserted(position)
//        notifyDataSetChanged()
    }

/*    private var mContext: Context? = null
    private var download: MutableList<File>? = null


    init {
        this.mContext = context
        this.download = ArrayList<File>(downloaded)

    }
*/}

