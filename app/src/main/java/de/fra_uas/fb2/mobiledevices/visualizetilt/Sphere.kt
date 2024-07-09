package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class Sphere(val id: Int, val centerX: Float, val centerY: Float, val centerZ: Float, val radius: Float, val image: ByteArray) {

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        attribute vec4 vPosition;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
        }
    """.trimIndent()

    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """.trimIndent()

    private var program: Int = 0
    private var positionHandle: Int = 0
    private var mvpMatrixHandle: Int = 0
    private var colorHandle: Int = 0

    private val vertexBuffer: FloatBuffer

    private val slices = 20
    private val stacks = 20
    private val vertexCount: Int

    init {
        val coords = mutableListOf<Float>()

        // Calculate sphere vertices
        for (i in 0..stacks) {
            val theta1 = i * Math.PI / stacks
            val sinTheta1 = sin(theta1)
            val cosTheta1 = cos(theta1)
            val sinTheta2 = sin(theta1 + Math.PI / stacks)
            val cosTheta2 = cos(theta1 + Math.PI / stacks)

            for (j in 0..slices) {
                val phi = j * 2 * Math.PI / slices
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x1 = cosPhi * sinTheta1
                val y1 = cosTheta1
                val z1 = sinPhi * sinTheta1

                val x2 = cosPhi * sinTheta2
                val y2 = cosTheta2
                val z2 = sinPhi * sinTheta2

                coords.addAll(listOf(
                    x1.toFloat(), y1.toFloat(), z1.toFloat(),
                    x2.toFloat(), y2.toFloat(), z2.toFloat()
                ))
            }
        }

        vertexCount = coords.size / COORDS_PER_VERTEX

        val bb = ByteBuffer.allocateDirect(coords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(coords.toFloatArray())
        vertexBuffer.position(0)

        val vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "vPosition").also {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(it, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        }

        colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        GLES20.glUniform4f(colorHandle, 1.0f, 0.0f, 0.0f, 1.0f) // Red color for sphere

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount)

        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    fun isTouched(rayOrigin: Vec3, rayDir: Vec3): Boolean {
        val sphereCenter = Vec3(centerX, centerY, centerZ)
        val oc = rayOrigin - sphereCenter

        val a = rayDir.dot(rayDir)
        val b = 2.0f * oc.dot(rayDir)
        val c = oc.dot(oc) - radius * radius

        val discriminant = b * b - 4 * a * c
        return discriminant > 0
    }

    companion object {
        private const val COORDS_PER_VERTEX = 3
        private const val vertexStride = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
    }
}