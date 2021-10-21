package com.vaca.throttlecurve

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.core.content.ContextCompat

import java.util.*

class TendencyView : View {
    private val sysPaint = Paint()
    private val sysShadowPaint = Paint()
    private val diaPaint = Paint()
    private val diaShadowPaint = Paint()
    private val boarderPaint = Paint()
    private val gridPaint = Paint()
    private val canvasW = getPixel(R.dimen.w)
    private val canvasH = getPixel(R.dimen.h)
    private val canvasHF = canvasH.toFloat()
    private val canvasWF = canvasW.toFloat()
    private val diaW = getPixel(R.dimen.dia_w).toFloat()

    var seeTime: Long = System.currentTimeMillis()
    var maxH = 150f
    var minH = 50f

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        boarderPaint.apply {
            color = getColor(R.color.myBlue)
            style = Paint.Style.STROKE
            strokeWidth = getPixel(R.dimen.grid_w).toFloat() * 2
        }

        gridPaint.apply {
            color = getColor(R.color.myGray)
            style = Paint.Style.STROKE
            strokeWidth = getPixel(R.dimen.grid_w).toFloat()
        }

        diaPaint.apply {
            color = getColor(R.color.diaColor)
            style = Paint.Style.FILL
        }
        diaShadowPaint.apply {
            color = getColor(R.color.diaShadowColor)
            style = Paint.Style.FILL
        }
        sysPaint.apply {
            color = getColor(R.color.sysColor)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        sysShadowPaint.apply {
            color = getColor(R.color.sysShadowColor)
            style = Paint.Style.FILL
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(getColor(R.color.white))

        for (k in 0 until 2) {
            canvas.drawLine(
                0f,
                (k + 1) * canvasH.toFloat() / 3,
                canvasW.toFloat(),
                (k + 1) * canvasH.toFloat() / 3,
                gridPaint
            )
        }

        for (k in 0 until 6) {
            canvas.drawLine(
                (k + 1) * canvasW.toFloat() / 7,
                0f,
                (k + 1) * canvasW.toFloat() / 7,
                canvasH.toFloat(),
                gridPaint
            )
        }

        var boarderPath = Path()
        boarderPath.moveTo(
            0f,
            0f
        )
        boarderPath.lineTo(0f, canvasH.toFloat())
        boarderPath.lineTo(canvasW.toFloat(), canvasH.toFloat())
        boarderPath.lineTo(canvasW.toFloat(), 0f)
        boarderPath.lineTo(0f, 0f)
        canvas.drawPath(boarderPath, boarderPaint)

        focusMyDream(canvas, seeTime)


    }

    private fun getColor(resource_id: Int): Int {
        return ContextCompat.getColor(context, resource_id)
    }

    private fun getPixel(resource_id: Int): Int {
        return resources.getDimensionPixelSize(resource_id)
    }

    override fun onMeasure(width: Int, height: Int) {
        setMeasuredDimension(canvasW, canvasH)
    }


    private fun focusMyDream(canvas: Canvas) {
        tendencyData?.run {
            if (tendencyData!!.isEmpty()) {
                return
            }
            val bottomDate = Array<String>(7) {
                ""
            }
            val lastMilli = tendencyData!![0].date
            for (k in 0 until 7) {
                bottomDate[k] = DateStringUtil.timeConvert2(lastMilli - (6 - k) * 86400000)
            }

            var dayIndex = 6
            var sysList: ArrayList<Int> = ArrayList()
            var diaList: ArrayList<Int> = ArrayList()
            for (dataItem in tendencyData!!) {
                if (DateStringUtil.timeConvert2(dataItem.date) == bottomDate[dayIndex]) {
                    sysList.add(dataItem.sys)
                    diaList.add(dataItem.dia)
                } else {
                    drawSysList(canvas, dayIndex, sysList)
                    drawDiaList(canvas, dayIndex, diaList)
                    sysList.clear()
                    diaList.clear()
                    sysList.add(dataItem.sys)
                    diaList.add(dataItem.dia)
                    if (dayIndex == 0) return
                    dayIndex--
                }
            }
        }


    }

    private fun abs(a: Long, b: Long): Long {
        var c = a.toInt()
        var d = b.toInt()
        if (c > d) {
            return (c - d).toLong()
        } else {
            return (d - c).toLong()
        }
    }

    private fun appropriate(a: Long, b: Long): Boolean {
        return abs(a, b) < 864000000
    }

    private fun focusMyDream(canvas: Canvas, time: Long) {
        tendencyData?.run {
            if (tendencyData!!.isEmpty()) {
                return
            }
            val bottomDate = Array<String>(7) {
                ""
            }
            for (k in 0 until 7) {
                bottomDate[k] = DateStringUtil.timeConvert2(time - (6 - k) * 86400000)
            }

            var dayIndex = 6
            var sysList: ArrayList<Int> = ArrayList()
            var diaList: ArrayList<Int> = ArrayList()
            for ((index, dataItem) in tendencyData!!.withIndex()) {
                if (time < dataItem.date) {
                    if (DateStringUtil.timeConvert3(time) != DateStringUtil.timeConvert3(dataItem.date)) {
                        continue
                    }
                }


                if ((DateStringUtil.timeConvert2(dataItem.date) == bottomDate[dayIndex]) && appropriate(
                        dataItem.date,
                        time
                    )
                ) {
                    sysList.add(dataItem.sys)
                    diaList.add(dataItem.dia)
                    if (index == tendencyData!!.size - 1) {
                        drawSysList(canvas, dayIndex, sysList)
                        drawDiaList(canvas, dayIndex, diaList)
                    }
                } else {
                    drawSysList(canvas, dayIndex, sysList)
                    drawDiaList(canvas, dayIndex, diaList)
                    sysList.clear()
                    diaList.clear()
                    sysList.add(dataItem.sys)
                    diaList.add(dataItem.dia)

                    var inBottomFlag = false
                    for ((index, k) in bottomDate.withIndex()) {
                        if (k == DateStringUtil.timeConvert2(dataItem.date)) {
                            dayIndex = index
                            inBottomFlag = true
                        }
                    }
                    if (!inBottomFlag) {
                        return@run
                    } else {
                        if (index == tendencyData!!.size - 1) {
                            drawSysList(canvas, dayIndex, sysList)
                            drawDiaList(canvas, dayIndex, diaList)
                        }
                    }
                }
            }
        }


    }

    var x1 = 0f
    var y1 = 0f
    var cr = ""
    var cr2 = ""
    var gi: Long = 0
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
                gi = seeTime
            }

