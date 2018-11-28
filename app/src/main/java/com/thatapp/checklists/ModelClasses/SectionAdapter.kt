package com.thatapp.checklists.ModelClasses

import android.widget.Toast
import android.support.v7.widget.LinearLayoutManager

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.thatapp.checklists.R


class SectionAdapter(private val context: Context, private val sectionModelArrayList: ArrayList<SectionModel>) : RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.section_row_layout, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val sectionModel = sectionModelArrayList[position]
        holder.sectionLabel.text = sectionModel.sectionLabel

        //recycler view for items
        holder.itemRecyclerView.setHasFixedSize(true)
        holder.itemRecyclerView.isNestedScrollingEnabled = false

        val linearLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        holder.itemRecyclerView.layoutManager = linearLayoutManager

        val adapter = ItemAdapter(context, sectionModel.itemArrayList)
        holder.itemRecyclerView.adapter = adapter

        //show toast on click of show all button
        holder.showAllButton.setOnClickListener { Toast.makeText(context, "You clicked on Show All of : " + sectionModel.sectionLabel, Toast.LENGTH_SHORT).show() }

    }

    override fun getItemCount(): Int {
        return sectionModelArrayList.size
    }

    class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionLabel: TextView
        val showAllButton: TextView
        val itemRecyclerView: RecyclerView

        init {
            sectionLabel = itemView.findViewById<View>(R.id.section_label) as TextView
            showAllButton = itemView.findViewById<View>(R.id.section_show_all_button) as TextView
            itemRecyclerView = itemView.findViewById<View>(R.id.item_recycler_view) as RecyclerView
        }
    }

}