package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView

class CustomBackgroundIV @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr){

    val paint = Paint()
    val mPathFreehand = Path()

    val TAG = "CustomShapes"

    var canvasWidth = 0
    var canvasHeight = 0
    var drawHeight = 0f
    var drawPercent = 15f // Change this if needed

    init{
        paint.color = Color.parseColor("#303F9F")
        paint.style = Paint.Style.FILL
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.strokeWidth = 5f
        adjustViewBounds = true
        scaleType = ImageView.ScaleType.FIT_CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasWidth = w
        canvasHeight = h
        drawHeight = ((drawPercent/100)*canvasHeight)
        Log.e("MYTAG",drawHeight.toString())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPathFreehand.moveTo(0f,0f)
        mPathFreehand.lineTo(canvasWidth.toFloat(),0f)
        mPathFreehand.lineTo(canvasWidth.toFloat(),canvasHeight.toFloat()-drawHeight)
        mPathFreehand.lineTo(canvasWidth/2.toFloat(),canvasHeight.toFloat())
        mPathFreehand.lineTo(0f,canvasHeight-drawHeight)
        mPathFreehand.lineTo(0f,0f)
        canvas.drawPath(mPathFreehand, paint)
    }
}