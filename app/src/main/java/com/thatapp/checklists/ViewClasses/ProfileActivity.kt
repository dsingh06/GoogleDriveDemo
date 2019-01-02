package com.thatapp.checklists.ViewClasses

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.media.ExifInterface
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.PopupMenu
import android.system.Os.read
import android.util.Log
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var actionBarObject: ActionBar

    private lateinit var prefManager: PrefManager

    private val SIGNATURE_CODE = 131
    private val REQUEST_IMAGE_CAPTURE = 101
    private val REQUEST_IMAGE_PICK = 102
    private val REQUEST_PERMISSIONS = 102
    lateinit var imagePath: String
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

        setLogoInImageview()
        setSignatureInImageview()
        signature.setOnClickListener {
            startActivityForResult(Intent(this, SignatureRecording::class.java), SIGNATURE_CODE)
        }

        btnSave.setOnClickListener {
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

		checkPermissions()
    }


    private fun setLogoInImageview() {
        val logo = File(getFilesDir().getAbsolutePath() + File.separator + "downloads" + File.separator + "companylogo.png")
        if (logo.exists()) {
            val bmp = BitmapFactory.decodeFile(logo.toString())
            imageView.setImageBitmap(bmp)
        }

    }


    private fun setSignatureInImageview() {
        val sign = File(getFilesDir().getAbsolutePath() + File.separator + "downloads" + File.separator + prefManager.dirName + File.separator + "signature.png")
        if (sign.exists()) {
            val bmp = BitmapFactory.decodeFile(sign.toString())
            signature.setImageBitmap(bmp)
        }

    }

    private fun showToast(backgroundColor: Int, message: String, length: Int) {
        val toast = Toast.makeText(this, message, length)
        val view = toast.getView()
        view.getBackground().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
        toast.setGravity(Gravity.BOTTOM, 0, 16)
        toast.show()
    }

    private fun menuPop(ivImage: ImageView) {
        val popup = PopupMenu(this, ivImage)
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.takechoosephoto, popup.getMenu())
        popup.setOnMenuItemClickListener { item ->
            // Setonclick Listener to the menu items
            when (item.itemId) {
                R.id.takePhoto -> { // Do below if this is clicked
                    if (checkPermissions()) {
                        sendTakePictureIntent()
                    } else {
                        showAlert(this)
                    }
                }
                R.id.choosePhoto -> {
                    if (checkPermissions()) {
                        sendIntentChoosePicture()
                    } else {
                        showAlert(this)
                    }
                }
            }
            true
        }
        popup.show() // Show the popup menu
    }

    fun showAlert(context: Context) {
        val builder: AlertDialog.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            builder = AlertDialog.Builder(context)
        }
        builder.setTitle("Permission Denied")
                .setMessage("Insufficient permissions! \nPlease enable Camera and Storage access in phone settings for this App")
                .setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    //dialog, which ->
                    // Do nothing
                })
                .setIcon(R.drawable.checklist)
                .show()
    }

    private fun sendIntentChoosePicture() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        } else {
            Toast.makeText(this, "Error performing this operation!", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        Log.e("inside", "result capture")

        if (requestCode == SIGNATURE_CODE) {
            setSignatureInImageview()
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
            Log.e("inside", "image capture   " + this.imagePath.toString())

            var imgFile = File(imagePath)
            if (imgFile.exists()) {
                Log.e("inside", "capture   " + this.imagePath.toString())

                imageView.setImageURI(Uri.fromFile(imgFile))
//                showToast(toastSuccessBackground, "Logo Saved", Toast.LENGTH_SHORT)
            }

        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK && data != null) {
            try {
                val uri: Uri = data.getData()
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri)
                    val path = saveImage(bitmap)
                    showToast(toastSuccessBackground, "Logo Saved", Toast.LENGTH_SHORT)
                    imageView.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val a = Toast.makeText(this, "Error reading file, please try a different file!", Toast.LENGTH_SHORT)
                a.setGravity(Gravity.FILL_HORIZONTAL, 0, 0)
                a.show()
            }
        }
    }

    private fun sendTakePictureIntent() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true)
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            var pictureFile: File?=null
            try {
                pictureFile = getPictureFile()
            } catch (ex: IOException) {
                Toast.makeText(this,
                        "Photo file can't be created, please try again",
                        Toast.LENGTH_SHORT).show()
                return
            }

            if (pictureFile != null) {
                val photoURI = FileProvider.getUriForFile(this,
                        "com.thatapp.checklists.provider",
                        pictureFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @Throws(IOException::class)
    private fun getPictureFile(): File {
        val pictureFile = "companylogo.png"
        val storageDir = filesDir.absolutePath + File.separator + "downloads"
        val image = File(storageDir, pictureFile)
        imagePath = image.getAbsolutePath()
        return image
    }


    fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)

        val pictureFile = "companylogo.png"
        val storageDir = filesDir.absolutePath + File.separator + "downloads"
        // have the object build the directory structure, if needed.

        try {
            val f = File(storageDir, pictureFile)
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()
//            Log.e("TAG", "Logo Saved " + f.getAbsolutePath())
            return f.getAbsolutePath()
        } catch (e1: IOException) {
            e1.printStackTrace()
        }
        return ""
    }

    private fun checkPermissions():Boolean
   {
       val currentAPIVersion = Build.VERSION.SDK_INT
       if(currentAPIVersion>=android.os.Build.VERSION_CODES.M) {
           if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
               if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) ) {
                   val alertBuilder = AlertDialog.Builder(this);
                   alertBuilder.setCancelable(true);
                   alertBuilder.setTitle("Permission necessary");
                   alertBuilder.setMessage("Camera and storage permissions are necessary to take pictures and save them !!");
                   alertBuilder.setPositiveButton(android.R.string.yes, {_,_->
					   ActivityCompat.requestPermissions(this,  arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS);
				   })
                   val alert = alertBuilder.create();
                   alert.show();
               } else {
                   ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS)
               }
               return false;
           } else {
               return true;
           }
       } else {
           return true;
       }
   }


   override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
       if (requestCode==REQUEST_PERMISSIONS) {
               if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				   true
               } else {
                   false
               }
       }
   }

	private fun getExifData(filePath:String):Matrix { //TODO image rotation based on orientation
		//      check the rotation of the image and display it properly
		val exif: ExifInterface
		val matrix = Matrix()
		try {
			exif = ExifInterface(filePath)

			val orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, 0)
			//Log.d("EXIF", "Exif: $orientation")

			if (orientation == 6) {
				matrix.postRotate(90f)
				//Log.d("EXIF", "Exif: $orientation")
			} else if (orientation == 3) {
				matrix.postRotate(180f)
				//Log.d("EXIF", "Exif: $orientation")
			} else if (orientation == 8) {
				matrix.postRotate(270f)
				//Log.d("EXIF", "Exif: $orientation")
			}
		} catch (e:Exception){
			showToast(toastFailureBackground,"Error rotating file",Toast.LENGTH_SHORT)
		}
		return matrix
	}
}
