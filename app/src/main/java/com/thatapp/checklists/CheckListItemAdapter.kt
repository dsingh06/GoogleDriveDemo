package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.question_list.view.*


class CheckListItemAdapter(var questionItemList: List<QuestionItem>, var context: Context) : RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

    override fun getItemCount() = questionItemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]


        if (!quesItem.serialNo.contains(".")) {
            holder.serialNo.setText(quesItem.serialNo)
            holder.headingLayout.setBackgroundColor(Color.parseColor("#BDBDBD"))
            holder.heading.setText(quesItem.strHeading)
            holder.question.setText("")
            holder.btnLayout.setVisibility(View.GONE)
        } else {
            holder.serialNo.setText("\t\t"+quesItem.serialNo)
            holder.headingLayout.setBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.heading.setText("")
            holder.question.setText(quesItem.strQuestion)

            holder.btnYes.setOnClickListener(View.OnClickListener {
                holder.btnYes.setBackgroundColor(Color.parseColor("#456789"))
                holder.btnNo.setBackgroundColor(Color.parseColor("#ffffff"))
                holder.btnElse.setBackgroundColor(Color.parseColor("#ffffff"))
                quesItem.answer = "Yes"
//                notifyDataSetChanged()

                Log.e("clicked","yes "+ position)
            })

            holder.btnNo.setOnClickListener(View.OnClickListener {
                holder.btnYes.setBackgroundColor(Color.parseColor("#ffffff"))
                holder.btnNo.setBackgroundColor(Color.parseColor("#456789"))
                holder.btnElse.setBackgroundColor(Color.parseColor("#ffffff"))
                quesItem.answer = "No"
//                notifyDataSetChanged()
                Log.e("clicked","No "+ position)
            })

            holder.btnElse.setOnClickListener(View.OnClickListener {
                holder.btnYes.setBackgroundColor(Color.parseColor("#ffffff"))
                holder.btnNo.setBackgroundColor(Color.parseColor("#ffffff"))
                holder.btnElse.setBackgroundColor(Color.parseColor("#456789"))
                quesItem.answer = "333"
//                notifyDataSetChanged()
                Log.e("clicked","333 "+ position)
            })
        }
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var serialNo: TextView = view.tvQNo
        var question: TextView = view.tvQuestion
        var heading: TextView = view.tvHeading
        var headingLayout: CardView = view.headingLayout
        var btnLayout: RelativeLayout = view.layoutButton
        var btnYes: Button = view.btnYes
        var btnNo: Button = view.btnNo
        var btnElse: Button = view.btnElse
        val parentView: View

        init {
            super.itemView
            parentView = itemView
        }

    }


}

