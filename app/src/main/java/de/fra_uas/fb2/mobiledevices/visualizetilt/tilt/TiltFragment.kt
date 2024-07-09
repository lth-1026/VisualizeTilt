package de.fra_uas.fb2.mobiledevices.visualizetilt.tilt

import android.annotation.SuppressLint
import android.content.Context.SENSOR_SERVICE
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import de.fra_uas.fb2.mobiledevices.visualizetilt.R
import de.fra_uas.fb2.mobiledevices.visualizetilt.database.MyApplication
import de.fra_uas.fb2.mobiledevices.visualizetilt.database.MyDataEntity
import de.fra_uas.fb2.mobiledevices.visualizetilt.database.MyDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class TiltFragment : Fragment(), SensorEventListener {

    private lateinit var database: MyDatabase

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var gameRotationVectorSensor: Sensor? = null
    private lateinit var customView: TiltView

    private var startAngle = 0f // 회전 시작 각도
    private var currentAngle = 0f // 현재 회전 각도

    private var latestX = 0f
    private var latestY = 0f
    private var latestZ = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tilt, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Toast.makeText(context,"If you touch the View, Picture will be saved", Toast.LENGTH_SHORT).show()

        customView = view.findViewById(R.id.customView)

        // SensorManager 초기화
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gameRotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)

        // 터치 리스너 추가
        customView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // 터치 이벤트 발생 시 화면 캡처 및 데이터 저장
                captureScreenAndSaveData()
            }
            true
        }

        // 데이터베이스 인스턴스 초기화
        database = (requireActivity().application as MyApplication).database
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
        magnetometer?.also { mag ->
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { gyro ->
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gameRotationVectorSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when(event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                latestX = x
                latestY = y
                latestZ = z
                customView.updateOffsets(x, y, z)
            }
            Sensor.TYPE_GYROSCOPE -> {
                val rotationSpeed = event.values[2] // z-rotation rate (radians/second)
                val deltaTime = 0.005 // 예시에서는 간격을 0.02초로 설정

                // 회전 각도 계산
                val deltaAngle = rotationSpeed * deltaTime // 회전 속도 * 시간 간격

                // 현재 각도 업데이트
                currentAngle += Math.toDegrees(deltaAngle).toFloat()

                val rotationFromInitial = currentAngle - startAngle
                customView.updateStrokeWidth(rotationFromInitial)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요시 구현
    }

    private fun captureScreenAndSaveData() {
        // View의 DrawingCache 활성화
        customView.isDrawingCacheEnabled = true
        customView.buildDrawingCache(true)

        // 화면 캡처
        val bitmap = Bitmap.createBitmap(customView.drawingCache)
        customView.isDrawingCacheEnabled = false

        // 데이터베이스에 저장 (x, y, z 값 및 이미지)
        saveDataToDatabase(latestX, latestY, latestZ, bitmap)
        Toast.makeText(context, "saved successfully", Toast.LENGTH_SHORT).show()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun saveDataToDatabase(x: Float, y: Float, z: Float, bitmap: Bitmap) {
        // 데이터베이스 엔티티 생성
        val data = MyDataEntity(
            x = x,
            y = y,
            z = z,
            image = bitmapToByteArray(bitmap) // Bitmap을 ByteArray로 변환
        )

        // 데이터베이스에 데이터 삽입
        // 예시: Room 데이터베이스 사용 시
        GlobalScope.launch {
            database.myDataDao().insert(data)
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }
}
