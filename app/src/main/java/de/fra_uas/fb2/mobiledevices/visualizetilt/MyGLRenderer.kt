package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.app.Activity
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import androidx.appcompat.app.AlertDialog
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    var angleX = 0f
    var angleY = 0f

    private lateinit var mAxis: Axis
    private lateinit var mSpheres: ArrayList<Sphere>

    private lateinit var myDataDao: MyDataDao // Room DAO
    // Room 데이터베이스 인스턴스
    private lateinit var database: MyDatabase

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f) // 배경을 흰색으로 설정
        GLES20.glEnable(GLES20.GL_DEPTH_TEST) // 깊이 테스트 활성화
        mAxis = Axis()
        // 여러 개의 구를 생성하여 리스트에 추가
        mSpheres = ArrayList()

        // Room 데이터베이스 초기화
        database = (context.applicationContext as MyApplication).database
        myDataDao = database.myDataDao()

        val pointsFromDatabase = myDataDao.getAll()

        for(point in pointsFromDatabase){
            mSpheres.add(Sphere(point.id, point.x *0.1f, point.y*0.1f, point.z*0.1f, 0.02f, point.image))
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, -5f, 0f, 0f, 0f, 0f, 1f, 0f)
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)

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

        // 그래프와 각 구를 그림
        for (sphere in mSpheres) {
            // 구의 모델 변환 행렬 설정
            val mSphereModelMatrix = FloatArray(16)
            Matrix.setIdentityM(mSphereModelMatrix, 0)
            Matrix.translateM(mSphereModelMatrix, 0, sphere.centerX, sphere.centerY, sphere.centerZ)
            Matrix.scaleM(mSphereModelMatrix, 0, sphere.radius, sphere.radius, sphere.radius)

            // 회전 중심을 원점으로 이동
            Matrix.translateM(mSphereModelMatrix, 0, 0f, 0f, 0f)
            // X 축 회전
            Matrix.rotateM(mSphereModelMatrix, 0, angleX, 1f, 0f, 0f)
            // Y 축 회전
            Matrix.rotateM(mSphereModelMatrix, 0, angleY, 0f, 1f, 0f)
            // 원점으로 되돌리기
            Matrix.translateM(mSphereModelMatrix, 0, 0f, 0f, 0f)

            // MVP 행렬 계산 (회전 변환 행렬과 구의 모델 변환 행렬을 결합)
            Matrix.multiplyMM(mSphereModelMatrix, 0, mMVPMatrix, 0, mSphereModelMatrix, 0)
            // 구를 그림
            sphere.draw(mSphereModelMatrix)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
    }

    fun handleTouch(normalizedX: Float, normalizedY: Float, context: Context) {
        val invertedMVPMatrix = FloatArray(16)
        Matrix.invertM(invertedMVPMatrix, 0, mMVPMatrix, 0)

        val nearPoint = floatArrayOf(normalizedX, normalizedY, -1.0f, 1.0f)
        val farPoint = floatArrayOf(normalizedX, normalizedY, 1.0f, 1.0f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        Matrix.multiplyMV(nearPointWorld, 0, invertedMVPMatrix, 0, nearPoint, 0)
        Matrix.multiplyMV(farPointWorld, 0, invertedMVPMatrix, 0, farPoint, 0)

        // Normalize world coordinates
        nearPointWorld[0] /= nearPointWorld[3]
        nearPointWorld[1] /= nearPointWorld[3]
        nearPointWorld[2] /= nearPointWorld[3]

        farPointWorld[0] /= farPointWorld[3]
        farPointWorld[1] /= farPointWorld[3]
        farPointWorld[2] /= farPointWorld[3]

        val rayOrigin = Vec3(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val rayEnd = Vec3(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        val rayDir = rayEnd - rayOrigin

        for (sphere in mSpheres) {
            if (sphere.isTouched(rayOrigin, rayDir)) {
                // Sphere is touched, show popup
                val touchedSphere = sphere
                (context as Activity).runOnUiThread {
                    showPopup("Sphere touched!", context)
                }
            }
        }
    }

    private fun showPopup(message: String, context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Popup")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
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