package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TiltView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var zValue: Float = 0f
    private var strokeWidth: Float = 0f

    private val paint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // z값에 따라 배경 색상 변경
        val backgroundColor = when {
            zValue > 0 -> Color.rgb(255, 255 - (zValue * 10).toInt(), 255 - (zValue * 10).toInt())
            else -> Color.rgb(255 - (zValue * -10).toInt(), 255, 255 - (zValue * -10).toInt())
        }
        canvas.drawColor(backgroundColor)

        paint.strokeWidth = strokeWidth

        // 수평선 그리기
        val centerY = height / 2 + yOffset
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, paint)

        // 수직선 그리기
        val centerX = width / 2 - xOffset
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), paint)
    }

    fun updateOffsets(x: Float, y: Float, z: Float) {
        // 가속도 값을 기반으로 오프셋 업데이트
        yOffset = y * (height/2)/10  // 값을 확대하여 더 눈에 띄게
        xOffset = x * (width/2)/10  // 값을 확대하여 더 눈에 띄게
        zValue = z //z 값을 업데이트
        invalidate()  // 뷰 다시 그리기
    }

    fun updateStrokeWidth(rotation: Float) {
        // 회전 값을 기반으로 업데이트
        var width = 50f - rotation/3.6f

        if(width < 2f) {
            width = 0f
        }else if(width > 100f) {
            width = 100f
        }

        this.strokeWidth = width
        invalidate()  // 뷰 다시 그리기
    }
}