package com.thatapp.checklist.ViewClasses

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.thatapp.checklist.R
import kotlinx.android.synthetic.main.activity_instructions.*
import java.io.File
import android.support.v4.content.FileProvider
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class InstructionsActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_instructions)

		share.setOnClickListener {
			val inputStream: InputStream
			val fileOutputStream: FileOutputStream
			try {
				inputStream = resources.openRawResource(R.raw.checklist)
				val output = File(filesDir.absolutePath + File.separator + "Template")
//				val output = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + "Template")
				if (!output.exists()) output.mkdirs()
				fileOutputStream = FileOutputStream(
						File(output, "checklist.xls"))

				val buffer = ByteArray(1024)
				var length = inputStream.read(buffer)
				while (length > 0) {
					fileOutputStream.write(buffer, 0, length)
					length = inputStream.read(buffer)
				}
				inputStream.close()
				fileOutputStream.close()
			} catch (e: IOException) {
				Toast.makeText(this,"Write error - Unable to save template to file system folder!",Toast.LENGTH_SHORT).show()
				Crashlytics.logException(e)
			} catch (e: Exception){
				Toast.makeText(this,"Unknown error - Unable to save template to file system folder!",Toast.LENGTH_SHORT).show()
				Crashlytics.logException(e)

			}

			val requestFile = File(filesDir.absolutePath+File.separator+"Template","checklist.xls")
			val fileUri
//			if(Build.VERSION_CODES.N<=android.os.Build.VERSION.SDK_INT){
				 = FileProvider.getUriForFile(this,
						"com.thatapp.checklist.provider",
						requestFile)
//			} else{
//				fileUri = Uri.fromFile(requestFile)
//			}

			val intent = Intent(Intent.ACTION_SEND)
			intent.putExtra(Intent.EXTRA_STREAM, fileUri)
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			intent.type = "application/vnd.ms-excel"
			startActivity(Intent.createChooser(intent, "Share via...."))
		}

		appVersion.setText("Version: " +this.getPackageManager()
			.getPackageInfo(this.getPackageName(), 0).versionName)
	}
}