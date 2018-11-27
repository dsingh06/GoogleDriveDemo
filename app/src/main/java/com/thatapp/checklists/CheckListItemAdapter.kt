package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.question_list.view.*


class CheckListItemAdapter(var questionItemList:List<QuestionItem>,var context:Context) :  RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

    override fun getItemCount() = questionItemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list,parent,false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]
		holder.serialNo.setText(quesItem.serialNo)
		if (quesItem.serialNo.c) {
				holder.headingLayout.setBackgroundColor(Color.parseColor("#BDBDBD"))
				holder.heading.setText(quesItem.strHeading)
				holder.question.setText("")
			} else {
				holder.heading.setText("")
				holder.question.setText(quesItem.strQuestion)
			}
    }

    class UserViewHolder (view: View) : RecyclerView.ViewHolder(view) {

		var serialNo: TextView = view.tvQNo
        var question: TextView = view.tvSummary
        var heading: TextView = view.tvHeading
		var headingLayout:ConstraintLayout = view.headingLayout

    }


}

