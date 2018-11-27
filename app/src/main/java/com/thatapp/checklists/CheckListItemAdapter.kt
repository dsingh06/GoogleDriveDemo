package com.thatapp.checklists

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import android.widget.TextView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.question_list.view.*


class CheckListItemAdapter(var questionItemList: List<QuestionItem>, context: Context) : RecyclerView.Adapter<CheckListItemAdapter.UserViewHolder>() {

    private var context: Context = context


    override fun getItemCount() = questionItemList.size


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val inflatedView
                = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)
        return UserViewHolder(inflatedView)
    }


    override fun onBindViewHolder(holder: CheckListItemAdapter.UserViewHolder, position: Int) {
        holder.setIsRecyclable(false)
        val quesItem = questionItemList[position]
     /*   if(quesItem.type.equals("heading")){
            holder.layoutques.setVisibility(View.GONE)
            holder.tvHead.setVisibility(View.VISIBLE)
            holder.tvHead.setText(quesItem.strHeading)
        }
        else if(quesItem.type.equals("question")){
       */
        holder.tvQNo.setText("" + (position+1))
        holder.tvHeading.setText(quesItem.strHeading)
        holder.tvSummary.setText(quesItem.strRemark)
//        holder.btnYes.setOnClickListener(
//        holder.btnYes.setOnClickListener(View.OnClickListener())
//           holder.btnYes.setBackgroundColor(context.resources.getResourceName(R.color.colorActive))
 //       })
    }

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var tvQNo: TextView = view.tvQNo
        var tvHeading: TextView = view.tvHeading
        var tvSummary: TextView = view.tvSummary
        var tvHead:TextView = view.head
        var layoutques:LinearLayout=view.layoutLinear
        var btnYes:Button=view.btnYes
        var btnNo:Button=view.btnNo
        var btnMaybe:Button=view.btnMaybe
    }


}


