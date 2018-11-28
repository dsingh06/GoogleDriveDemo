package com.thatapp.checklists.ModelClasses

import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.downloaded_item.view.*


class DisplayChecklistAdapter(var downloaded: List<String>, var context: Context) : RecyclerView.Adapter<DisplayChecklistAdapter.UserViewHolder>() {

    override fun getItemCount() = downloaded.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.downloaded_item, parent, false)
        return UserViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {


        holder.fileName.setText(downloaded[position])
        holder.parentView.setOnClickListener{
            val intent = Intent(context, DisplayQuestionsActivity::class.java)
            intent.putExtra("fileName",downloaded[position])
                    context.startActivity(intent)
            }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var imageView: ImageView = view.ivFileImage
        var fileName: TextView = view.tvFileName
        val parentView: View
        init {
            super.itemView
            parentView = itemView
        }
    }
}

