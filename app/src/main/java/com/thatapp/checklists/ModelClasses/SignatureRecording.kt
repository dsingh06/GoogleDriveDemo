package com.thatapp.checklists.ModelClasses

import android.support.v7.app.AppCompatActivity
import android.view.View

import com.thatapp.checklists.R
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.AsyncTask
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.activity_signature_recording.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import android.graphics.Bitmap
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastFailureBackground
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastSuccessBackground
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.IOException
import android.R.attr.bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log


class SignatureRecording : AppCompatActivity() {

    private val TAG = "ImageMarkupp"
    lateinit var imageView: CustomImageview
    lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature_recording)

        init()

        save.setOnClickListener {
            finish()
        }

        clear.setOnClickListener {
            imageView.removeAllDrawings()
            imageView.setColorFilter(Color.WHITE)
        }

        save.setOnClickListener {
            createImageAndSave()
            finish()
        }

        val sign = File(getFilesDir().getAbsolutePath() + File.separator + "downloads" + File.separator + "signature.png")
        if (sign.exists()) {
            val bmp = BitmapFactory.decodeFile(sign.toString())
            imageView.setImageBitmap(bmp)
        }
    }


    private fun createImageAndSave() {

        val bmp = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val drawable = imageView.background
        if (drawable != null)
        //has background drawable, then draw it on the canvas
            drawable.draw(c);
        else
        //does not have background drawable, then draw white background on the canvas
            c.drawColor(Color.WHITE);
        // draw the view on the canvas
        imageView.draw(c)

        val destination: File = getDestinationFile()
        saveFile(bmp, destination)
    }

    private fun saveFile(bmp: Bitmap, destination: File) {
        try {
            FileOutputStream(destination).use({ out ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
            })
            val t = Toast.makeText(this, "Signature saved", Toast.LENGTH_LONG)
            val v = t.view
            v.getBackground().setColorFilter(toastSuccessBackground, PorterDuff.Mode.SRC_IN)
            t.show()
        } catch (e: IOException) {
            //e.printStackTrace()
            val t = Toast.makeText(this, "Error saving file", Toast.LENGTH_LONG)
            val v = t.view
            v.getBackground().setColorFilter(toastFailureBackground, PorterDuff.Mode.SRC_IN)
            t.show()
        }
    }


    private fun getDestinationFile(): File {
        val f = File(getFilesDir().getAbsolutePath() + File.separator + "downloads")
        if (!f.exists()) f.mkdirs()
        return (File(getFilesDir().getAbsolutePath() + File.separator + "downloads", "signature.png"))
    }


    private fun init() {

        prefManager = PrefManager(this)
        imageView = findViewById(R.id.imageView7)
    }

}
