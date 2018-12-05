package com.thatapp.checklists.ViewClasses

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import com.thatapp.checklists.ModelClasses.DisplayChecklistAndPDFAdapter
import com.thatapp.checklists.R
import java.io.File
import java.io.FilenameFilter
import android.support.v7.widget.helper.ItemTouchHelper

import android.graphics.Color
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.Toast


class DisplayReportsActivity : AppCompatActivity(), RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    var downloaded: ArrayList<File> = ArrayList<File>()
    private val TAG = "Downloaded:-"
    lateinit var mAdapter: DisplayChecklistAndPDFAdapter
    lateinit var coordinatorLayout: CoordinatorLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloaded_checklists)
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar.setTitle("Generated Reports")
        setSupportActionBar(toolbar)

        loadAllGeneratedFiles()
        setAdapter()

    }

    private fun setAdapter() {
        val rv_list: RecyclerView = findViewById(R.id.rv_downloaded)
        rv_list.layoutManager = LinearLayoutManager(this)
        // Access the RecyclerView Adapter and load the data into it
        mAdapter = DisplayChecklistAndPDFAdapter(downloaded, this, "pdf")
        rv_list.adapter = mAdapter
        val itemTouchHelperCallback = RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this)
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rv_list)

    }

    private fun loadAllGeneratedFiles() {
        val fileNameFilter = FilenameFilter { dir, name ->
            if (name.lastIndexOf('.') > 0) {

                // get last index for '.' char
                val lastIndex = name.lastIndexOf('.')

                // get extension
                val str = name.substring(lastIndex)

                // match path name extension
                if (str == ".pdf") {
                    return@FilenameFilter true
                }
            }
            false
        }
        val storageDir = getFilesDir()
        val files:Array<File>? = File(storageDir.getAbsolutePath() + File.separator + "generated"+File.separator +"awasrishabh@gmail.com").listFiles(fileNameFilter)
        if (files!=null) {
            Log.e("Files", "Size: " + files.size)
            for (i in files) {
//            Log.e("Files", "FileName:" + files[i].name)
                downloaded.add(i)
            }
        } else{
            Toast.makeText(this,"No files to display",Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int, position: Int) {
        if (viewHolder is DisplayChecklistAndPDFAdapter.UserViewHolder) {
            // get the removed item name to display it in snack bar
            val name = downloaded.get(viewHolder.getAdapterPosition()).getName()
            // backup of removed item for undo purpose
            //val deletedItem = downloaded.get(viewHolder.getAdapterPosition())
            val deletedItem = downloaded.get(position)
            val deletedIndex = viewHolder.getAdapterPosition()
            // remove the item from recycler view
            mAdapter.removeItem(viewHolder.getAdapterPosition(),"report")
            // showing snack bar with Undo option

            val snackbar = Snackbar
                    .make(coordinatorLayout, name + " deleted !", Snackbar.LENGTH_LONG)
            snackbar.setAction("UNDO", object : View.OnClickListener {
                override fun onClick(view: View) {
                    // undo is selected, restore the deleted item
                    mAdapter.restoreItem(deletedItem, deletedIndex,"report")

                }
            })
            snackbar.setActionTextColor(Color.YELLOW)
            snackbar.show()
        }
    }
}