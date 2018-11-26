package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.question_list.view.*


class CheckListItemAdapter(var questionItemList:List<QuestionItem>,var context:Context) :  RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

//    private  var context: Context = context

    override fun getItemCount() = questionItemList.size
private  var i=1;

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list,parent,false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]
        holder.tvSNo.setText(quesItem.strHeading)
    }

    inner class UserViewHolder (view: View) : RecyclerView.ViewHolder(view) {

         var tvSNo: TextView = view.tvSummary
    }


}

