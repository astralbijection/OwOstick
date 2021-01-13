package tech.astrid.owostick.android.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import tech.astrid.owostick.android.R

/**
 * It's a big red slider. What will it do!?!?!?
 */
class BigSlider : View {

    private var _value: Float = 0f

    /**
     * In the example view, this dimension is the font size.
     */
    var value: Float
        get() = _value
        set(value) {
            _value = value
            invalidate()
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        // Load attributes
        context.obtainStyledAttributes(attrs, R.styleable.BigSlider, defStyle, 0)
                .apply {
                    _value = getFloat(R.styleable.BigSlider_value, 0f)
                }.recycle()
    }

    private val paint = Paint(0).apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawColor(Color.BLACK)
        canvas.drawRect(0f, value * height, width, height, paint)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        value = event!!.y / height.toFloat()
        invalidate()
        return true
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        value = event!!.y / height.toFloat()
        invalidate()
        return true
    }
}