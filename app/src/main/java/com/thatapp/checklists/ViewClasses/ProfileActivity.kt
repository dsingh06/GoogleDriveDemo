package com.thatapp.checklists.ViewClasses

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.thatapp.checklists.ModelClasses.PrefManager
import com.thatapp.checklists.ModelClasses.SignatureRecording
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastFailureBackground
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastSuccessBackground
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.io.IOException


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

            if (name.isBlank()) {
                showToast(toastFailureBackground, "Please Enter Your Name", Toast.LENGTH_SHORT)

            } else if (jobTitle.isBlank()) {
                showToast(toastFailureBackground, "Please Enter Your Job Title", Toast.LENGTH_SHORT)

            } else if (companyName.isBlank()) {
				showToast(toastFailureBackground, "Please Enter Your Company Name", Toast.LENGTH_SHORT)

            } else {
                prefManager.userName = name
                prefManager.jobTitle = jobTitle
                prefManager.companyName = companyName
                showToast(toastSuccessBackground, "Profile Saved", Toast.LENGTH_SHORT)
                finish()
            }
        }

		logoLayout.setOnClickListener {
			menuPop(companyImageView)
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

	private fun showToast(backgroundColor:Int, message: String, length:Int) {
		val toast = Toast.makeText(this,message,length)
		val view = toast.getView()
		view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
		toast.setGravity(Gravity.BOTTOM,0,16)
		toast.show()
	}

	private fun menuPop(ivImage: ImageView) {
		val popup = PopupMenu(this,ivImage)
		//Inflating the Popup using xml file
		popup.getMenuInflater().inflate(R.menu.takechoosephoto, popup.getMenu())
		popup.setOnMenuItemClickListener { item ->  // Setonclick Listener to the menu items
			when (item.itemId) {
				R.id.takePhoto -> { // Do below if this is clicked
					if (checkPermissions(this)){
						sendIntentTakePicture() //TODO from here downwards - I've added functions
					} else{
						showAlert(this)
					}
				}
				R.id.choosePhoto -> {
					if (checkPermissions(this)){
						sendIntentChoosePicture()
					} else{
						showAlert(this)
					}
				}
			}
			true
		}
		popup.show() // Show the popup menu
	}

	fun showAlert(context: Context){
		val builder: AlertDialog.Builder
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
		} else {
			builder = AlertDialog.Builder(context)
		}
		builder.setTitle("Permission Denied")
				.setMessage("Insufficient permissions! \nPlease enable Camera and Storage access in phone settings for this App")
				.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->//dialog, which ->
					// Do nothing
				})
				.setIcon(R.drawable.alert)
				.show()
	}

	fun checkPermissions(context:Context):Boolean { //To Check
		var bool = false
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			bool = ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
		}
		return bool
	}

	private fun sendIntentTakePicture() {
		val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		var destFile:File?=null
		if (intent.resolveActivity(getPackageManager()) != null) {
			try {
				if (checkPermissions(this)) {
					destFile = File(getFilesDir().getAbsolutePath() + File.separator + "downloads")
					if (!destFile.exists()) destFile.mkdirs()
					destFile = File(getFilesDir().getAbsolutePath() + File.separator + "downloads","companyImage.png")
				}
			} catch (c: IOException) {
				Toast.makeText(this,"ReportSettings: Error creating file, please try again!", Toast.LENGTH_SHORT).show()
			}
			if (destFile != null) {
				val photoURI: Uri
				if(Build.VERSION_CODES.N<=android.os.Build.VERSION.SDK_INT){
					photoURI = FileProvider.getUriForFile(this,
							"com.thatApp.fileprovider",
							destFile)
				} else{
					photoURI = Uri.fromFile(destFile)
				}
				intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
				startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
			}
		}
	}

	private fun sendIntentChoosePicture() {
		val intent = Intent(Intent.ACTION_GET_CONTENT)
		intent.setType("image/*")
		if (intent.resolveActivity(this.packageManager)!=null){
			startActivityForResult(intent, REQUEST_IMAGE_PICK)
		} else {
			Toast.makeText(this,"Error performing this operation!",Toast.LENGTH_SHORT).show()
		}
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
			// Load into main Image
			imagePath = photoContainer!!.absolutePath.toString().drop(0)
			loadImageAndStoreImagePath(imagePath, GlobalSiteRecords.tookPicture)
		}else if (requestCode == REQUEST_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK && data!=null) {
			try{
				val uri:Uri = data.getData()
				val imageP = GetPathFromGallery.getPath(activity!!,uri)
				imagePath = Uri.parse(imageP).toString()
				loadImageAndStoreImagePath(imagePath, GlobalSiteRecords.choosePicture)
			} catch(e:Exception){
				val a = Toast.makeText(activity,"Error reading file, please try a different file!",Toast.LENGTH_SHORT)
				a.setGravity(Gravity.FILL_HORIZONTAL,0,0)
				a.show()
			}
		}
	}

	private fun loadImageAndStoreImagePath(externalPath: String,tookOrChose:Int) {
		imagePath = compressImage(externalPath,null,activity!!)
		if (!GlobalSiteRecords.savePhotosExternally && tookOrChose == GlobalSiteRecords.tookPicture)deleteImageFile(externalPath)
		Glide.with(this)
				.load(imagePath)
				.apply(GlobalSiteRecords.options)
				.into(companyImageView)
		backgroundcompanyImageView.visibility = View.INVISIBLE
	}

}
