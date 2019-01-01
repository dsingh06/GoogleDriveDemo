package com.thatapp.checklists.ModelClasses

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_profile.view.*
import java.io.File
import kotlin.math.absoluteValue


class CustomImageview @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ImageView(context, attrs, defStyleAttr){

	val paint = Paint()
	val mPathFreehand = Path()

	val paths = ArrayList<Path>()
	var startX = 0f
	var startY = 0f
	var endX = 0f
	var endY = 0f

	val TAG = "CustomShapes"

	var canvasWidth = 0
	var canvasHeight = 0

	init{
		paint.color = Color.BLACK
		paint.style = Paint.Style.STROKE
		paint.flags = Paint.ANTI_ALIAS_FLAG
		paint.strokeWidth = 5f
		adjustViewBounds = true
		scaleType = ImageView.ScaleType.FIT_CENTER
	}


	fun removeAllDrawings(){
		resetCoordinates()
		invalidate()
	}

	private fun resetCoordinates() {
		startX = 0f
		startY = 0f
		endX = 0f
		endY = 0f
		mPathFreehand.reset()
		paths.clear()
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		canvasWidth = w
		canvasHeight = h
	}

	override fun onDraw(canvas: Canvas?) {
		super.onDraw(canvas)
			if (canvas != null) {
				canvas.drawPath(mPathFreehand, paint)
				if (!paths.isEmpty()){
					for (i in paths) canvas.drawPath(i, paint)
				}
			}
	}

	override fun onTouchEvent(event: MotionEvent): Boolean {
		performClick() // does nothing
		val action: Int = event.action

		return when (action){
			MotionEvent.ACTION_DOWN -> {
				startX = event.x
				startY = event.y
				endX = startX
				endY = startY
				mPathFreehand.reset()
				mPathFreehand.moveTo(startX,startY)
				invalidate()
				true
			}
			MotionEvent.ACTION_CANCEL ->{
				true
			}
			MotionEvent.ACTION_MOVE -> {
				if (event.x>canvasWidth){
					endX = canvasWidth.toFloat()
				} else if (event.x<0f){
					endX = 0f
				} else {
					endX = event.x
				}
				if (event.y>canvasHeight){
					endY = canvasHeight.toFloat()
				} else if (event.y<0){
					endY = 0f
				} else {
					endY = event.y
				}
				mPathFreehand.lineTo(endX,endY)
				invalidate()
				true
			}
			MotionEvent.ACTION_POINTER_UP ->{
				true
			}
			MotionEvent.ACTION_UP -> {
				if (event.x>canvasWidth){
					endX = canvasWidth.toFloat()
				} else if (event.x<0f){
					endX = 0f
				} else {
					endX = event.x
				}

				if (event.y>canvasHeight){
					endY = canvasHeight.toFloat()
				} else if (event.y<0){
					endY = 0f
				} else {
					endY = event.y
				}
				paths.add(Path(mPathFreehand))
				invalidate()
				true
			}
			else -> super.onTouchEvent(event)
		}
	}

}