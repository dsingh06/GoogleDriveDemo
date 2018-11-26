package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.question_list.view.*


class CheckListItemAdapter(var questionItemList:List<QuestionItem>,context:Context) :  RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

    private  var context: Context = context
   private var i = 1
    override fun getItemCount() = questionItemList.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list,parent,false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]

        holder.tvSno.text = ""+i
        holder.tvHeading.text = quesItem.strHeading
        i++

    }

    inner class UserViewHolder (view: View) : RecyclerView.ViewHolder(view) {

         val tvSno: TextView = view.tvQNo
         var tvHeading: TextView = view.tvHeading
    }


}

