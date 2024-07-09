package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {

    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mRotationMatrix = FloatArray(16)

    var angleX = 0f
    var angleY = 0f

    private lateinit var mAxis: Axis

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // 배경을 흰색으로 설정
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // 깊이 테스트 활성화
        mAxis = Axis()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

//        // 회전 행렬 계산
//        Matrix.setRotateM(mRotationMatrix, 0, angleX, 0f, 1f, 0f)
//        Matrix.rotateM(mRotationMatrix, 0, angleY, 1f, 0f, 0f)
//        Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0)

        // 모델 변환 행렬 초기화
        Matrix.setIdentityM(mMVPMatrix, 0)

        // 회전 중심을 원점으로 이동
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)

        // X 축 회전
        Matrix.rotateM(mMVPMatrix, 0, angleX, 1f, 0f, 0f)

        // Y 축 회전
        Matrix.rotateM(mMVPMatrix, 0, angleY, 0f, 1f, 0f)

        // 원점으로 되돌리기
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, 0f)

        mAxis.draw(mMVPMatrix)

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    companion object {
        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }
    }
}