package bk.scalegesturelayout

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.TextView
import java.lang.String.format
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sf = findViewById<TextView>(R.id.scaleFactor)
        val sl = findViewById<ScaleGestureLayout>(R.id.scaleLayout)

        sl.setOnScaleGestureListener(object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                sf.text = format(Locale.getDefault(), "%.1f%%", 100 * sl.scaleFactor)
                return true
            }
        })
    }
}