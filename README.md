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

You can customize ScaleGestureLayout with properties:
```xml
        <-- If true, the size of the child element is adjusted to the size of the layout, taking into account the scaling factor -->
        app:fitTargetSize="true"
        <-- Maximum allowable magnification factor -->
        app:maxZoom="10" 
        <-- Minimum allowable reduction factor -->
        app:minZoom="0.1" 
```

Take a look at the [sample project](sample) for more information.

### License 

```
Copyright 2022 Bizyur Konstantin
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


[sample]: <https://github.com/biz-konst/ScaleGestureLayout/tree/master/sample>
