package com.thatapp.checklists

import android.support.v7.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.downloaded_item.view.*
import kotlinx.android.synthetic.main.question_list.view.*


class DownloadListItemAdapter(var downloaded: List<String>, var context: Context) : RecyclerView.Adapter<DownloadListItemAdapter.UserViewHolder>() {

    override fun getItemCount() = downloaded.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.downloaded_item, parent, false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: DownloadListItemAdapter.UserViewHolder, position: Int) {


        holder.fileName.setText(downloaded[position])
        holder.parentView.setOnClickListener(View.OnClickListener {
            val intent = Intent(context,MyCheckList::class.java)
            intent.putExtra("fileName",downloaded[position])
                    context.startActivity(intent)
            })

        /*      holder.serialNo.setText(quesItem.serialNo)
                  holder.btnNo.setOnClickListener(View.OnClickListener {
                      holder.btnYes.setBackgroundColor(Color.parseColor("#ffffff"))
                      holder.btnNo.setBackgroundColor(Color.parseColor("#456789"))
                      holder.btnElse.setBackgroundColor(Color.parseColor("#ffffff"))
                      quesItem.answer = "No"
                      notifyDataSetChanged()
                  })
      */

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

