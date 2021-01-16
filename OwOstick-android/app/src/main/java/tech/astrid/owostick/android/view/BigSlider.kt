package tech.astrid.owostick.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import io.reactivex.rxjava3.subjects.BehaviorSubject
import tech.astrid.owostick.android.R

/**
 * It's a big red slider. What will it do!?!?!?
 */
class BigSlider : View {

    var value: Float
        get() = valueSubject.value
        set(value) {
            valueSubject.onNext(value)
        }

    val valueSubject = BehaviorSubject.createDefault(0.0f)!!

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
                    valueSubject.onNext(getFloat(R.styleable.BigSlider_value, 0f))
                }.recycle()

        valueSubject.subscribe { invalidate() }
    }

    private val paint = Paint(0).apply {
        color = Color.RED
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawColor(Color.BLACK)
        canvas.drawRect(0f, (1 - value) * height, width, height, paint)
    }

    private fun yToValue(y: Float): Float {
        return (1 - y / height.toFloat()).coerceIn(0f, 1f)
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        super.onDragEvent(event)
        value = yToValue(event!!.y)
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        value = yToValue(event!!.y)
        return true
    }
}