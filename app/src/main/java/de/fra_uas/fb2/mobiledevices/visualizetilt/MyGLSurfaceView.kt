package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent

class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {

    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2) // OpenGL ES 2.0 context 설정
        renderer = MyGLRenderer()
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY // 터치 이벤트가 발생할 때만 렌더링
    }

    private var previousX = 0f
    private var previousY = 0f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY

                // 반응성을 높이기 위해 x 및 y 좌표의 변경 값을 적용합니다.
                renderer.angleX += dx * 0.3f
                renderer.angleY += dy * 0.3f

                requestRender()
            }
        }

        previousX = x
        previousY = y
        return true
    }
}