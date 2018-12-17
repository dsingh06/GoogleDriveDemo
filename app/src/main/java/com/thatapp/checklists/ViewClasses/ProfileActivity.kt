package com.thatapp.checklists.ViewClasses

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.AppCompatButton
import android.view.View
import android.widget.Toast
import com.thatapp.checklists.ModelClasses.PrefManager
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private lateinit var actionBarObject: ActionBar

    // private lateinit var etName,etJobTitle,etCompanyName
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        actionBarObject = supportActionBar!!
        actionBarObject.setDisplayHomeAsUpEnabled(true)
        actionBarObject.title = "My Profile"

        actionBarObject.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        prefManager = PrefManager(this)
        etyourname.setText(prefManager.userName)
        etcompanyname.setText(prefManager.companyName)
        etjobtitle.setText(prefManager.jobTitle)


        btnSave.setOnClickListener(View.OnClickListener {
            var name = etyourname.text.toString()
            var companyName = etcompanyname.text.toString()
            var jobTitle = etjobtitle.text.toString()

            if (name.length < 3) {
                Toast.makeText(applicationContext, "Please Enter Your Name", Toast.LENGTH_SHORT).show()

            } else if (jobTitle.length < 3) {
                Toast.makeText(applicationContext, "Please Enter Your Job Title", Toast.LENGTH_SHORT).show()

            } else if (companyName.length < 3) {
                Toast.makeText(applicationContext, "Please Enter Your Company Name", Toast.LENGTH_SHORT).show()

            } else {
                prefManager.userName = name
                prefManager.jobTitle = jobTitle
                prefManager.companyName = companyName
                Toast.makeText(applicationContext, "Profile Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

//
    }
}
