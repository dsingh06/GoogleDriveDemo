package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button


class CheckListItemAdapter(var questionItemList:List<QuestionItem>,context:Context) :  RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

    private  var context: Context = context

    override fun getItemCount() = questionItemList.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list,parent)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]
    }

    inner class UserViewHolder (view: View) : RecyclerView.ViewHolder(view) {

         var tvName: TextView? = null
         var tvAddress: TextView? = null
         var tvDate: TextView? = null
         var tvConcernedPerson: TextView? = null
         var tvMobile: TextView? = null
        internal var btnReschedule: Button? = null
        internal var btnCheckIn: Button? = null
        internal var btnCheckOut: Button? = null
         val layoutButtons: LinearLayout? = null
         val itemLayout: LinearLayout? = null
    }


}

