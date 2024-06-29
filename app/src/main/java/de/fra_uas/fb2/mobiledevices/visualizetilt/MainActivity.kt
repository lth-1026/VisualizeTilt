package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener{

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var xValue: TextView
    private lateinit var yValue: TextView
    private lateinit var xBar: ProgressBar
    private lateinit var yBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TextView 및 ProgressBar 초기화
        xValue = findViewById(R.id.x_value)
        yValue = findViewById(R.id.y_value)
        xBar = findViewById(R.id.x_bar)
        yBar = findViewById(R.id.y_bar)

        // ProgressBar의 최대값 설정
        xBar.max = 40  // -20 to 20 m/s² (scaled to 0 to 40)
        yBar.max = 20

        // SensorManager 초기화
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // 가속도 센서가 없는 경우 예외 처리
        if (accelerometer == null) {
            xValue.text = "Accelerometer not available"
            yValue.text = "Accelerometer not available"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { accel ->
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && event.values[2] >= 0) {
            val x = event.values[0]
            val y = event.values[1]

            xValue.text = "X: ${abs(x.roundToInt())} m/s²"
            yValue.text = "Y: ${abs(y.roundToInt())} m/s²"

            // ProgressBar를 기울기에 따라 업데이트
            xBar.progress = (x + 20).roundToInt()  // -20 to 20 scaled to 0 to 40
            yBar.progress = (y + 10).roundToInt()  // -20 to 20 scaled to 0 to 40
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요시 구현
    }
}