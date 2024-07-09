package de.fra_uas.fb2.mobiledevices.visualizetilt.tilt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class TiltView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var yOffset: Float = 0f
    private var xOffset: Float = 0f
    private var strokeWidth: Float = 0f
    private var color = Color.WHITE

    private val strokePaint = Paint()
    private val spacePaint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.WHITE)

        // 수평선 굵기 변화
        strokePaint.strokeWidth = strokeWidth

        // 수평선 그리기
        val centerY = height / 2 + yOffset
        canvas.drawLine(0f, centerY, width.toFloat(), centerY, strokePaint)

        // 수직선 그리기
        val centerX = width / 2 - xOffset
        canvas.drawLine(centerX, 0f, centerX, height.toFloat(), strokePaint)

        val halfStrokeWidth = strokeWidth/2

        // 기울기에 따라 사분면 색상 채우기
        if (xOffset < 0 && yOffset < 0) {
            // 1사분면
            spacePaint.color = color
            canvas.drawRect(centerX+halfStrokeWidth, 0f, width.toFloat(), centerY-halfStrokeWidth, spacePaint)
        } else if (xOffset > 0 && yOffset < 0) {
            // 2사분면
            spacePaint.color = Color.GREEN
            canvas.drawRect(0f, 0f, centerX-halfStrokeWidth, centerY-halfStrokeWidth, spacePaint)
        } else if (xOffset > 0 && yOffset > 0) {
            // 3사분면
            spacePaint.color = Color.BLUE
            canvas.drawRect(0f, centerY+halfStrokeWidth, centerX-halfStrokeWidth, height.toFloat(), spacePaint)
        } else if (xOffset < 0 && yOffset > 0) {
            // 4사분면
            spacePaint.color = Color.YELLOW
            canvas.drawRect(centerX+halfStrokeWidth, centerY+halfStrokeWidth, width.toFloat(), height.toFloat(), spacePaint)
        }

    }

    fun updateOffsets(x: Float, y: Float, z: Float) {
        // 가속도 값을 기반으로 오프셋 업데이트
        yOffset = y * (height/2)/10  // 값을 확대하여 더 눈에 띄게
        xOffset = x * (width/2)/10  // 값을 확대하여 더 눈에 띄게

        // 가속도계 값을 RGB 값으로 변환
        val r = ((x + 9.8) / 19.6 * 255).toInt().coerceIn(0, 255)
        val g = ((y + 9.8) / 19.6 * 255).toInt().coerceIn(0, 255)
        val b = ((z + 9.8) / 19.6 * 255).toInt().coerceIn(0, 255)

        // RGB 색상 생성
        color = Color.rgb(r, g, b)

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