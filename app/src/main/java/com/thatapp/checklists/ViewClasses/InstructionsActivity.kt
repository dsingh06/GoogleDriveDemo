package com.thatapp.checklists.ViewClasses

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.activity_instructions.*
import java.io.File
import android.webkit.MimeTypeMap
import android.os.Environment.getExternalStorageDirectory
import android.support.v4.content.FileProvider
import com.crashlytics.android.Crashlytics
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception


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
				Crashlytics.logException(e)
			}

			val requestFile = File(filesDir.absolutePath+File.separator+"Template","checklist.xls")
			val fileUri: Uri?
			if(Build.VERSION_CODES.N<=android.os.Build.VERSION.SDK_INT){
				fileUri = FileProvider.getUriForFile(this,
						"com.thatapp.checklists.provider",
						requestFile)
			} else{
				fileUri = Uri.fromFile(requestFile)
			}

			val intent = Intent(Intent.ACTION_SEND)
			intent.putExtra(Intent.EXTRA_STREAM, fileUri)
			intent.type = "application/vnd.ms-excel"
			startActivity(Intent.createChooser(intent, "Share via...."))
		}
	}
}