# ScaleGestureLayout
Layout class with gesture zoom support

### Download

### Usage

Add a ScaleGestureLayout view with a child (only one) to the activity's layout:
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <bk.scalegesturelayout.ScaleGestureLayout
        android:id="@+id/scaleLayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Hello World!" />
    <bk.scalegesturelayout.ScaleGestureLayout>

</RelativeLayout>
```

You can customize the ScaleGestureLayout using the properties:
```xml
        <-- If true, the size of the child element is adjusted to the size of the layout, taking into account the scaling factor -->
        app:fitTargetSize="true"
        <-- Maximum allowable magnification factor -->
        app:maxZoom="10" 
        <-- Minimum allowable reduction factor -->
        app:minZoom="0.1" 
```

Set scale factor programmatically and hang a scale event listener:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sl = findViewById<ScaleGestureLayout>(R.id.scaleLayout)
        // set scale factor
        sl.scaleFactor = 2f
        // listen to zoom event
        sl.setOnScaleGestureListener(object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                TODO("Not yet implemented")
            }
        })
    }
}
```

Take a look at the [sample project](sample) for more information.


[sample]: <https://github.com/biz-konst/ScaleGestureLayout/tree/master/sample>
