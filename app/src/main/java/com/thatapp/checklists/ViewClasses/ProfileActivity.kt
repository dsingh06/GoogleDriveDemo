package com.thatapp.checklists.ViewClasses

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.widget.PopupMenu
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.crashlytics.android.Crashlytics
import com.thatapp.checklists.ModelClasses.PrefManager
import com.thatapp.checklists.R
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastFailureBackground
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastSuccessBackground
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileActivity : AppCompatActivity() {

    private lateinit var actionBarObject: ActionBar
    private lateinit var prefManager: PrefManager
    private val SIGNATURE_CODE = 131
    private val REQUEST_IMAGE_CAPTURE = 101
    private val REQUEST_IMAGE_PICK = 331
    private val REQUEST_PERMISSIONS = 113
    private var imagePath: String=""

    private val TAG = "ProfileData: "

    lateinit var tempStorageDir:File
    lateinit var storageDir:File

	val optionsGlide = RequestOptions()
			.diskCacheStrategy(DiskCacheStrategy.NONE)
			.skipMemoryCache(true)


override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.my_toolbar))


        tempStorageDir = File(filesDir.absolutePath + File.separator + "TempFolder")
        storageDir = File(filesDir.absolutePath + File.separator + "CompanyPhoto")
        if (!tempStorageDir.exists()) tempStorageDir.mkdirs()
        if(!storageDir.exists()) storageDir.mkdirs()


        actionBarObject = supportActionBar!!
//        actionBarObject.setDisplayHomeAsUpEnabled(true)
        actionBarObject.title = "Profile"
