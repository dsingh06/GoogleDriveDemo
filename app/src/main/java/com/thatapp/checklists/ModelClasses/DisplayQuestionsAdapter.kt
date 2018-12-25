package com.thatapp.checklists.ModelClasses

import android.support.v7.widget.RecyclerView
import android.widget.TextView

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.DisplayQuestionsActivity
import kotlinx.android.synthetic.main.question_list.view.*


class DisplayQuestionsAdapter(var questionItemList: List<QuestionItem>, var context: Context) : RecyclerView.Adapter<DisplayQuestionsAdapter.UserViewHolder>() {

    override fun getItemCount() = questionItemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflatedView = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)
        return UserViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val quesItem = questionItemList[position]
        if (!quesItem.strHeading.equals("")) {
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

            holder.btnYes.setOnClickListener{
				if (position>=0) (context as DisplayQuestionsActivity).hideSoftKeyboard()
				holder.btnYes.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_active_left))
				holder.btnNo.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_middle))
				holder.btnElse.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_right))

				holder.btnYes.setTextColor(Color.parseColor("#ffffff"))
				holder.btnNo.setTextColor(Color.parseColor("#303F9F"))
				holder.btnElse.setTextColor(Color.parseColor("#303F9F"))

                quesItem.answer = "Yes"
            }

            holder.btnNo.setOnClickListener{
				if (position>=0) (context as DisplayQuestionsActivity).hideSoftKeyboard()
				holder.btnYes.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_left))
				holder.btnNo.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_middle_active))
				holder.btnElse.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_right))

				holder.btnNo.setTextColor(Color.parseColor("#ffffff"))
				holder.btnYes.setTextColor(Color.parseColor("#303F9F"))
				holder.btnElse.setTextColor(Color.parseColor("#303F9F"))

				quesItem.answer = "No"
            }

            holder.btnElse.setOnClickListener{
				if (position>=0) (context as DisplayQuestionsActivity).hideSoftKeyboard()
				holder.btnYes.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_left))
				holder.btnNo.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_middle))
				holder.btnElse.setBackground(ContextCompat.getDrawable(context, R.drawable.button_border_active_right))

				holder.btnElse.setTextColor(Color.parseColor("#ffffff"))
				holder.btnNo.setTextColor(Color.parseColor("#303F9F"))
				holder.btnYes.setTextColor(Color.parseColor("#303F9F"))

				quesItem.answer = "N/A"
            }
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