            ACTION_UP -> {

            }

            ACTION_MOVE -> {
                cr = DateStringUtil.timeConvert2(seeTime)
                seeTime = (gi.toFloat() + (x1 - event.x) * 1000000f).toLong()
                cr2 = DateStringUtil.timeConvert2(seeTime)
                if (cr != cr2) {
                    BleServer.pc100Ten.postValue(seeTime)
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawDiaList(canvas: Canvas, i: Int, b1: ArrayList<Int>) {
        val b = virL2RealL(b1)
        if (b.size == 0) return
        val x = i * canvasWF / 7 + canvasWF / 14
        val halfWidth = diaW / 2
        if (b.size != 1) {
            b.sort()
            canvas.drawRect(
                RectF(
                    x - halfWidth,
                    b[0].toFloat(),
                    x + halfWidth,
                    b[b.size - 1].toFloat()
                ), diaShadowPaint
            )
            drawDia(canvas, x, b[0].toFloat())
            drawDia(canvas, x, b.last().toFloat())
        } else {
            drawDia(canvas, x, b[0].toFloat())
        }

    }


    private fun drawSysList(canvas: Canvas, i: Int, b1: ArrayList<Int>) {
        val b = virL2RealL(b1)
        if (b.size == 0) return
        val x = i * canvasWF / 7 + canvasWF / 14
        val halfWidth = diaW / 2
        if (b.size != 1) {
            b.sort()
            canvas.drawRect(
                RectF(
                    x - halfWidth,
                    b[0].toFloat(),
                    x + halfWidth,
                    b[b.size - 1].toFloat()
                ), sysShadowPaint
            )
            drawSys(canvas, x, b[0].toFloat())
            drawSys(canvas, x, b.last().toFloat())
        } else {
            drawSys(canvas, x, b[0].toFloat())
        }


    }


    private fun drawDia(canvas: Canvas, x: Float, y: Float) {
        val halfWidth = diaW / 2
        val path = Path()
        path.moveTo(x, y + halfWidth)
        path.lineTo(x - halfWidth, y)
        path.lineTo(x, y - halfWidth)
        path.lineTo(x + halfWidth, y)
        path.lineTo(x, y + halfWidth)
        path.close()
        canvas.drawPath(path, diaPaint)
    }

    private fun drawSys(canvas: Canvas, x: Float, y: Float) {
        val halfWidth = diaW / 2
        canvas.drawCircle(x, y, halfWidth, sysPaint)
    }

    private fun vir2Real(f: Float): Float {
        return canvasHF - (f - minH) / maxH * canvasHF
    }

    private fun virL2RealL(a: ArrayList<Int>): ArrayList<Int> {
        val b = ArrayList<Int>()
        for (k in a) {
            b.add(vir2Real(k.toFloat()).toInt())
        }
        return b
    }
}