//        actionBarObject.setHomeAsUpIndicator(R.drawable.ic_arrow_back) // because menu is required and save is implemented

        prefManager = PrefManager(this)
        etyourname.setText(prefManager.userName)
        etcompanyname.setText(prefManager.companyName)
        etjobtitle.setText(prefManager.jobTitle)

        setLogoInImageview() // good to go

        setSignatureInImageview() // good  to go

        signature.setOnClickListener {
            startActivityForResult(Intent(this, SignatureRecording::class.java), SIGNATURE_CODE)
        }

        logoLayout.setOnClickListener { menuPop(companyImageView) }

        checkPermissions()
    }


	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.save,menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId){
			R.id.saveMenuItem -> {
				val name = etyourname.text.toString()
				val companyName = etcompanyname.text.toString()
				val jobTitle = etjobtitle.text.toString()

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
					showToast(toastSuccessBackground, "Profile Saved!", Toast.LENGTH_SHORT)
					finish()
				}
			}

		}
		return super.onOptionsItemSelected(item)
	}


    private fun setLogoInImageview() {
        val logo = File(getFilesDir().getAbsolutePath() + File.separator + "CompanyPhoto" + File.separator + "companylogo.jpg")
        if (logo.exists()) {
            Glide.with(this).load(logo).apply(optionsGlide).into(imageView)
        }
    }

    private fun setSignatureInImageview() {
        val sign = File(getFilesDir().getAbsolutePath() + File.separator + "downloads" + File.separator + "signature.png")
        if (sign.exists()) {
            Glide.with(this).load(sign).apply(optionsGlide).into(signature)
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

    private fun showAlert(context: Context) {
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

	private fun sendTakePictureIntent() {
		val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
		//    cameraIntent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION, true)
		if (cameraIntent.resolveActivity(getPackageManager()) != null) {
			try {
				val pictureName = "companylogo"
				val temp = File.createTempFile(pictureName,".jpg",tempStorageDir)
				imagePath = temp.absolutePath	// Now pointing to the temporary directory
				val photoURI: Uri
				if (temp!=null){
					if(Build.VERSION_CODES.N<=android.os.Build.VERSION.SDK_INT) {
						photoURI = FileProvider.getUriForFile(this,
								"com.thatapp.checklists.provider",
								temp);
					} else{
						photoURI = Uri.fromFile(temp)
					}
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
					startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
				}
			} catch (ex: Exception) {
				Crashlytics.logException(ex)
				Toast.makeText(this,
						"Photo file couldn't be created, please try again",
						Toast.LENGTH_SHORT).show()
				return
			}
		}
	}


	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

		val imageFinalPath = File(storageDir, "companylogo.jpg")
		val IMAGE_SIZE = 450

        if (requestCode == SIGNATURE_CODE) {
            setSignatureInImageview()
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == AppCompatActivity.RESULT_OK) {
                if (File(imagePath).exists()) { //imagepath pointing to temp image path
                    orientationCorrectionAndSaveLowerRes(imagePath,imageFinalPath, IMAGE_SIZE)
                }
            } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == AppCompatActivity.RESULT_OK && data != null) {
                try {
                    val uri: Uri? = data.getData()
                    if (uri!=null){
                        val destination:File? = downloadFileFromFilePicker(uri)
						if (destination!=null){
							orientationCorrectionAndSaveLowerRes(destination.absolutePath,imageFinalPath,IMAGE_SIZE)
						}
                    }
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                    val a = Toast.makeText(this, "Error reading file, please try a different file!", Toast.LENGTH_SHORT)
                    a.setGravity(Gravity.FILL_HORIZONTAL, 0, 0)
                    a.show()
                }
            }
    }


    private fun orientationCorrectionAndSaveLowerRes(temppath: String, newpath:File, size: Int) {
        val matrix = getExifData(temppath) // to get the rotation of the image

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(temppath, options)
        options.inSampleSize = calculateInSampleSize(options,size,size)

        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(temppath,options)

        try{
            val out = FileOutputStream(newpath)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out)
            if (File(temppath).exists()) File(temppath).delete()
            imagePath = newpath.absolutePath
        } catch (e:IOException){
            Crashlytics.logException(e)
            showToast(toastFailureBackground, "Error saving file", Toast.LENGTH_SHORT)
        }

        val properBitmap:Bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true)
		Glide.with(this).load(properBitmap).apply(optionsGlide).into(imageView)

        try{
            val out = FileOutputStream(newpath)
            properBitmap.compress(Bitmap.CompressFormat.JPEG,90,out)
        } catch (e:IOException){
            Crashlytics.logException(e)
            showToast(toastFailureBackground, "Error saving file", Toast.LENGTH_SHORT)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun getExifData(filePath: String): Matrix {
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
        } catch (e: Exception) {
            Crashlytics.logException(e)
            showToast(toastFailureBackground, "Error rotating file", Toast.LENGTH_SHORT)
        }
        return matrix
    }

	private fun downloadFileFromFilePicker(uri: Uri):File? {
		// Retrieve the metadata as a File object.
		var des:File?=null
		try {
			contentResolver.query(uri, null, null, null, null)!!.use { cursor ->
				if (cursor.moveToFirst()) {
					val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
					val fileName = cursor.getString(nameIndex)
					val inputStream = applicationContext?.contentResolver?.openInputStream(uri) //FileNotFound Exception could be thrown
					val storageDir = filesDir
					val filep = java.io.File(storageDir?.getAbsolutePath() + java.io.File.separator + "TempFolder")
					if (!filep.exists()) filep.mkdirs()
					des = java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName)

					val outputStream = FileOutputStream(des)
					val buffer = ByteArray(1024)
					var length = inputStream!!.read(buffer)
					while (length > 0) {
						outputStream.write(buffer, 0, length)
						length = inputStream.read(buffer)
					}
					outputStream.close()
					inputStream.close()
				}
			}
		} catch (e: Exception){
			Crashlytics.logException(e)
			return null
		}
		return des
	}

	private fun checkPermissions(): Boolean {
		val currentAPIVersion = Build.VERSION.SDK_INT
		if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
			if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) != PackageManager.PERMISSION_GRANTED) {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					val alertBuilder = AlertDialog.Builder(this);
					alertBuilder.setCancelable(true);
					alertBuilder.setTitle("Permission necessary");
					alertBuilder.setMessage("Camera and storage permissions are necessary to take pictures and save them !!");
					alertBuilder.setPositiveButton(android.R.string.yes, { _, _ ->
						ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSIONS);
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
		if (requestCode == REQUEST_PERMISSIONS) {
			if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
				true
			} else {
				false
			}
		}
	}

}
