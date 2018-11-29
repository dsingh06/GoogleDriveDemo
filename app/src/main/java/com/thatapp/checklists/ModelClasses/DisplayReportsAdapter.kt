//package com.thatapp.checklists.ModelClasses
//
//import android.support.v7.widget.RecyclerView
//
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
//import com.thatapp.checklists.R
//import kotlinx.android.synthetic.main.checklist_layout.view.*
//import java.io.File
//
//import kotlin.collections.ArrayList
//
//
//class DisplayReportsAdapter(var downloaded: ArrayList<File>, var context: Context) : RecyclerView.Adapter<DisplayReportsAdapter.UserViewHolder>() {
//
//	val TAG = "MyLogsDisplayReport"
//    override fun getItemCount() = downloaded.size
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        val inflatedView = LayoutInflater.from(context).inflate(R.layout.checklist_layout, parent, false)
//        return UserViewHolder(inflatedView)
//    }
//
//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//
//        val name:String = downloaded[position].name.substringBefore(":")
//		Log.e(TAG, name)
//        holder.fileName.setText(name)
//
//        val timeCreated = downloaded[position].name
//				.substringAfter(":")
//				//.substringBefore(".")
//				//.split("_")
//        holder.updateDateTime.setText("Created: ".plus(timeCreated[0]).plus(timeCreated[1]))
//		Log.e(TAG, timeCreated[0] +" "+ timeCreated[1])
//
//       holder.parentView.setOnClickListener {
//            val intent = Intent(context, DisplayQuestionsActivity::class.java)
//            intent.putExtra("fileName", downloaded[position].name)
//            context.startActivity(intent)
//        }
//    }
//
//    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        var imageView: ImageView = view.ivFileImage
//        var fileName: TextView = view.tvFileName
//           var updateDateTime: TextView = view.tvDateTime
//        var viewForeground = view.view_foreground
//        var viewBackground = view.view_background
//        var parentView: View
//
//        init {
//            super.itemView
//            parentView = itemView
//        }
//    }
//
//    fun removeItem(position: Int) {
////        remove(position:downloaded)
//       // download!!.removeAt(position)
//        downloaded.removeAt(position)
//        // notify the item removed by position
//        // to perform recycler view delete animations
//        // NOTE: don't call notifyDataSetChanged()
//        notifyItemRemoved(position)
////        notifyDataSetChanged()
//    }
//
//    fun restoreItem(item: File, position: Int) {
//        downloaded.add(position,item)
//        // notify item added by position
//        notifyItemInserted(position)
////        notifyDataSetChanged()
//    }
//
///*    private var mContext: Context? = null
//    private var download: MutableList<File>? = null
//
//
//    init {
//        this.mContext = context
//        this.download = ArrayList<File>(downloaded)
//
//    }
//*/}
//
