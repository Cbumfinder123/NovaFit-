package com.cibertec.novafit.ui

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private var mode = NONE
    private val last = PointF()
    private val start = PointF()
    private var minScale = 1f
    private var maxScale = 4f
    private var currentScale = 1f
    private var redundantXSpace = 0f
    private var redundantYSpace = 0f
    private var width = 0f
    private var height = 0f
    private var saveScale = 1f
    private var right = 0f
    private var bottom = 0f
    private var origWidth = 0f
    private var origHeight = 0f
    private var bmWidth = 0f
    private var bmHeight = 0f

    private val scaleDetector: ScaleGestureDetector

    init {
        scaleType = ScaleType.MATRIX
        imageMatrix = matrix

        scaleDetector = ScaleGestureDetector(context, ScaleListener())

        setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)

            val curr = PointF(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    last.set(event.x, event.y)
                    start.set(last)
                    mode = ZOOM
                }

                MotionEvent.ACTION_MOVE -> {
                    if (mode == DRAG) {
                        val deltaX = curr.x - last.x
                        val deltaY = curr.y - last.y
                        val scaleWidth = bmWidth * saveScale
                        val scaleHeight = bmHeight * saveScale

                        if (scaleWidth < width) {
                            matrix.postTranslate(deltaX, 0f)
                            last.set(curr.x, last.y)
                        } else if (scaleHeight < height) {
                            matrix.postTranslate(0f, deltaY)
                            last.set(last.x, curr.y)
                        } else {
                            matrix.postTranslate(deltaX, deltaY)
                            last.set(curr.x, curr.y)
                        }

                        fixTranslation()
                        imageMatrix = matrix
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                }
            }

            true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()

        val scale: Float
        val scaleX = width / drawable.intrinsicWidth
        val scaleY = height / drawable.intrinsicHeight
        scale = min(scaleX, scaleY)

        matrix.setScale(scale, scale)
        imageMatrix = matrix
        saveScale = 1f

        redundantYSpace = height - (scale * drawable.intrinsicHeight)
        redundantXSpace = width - (scale * drawable.intrinsicWidth)
        redundantYSpace /= 2f
        redundantXSpace /= 2f

        matrix.postTranslate(redundantXSpace, redundantYSpace)

        origWidth = width - 2 * redundantXSpace
        origHeight = height - 2 * redundantYSpace
        right = width * saveScale - width - (2 * redundantXSpace * saveScale)
        bottom = height * saveScale - height - (2 * redundantYSpace * saveScale)
        imageMatrix = matrix
    }

    private fun fixTranslation() {
        matrix.getValues(matrixValues)
        val transX = matrixValues[Matrix.MTRANS_X]
        val transY = matrixValues[Matrix.MTRANS_Y]

        val fixTransX = getFixTranslation(transX, width, origWidth * saveScale)
        val fixTransY = getFixTranslation(transY, height, origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            matrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans) {
            return -trans + minTrans
        }
        if (trans > maxTrans) {
            return -trans + maxTrans
        }
        return 0f
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val prevScale = saveScale
            saveScale *= mScaleFactor

            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / prevScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / prevScale
            }

            right = width * saveScale - width - (2 * redundantXSpace * saveScale)
            bottom = height * saveScale - height - (2 * redundantYSpace * saveScale)

            if (origWidth * saveScale <= width || origHeight * saveScale <= height) {
                matrix.postScale(mScaleFactor, mScaleFactor, width / 2, height / 2)
                if (mScaleFactor < 1) {
                    matrix.getValues(matrixValues)
                    val x = matrixValues[Matrix.MTRANS_X]
                    val y = matrixValues[Matrix.MTRANS_Y]
                    if (mScaleFactor < 1) {
                        if (round(origWidth * saveScale) < width) {
                            if (y < -bottom)
                                matrix.postTranslate(0f, -(y + bottom))
                            else if (y > 0)
                                matrix.postTranslate(0f, -y)
                        } else {
                            if (x < -right)
                                matrix.postTranslate(-(x + right), 0f)
                            else if (x > 0)
                                matrix.postTranslate(-x, 0f)
                        }
                    }
                }
            } else {
                matrix.postScale(mScaleFactor, mScaleFactor, detector.focusX, detector.focusY)
                matrix.getValues(matrixValues)
                val x = matrixValues[Matrix.MTRANS_X]
                val y = matrixValues[Matrix.MTRANS_Y]
                if (mScaleFactor < 1) {
                    if (x < -right)
                        matrix.postTranslate(-(x + right), 0f)
                    else if (x > 0)
                        matrix.postTranslate(-x, 0f)
                    if (y < -bottom)
                        matrix.postTranslate(0f, -(y + bottom))
                    else if (y > 0)
                        matrix.postTranslate(0f, -y)
                }
            }
            return true
        }
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        private val matrixValues = FloatArray(9)

        private fun round(value: Float): Float {
            return (value * 100).toInt() / 100f
        }
    }
}