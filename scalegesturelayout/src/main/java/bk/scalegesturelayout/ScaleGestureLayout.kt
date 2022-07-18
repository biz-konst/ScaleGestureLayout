@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package bk.scalegesturelayout

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import androidx.core.view.children
import kotlin.math.roundToInt

/**
 * @author Bizyur Konstantin <bkonst2180@gmail.com>
 * @since 01.02.2022
 *
 * Layout class with gesture zoom support.
 * The first visible child element will be scaled according to the scaleFactor attribute.
 * You can add one child element to the layout and control its position by assigning
 * gravity with the app:gravity attribute.
 */
open class ScaleGestureLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val DEFAULT_TARGET_GRAVITY = Gravity.START and Gravity.TOP
        const val DEFAULT_MIN_ZOOM = 0.5f
        const val DEFAULT_MAX_ZOOM = 2f
    }

    /**
     * Minimum allowable scaling factor
     */
    var minZoom = DEFAULT_MIN_ZOOM
        set(value) {
            check(factorIsValid(value)) { "Min zoom value is invalid" }
            field = value
            scaleFactor = scaleFactor.coerceAtLeast(value)
        }

    /**
     * Maximum allowable scaling factor
     */
    var maxZoom = DEFAULT_MAX_ZOOM
        set(value) {
            check(factorIsValid(value)) { "Max zoom value is invalid" }
            field = value
            scaleFactor = scaleFactor.coerceAtMost(value)
        }

    var scaleFactor = 1f
        set(value) {
            check(factorIsValid(value)) { "Scale factor is invalid" }

            val newValue = value.coerceIn(minZoom, maxZoom)
            if (field == newValue) {
                return
            }

            field = newValue
            applyScaleFactor()
        }

    /**
     * Flag to fit the target size to the given scaling factor to properly fill the layout
     */
    var fitTargetSize = true
        set(value) {
            field = value
            if (childCount > 0) {
                invalidate()
            }
        }

    var gravity = DEFAULT_TARGET_GRAVITY
        set(value) {
            var newValue = value
            if (newValue and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK == 0) {
                newValue = newValue or Gravity.START
            }
            if (newValue and Gravity.VERTICAL_GRAVITY_MASK == 0) {
                newValue = newValue or Gravity.TOP
            }
            if (field == newValue) {
                return
            }

            field = newValue
            if (childCount > 0) {
                invalidate()
            }
        }

    init {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.ScaleGestureLayout, defStyleAttr, defStyleRes
        )
        isEnabled = a.getBoolean(R.styleable.ScaleGestureLayout_android_enabled, true)
        fitTargetSize = a.getBoolean(R.styleable.ScaleGestureLayout_fitTargetSize, true)
        minZoom = a.getFloat(R.styleable.ScaleGestureLayout_minZoom, DEFAULT_MIN_ZOOM)
        maxZoom = a.getFloat(R.styleable.ScaleGestureLayout_maxZoom, DEFAULT_MAX_ZOOM)
        gravity = a.getInt(R.styleable.ScaleGestureLayout_gravity, DEFAULT_TARGET_GRAVITY)
        a.recycle()
    }

    private var target: View? = null

    private var onScaleGestureListener: ScaleGestureDetector.OnScaleGestureListener? = null

    private val scaleGestureListener =
        object : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
                return onScaleGestureListener?.onScaleBegin(detector) ?: true
            }

            override fun onScale(detector: ScaleGestureDetector?): Boolean {
                detector ?: return false
                var result = false
                target ?: ensureTarget()
                target?.let {
                    scaleFactor *= detector.scaleFactor
                    result = true
                }
                return onScaleGestureListener?.onScale(detector) == true || result
            }

            override fun onScaleEnd(detector: ScaleGestureDetector?) {
                onScaleGestureListener?.onScaleEnd(detector)
            }
        }

    private val scaleGestureDetector = ScaleGestureDetector(context, scaleGestureListener)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isEnabled || childCount == 0) {
            return false
        }
        if (!scaleGestureDetector.isInProgress) {
            scaleGestureDetector.onTouchEvent(ev)
        }
        if (scaleGestureDetector.isInProgress) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return scaleGestureDetector.isInProgress
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (childCount == 0) {
            return
        }

        target ?: ensureTarget()
        target?.let { child ->
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight

            val scaledWidth = (childWidth * child.scaleX).roundToInt()
            val scaledHeight = (childHeight * child.scaleY).roundToInt()

            val absGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
            val childLeft = when (absGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL ->
                    paddingLeft + (right - left - paddingLeft - paddingRight - scaledWidth) / 2
                Gravity.RIGHT ->
                    right - left - paddingRight - scaledWidth
                else -> paddingLeft
            }

            val childTop = when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.CENTER_VERTICAL ->
                    paddingTop + (bottom - top - paddingTop - paddingBottom - scaledHeight) / 2
                Gravity.BOTTOM ->
                    bottom - top - paddingBottom - scaledHeight
                else -> paddingTop
            }

            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var maxWidth = suggestedMinimumWidth
        var maxHeight = suggestedMinimumHeight
        var childState = 0

        target ?: ensureTarget()
        target?.let { child ->
            val hPadding = paddingLeft + paddingRight
            val vPadding = paddingTop + paddingBottom

            val lp = child.layoutParams
            child.measure(
                getChildMeasureSpec2(widthMeasureSpec, hPadding, lp.width, child.scaleX),
                getChildMeasureSpec2(heightMeasureSpec, vPadding, lp.height, child.scaleY)
            )

            maxWidth = maxWidth.coerceAtLeast(
                getMeasureSize(widthMeasureSpec, hPadding, child.measuredWidth, child.scaleX)
            )
            maxHeight = maxHeight.coerceAtLeast(
                getMeasureSize(heightMeasureSpec, vPadding, child.measuredHeight, child.scaleY)
            )
            childState = combineMeasuredStates(childState, child.measuredState)
        }

        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(
                maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT
            )
        )
    }

    override fun addView(child: View?, index: Int, params: LayoutParams?) {
        if (childCount > 0) {
            throw IllegalStateException("ScaleGestureLayout can host only one direct child")
        }

        super.addView(child, index, params)
        applyScaleFactor()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled || childCount == 0) {
            return false
        }
        return scaleGestureDetector.onTouchEvent(event)
    }

    override fun shouldDelayChildPressedState() = true

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is ScaleSavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        fitTargetSize = state.fitTargetSize
        maxZoom = state.maxZoom
        minZoom = state.minZoom
        gravity = state.gravity
        scaleFactor = state.scaleFactor
    }

    override fun onSaveInstanceState(): Parcelable {
        return ScaleSavedState(super.onSaveInstanceState()).also {
            it.fitTargetSize = fitTargetSize
            it.minZoom = minZoom
            it.maxZoom = maxZoom
            it.gravity = gravity
            it.scaleFactor = scaleFactor
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            scaleFactor = 1f
        }
    }

    /**
     * Set event listener from scale gesture
     *
     * @param listener Scale gesture event listener
     */
    fun setOnScaleGestureListener(listener: ScaleGestureDetector.OnScaleGestureListener?) {
        onScaleGestureListener = listener
    }

    /**
     * Apply scale factor to child element
     */
    private fun applyScaleFactor() {
        target ?: ensureTarget()
        target?.let {
            it.pivotX = 0f
            it.pivotY = 0f
            layoutParams?.let { lp ->
                it.scaleX =
                    fitScaleFactor(scaleFactor, lp.width, width - paddingLeft - paddingRight)
                it.scaleY =
                    fitScaleFactor(scaleFactor, lp.height, height - paddingTop - paddingBottom)
            }
            it.requestLayout()
        }
    }

    /**
     * Set first visible child as target
     */
    protected fun ensureTarget() {
        if (target?.parent != this) {
            target = null
        }
        if (target == null && childCount > 0) {
            target = children.firstOrNull { it.visibility != GONE }
        }
    }

    /**
     * Validate the set factor
     *
     * @param factor checked factor
     * @return true if factor is valid
     */
    protected fun factorIsValid(factor: Float) = !factor.isNaN() && !factor.isInfinite()

    /**
     * Get the minimum scale factor at which the child element will
     * most accurately fit into the dimension
     *
     * @param newFactor initial scale factor
     * @param mode layout dimension
     * @param size the size at which the child fits
     * @return minimum scale factor
     */
    private fun fitScaleFactor(newFactor: Float, mode: Int, size: Int): Float {
        return if (mode == LayoutParams.WRAP_CONTENT) newFactor
        else size.toFloat() / (size / newFactor).roundToInt()
    }

    /**
     * Get the measurement specification of the child element, given the scale factor,
     * the fitChildSize flag, and measurement mode
     *
     * @param measureSpec the measure specification of parent dimension
     * @param padding the parent padding
     * @param childSize the child element dimension
     * @param scaleFactor the scale factor of dimension
     * @return the measurement specification of the child element
     */
    private fun getChildMeasureSpec2(
        measureSpec: Int,
        padding: Int,
        childSize: Int,
        scaleFactor: Float
    ): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        var size = MeasureSpec.getSize(measureSpec) - padding

        if (mode == MeasureSpec.EXACTLY && fitTargetSize) {
            size = (size / scaleFactor).roundToInt()
        }

        return getChildMeasureSpec(MeasureSpec.makeMeasureSpec(size, mode), 0, childSize)
    }

    /**
     * Get measured size given scale factor and child size
     *
     * @param measureSpec the measure specification of dimension
     * @param padding the padding
     * @param childSize the child element dimension
     * @param scaleFactor the scale factor of dimension
     * @return the measured size
     */
    private fun getMeasureSize(
        measureSpec: Int,
        padding: Int,
        childSize: Int,
        scaleFactor: Float
    ): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val size = MeasureSpec.getSize(measureSpec)
        return if (mode == MeasureSpec.EXACTLY) size else (childSize * scaleFactor).roundToInt() + padding
    }

    /**
     * Constructor called by derived classes when creating their SavedState objects
     *
     * @params superState the state of the superclass of this view
     */
    private class ScaleSavedState(superState: Parcelable?) :
        AbsSavedState(superState ?: EMPTY_STATE) {

        var fitTargetSize = true
        var minZoom = DEFAULT_MIN_ZOOM
        var maxZoom = DEFAULT_MAX_ZOOM
        var gravity = DEFAULT_TARGET_GRAVITY
        var scaleFactor = 1f

        @JvmOverloads
        constructor(source: Parcel, loader: ClassLoader? = null) : this(
            source.readParcelable<Parcelable>(loader)
        ) {
            fitTargetSize = source.readInt() != 0
            minZoom = source.readFloat()
            maxZoom = source.readFloat()
            gravity = source.readInt()
            scaleFactor = source.readFloat()
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.apply {
                writeParcelable(superState, flags)
                writeInt(if (fitTargetSize) 1 else 0)
                writeFloat(minZoom)
                writeFloat(maxZoom)
                writeInt(gravity)
                writeFloat(scaleFactor)
            }
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<ScaleSavedState> {
            override fun createFromParcel(source: Parcel, loader: ClassLoader?): ScaleSavedState =
                createFromParcel(source)

            override fun createFromParcel(parcel: Parcel) = ScaleSavedState(parcel)

            override fun newArray(size: Int) = arrayOfNulls<ScaleSavedState?>(size)

        }
    }

}