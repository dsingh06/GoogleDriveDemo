package com.thatapp.checklists.ViewClasses

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import com.thatapp.checklists.R

class ProfileActivity : AppCompatActivity() {

    private lateinit var actionBarObject:ActionBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        actionBarObject = supportActionBar!!
        actionBarObject.setDisplayHomeAsUpEnabled(true)
        actionBarObject.title = "My Profile"
//        actionBarObject.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    }
}
