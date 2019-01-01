package com.thatapp.checklists.ViewClasses

import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.AppCompatButton
import android.view.View
import android.widget.Toast
import com.thatapp.checklists.ModelClasses.PrefManager
import com.thatapp.checklists.ModelClasses.SignatureRecording
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File


class ProfileActivity : AppCompatActivity() {

    private lateinit var actionBarObject: ActionBar

    // private lateinit var etName,etJobTitle,etCompanyName
    private lateinit var prefManager: PrefManager

    private val SIGNATURE_CODE = 131

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

		setSignatureInImageview()
		signature.setOnClickListener {
            startActivityForResult(Intent(this,SignatureRecording::class.java),SIGNATURE_CODE)
        }

        btnSave.setOnClickListener{
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
        }
    }

	private fun setSignatureInImageview() {
		val sign = File(getFilesDir().getAbsolutePath() + File.separator + "downloads" + File.separator +prefManager.dirName+File.separator +  "signature.png")
		if(sign.exists()){
			val bmp = BitmapFactory.decodeFile(sign.toString())
			signature.setImageBitmap(bmp)
		}

	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
		if (requestCode==SIGNATURE_CODE)setSignatureInImageview()
    }
}
