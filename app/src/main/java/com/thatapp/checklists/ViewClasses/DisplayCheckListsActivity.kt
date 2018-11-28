package com.thatapp.checklists.ViewClasses

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.system.Os.accept
import android.util.Log
import com.thatapp.checklists.ModelClasses.DisplayChecklistAdapter
import com.thatapp.checklists.R
import java.io.File
import java.io.FileFilter
import java.io.FilenameFilter



class DisplayCheckListsActivity : AppCompatActivity() {

    val downloaded: ArrayList<File> = ArrayList()
    private val TAG = "Downloaded:-"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloaded_checklists)
        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setTitle("Checklists")
        setSupportActionBar(toolbar)

        loadAllDownloadedFiles();
		setAdapter()
	}

	private fun setAdapter() {
		val rv_list: RecyclerView = findViewById(R.id.rv_downloaded)
		rv_list.layoutManager = LinearLayoutManager(this)
		// Access the RecyclerView Adapter and load the data into it
		rv_list.adapter = DisplayChecklistAdapter(downloaded, this)
	}

	private fun loadAllDownloadedFiles() {
		val fileNameFilter = FilenameFilter { dir, name ->
			if (name.lastIndexOf('.') > 0) {

				// get last index for '.' char
				val lastIndex = name.lastIndexOf('.')

				// get extension
				val str = name.substring(lastIndex)

				// match path name extension
				if (str == ".xls") {
					return@FilenameFilter true
				}
			}
			false
		}
        val files= getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).listFiles(fileNameFilter)
        Log.e("Files", "Size: " + files!!.size)
        for (i in files) {
//            Log.e("Files", "FileName:" + files[i].name)
            downloaded.add(i)
        }
    }
}