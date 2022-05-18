package com.example.traders.presentation.customviews

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.res.ResourcesCompat
import com.example.traders.R
import com.example.traders.network.models.cryptoChartData.CryptoChart
import com.example.traders.presentation.customviews.candleChartData.CandlePosition
import com.example.traders.presentation.customviews.candleChartData.LinePosition
import com.example.traders.utils.roundAndFormatDouble
import java.util.Collections.binarySearch
import kotlin.math.abs

class CandleChart(context: Context, attrs: AttributeSet) : View(context, attrs), ScrubListener {
    private var cryptoData: List<CryptoChart> = emptyList()
    private var linePositions: MutableList<LinePosition>   = mutableListOf()
    private var candlePositions: MutableList<CandlePosition> = mutableListOf()
    private var mWidth = 0f
    private var mHeight = 0f
    private var candleWidth = 0f
    private var candleSpacing = 0f
    private var chartSizeMultiplier = 0.84f
    private var mGreenPaint = Paint()
    private var mRedPaint = Paint()
    private var mGreenLinePaint = Paint()
    private var mRedLinePaint = Paint()
    private var mTextPaint = Paint()
    private var minVal = 0f
    private var maxVal = 0f
    private var maxValPosition = 0f
    private var minValPosition = 0f
    private var currentPricePosition = 0f

    // scrubLine
    val scrubLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val scrubLinePath = Path()
    // listeners
    private var scrubListener: OnScrubListener? = null
    lateinit var scrubGestureDetector: ScrubGestureDetector

    init {
        setPaintColors()
        setScrubLinePaint()
        setPaintSize()
        setScrubGestureDetector()
    }

    fun setScrubListener(listener: (CryptoChart?) -> Unit) {
        scrubListener = OnScrubListener(listener)
    }

    fun importListValues(list: List<CryptoChart>) {
        //listof([volume, open, high, low, close], [], ..., [])
        if(list == cryptoData) return
        cryptoData = list
        minVal = list.minOf { it.low }
        maxVal = list.maxOf { it.high }
        setWidthAndHeigth()
        calculateCandleSizes()
        calculateCoords()
        invalidate()
    }

    private fun setPaintColors() {
        mGreenPaint.color = ResourcesCompat.getColor(getResources(), R.color.green, null); //without theme
        mRedPaint.color = ResourcesCompat.getColor(getResources(), R.color.red, null);
        mGreenLinePaint.color = ResourcesCompat.getColor(getResources(), R.color.green, null);
        mRedLinePaint.color = ResourcesCompat.getColor(getResources(), R.color.red, null);
    }

    private fun setScrubLinePaint() {
        scrubLinePaint.style = Paint.Style.STROKE
        scrubLinePaint.strokeWidth = 5f
        scrubLinePaint.color = ResourcesCompat.getColor(getResources(), R.color.black, null)
//        scrubLinePaint.strokeCap = Paint.Cap.ROUND
    }

    private fun setPaintSize() {
        mTextPaint.textSize = 25F
        mTextPaint.strokeWidth = 3F
        mGreenLinePaint.strokeWidth = 3f
        mRedLinePaint.strokeWidth = 3f
    }

    private fun setScrubGestureDetector() {
        val handler = Handler(Looper.getMainLooper())
        val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        scrubGestureDetector = ScrubGestureDetector(this, handler, touchSlop)
        scrubGestureDetector.enabled = true
        setOnTouchListener(scrubGestureDetector)
    }

    private fun setWidthAndHeigth() {
        mWidth = width.toFloat()
        mHeight = height.toFloat()
    }

    private fun calculateCandleSizes() {
        candleWidth = mWidth * chartSizeMultiplier / (cryptoData.size-1) * 0.6f
        candleSpacing = mWidth * chartSizeMultiplier / (cryptoData.size-1) * 0.4f
        mGreenPaint.strokeWidth = candleWidth
        mRedPaint.strokeWidth = candleWidth
    }

    private fun calculateCoords() {
        var xVal: Float = mWidth * (1 - chartSizeMultiplier) / 4 // assign start position of X
        var yLineTop: Float
        var yLineBottom: Float
        var yCandleOpen: Float
        var yCandleClose: Float
        linePositions.clear()
        candlePositions.clear()

        val minMaxDiff = maxVal - minVal

        //listof([volume, open, high, low, close], [], ..., [])

        for(i in 0..(cryptoData.size -1)) {
            yLineTop = mHeight - ((mHeight * 0.9f) * ((cryptoData[i].high - minVal)/minMaxDiff) + (mHeight * 0.05f))
            yLineBottom = mHeight - ((mHeight * 0.9f) * ((cryptoData[i].low - minVal)/minMaxDiff) + (mHeight * 0.05f))
            val linePosition = LinePosition(
                yTop = yLineTop,
                yBot = yLineBottom,
                x = xVal
            )
            linePositions.add(linePosition)
//            linePositions.add(listOf(yLineTop, yLineBottom, xVal))

            if(cryptoData[i].high == maxVal) maxValPosition = yLineTop
            if(cryptoData[i].low == minVal) minValPosition = yLineBottom

            yCandleOpen = mHeight - ((mHeight * 0.9f) * ((cryptoData[i].open - minVal)/minMaxDiff) + (mHeight * 0.05f))
            yCandleClose = mHeight - ((mHeight * 0.9f) * ((cryptoData[i].close - minVal)/minMaxDiff) + (mHeight * 0.05f))
            val candlePosition = CandlePosition(
                candleOpen = yCandleOpen,
                candleClose = yCandleClose,
                x = xVal
            )
            candlePositions.add(candlePosition)
//            candlePositions.add(listOf(yCandleOpen, yCandleClose, xVal))

            if(i == (cryptoData.size-1)) currentPricePosition = yCandleClose
            xVal += (candleSpacing) + candleWidth
        }
    }

    private fun drawCandles( canvas: Canvas? ) {
        if(cryptoData.size > 0) {
            Log.e("tag", "drawCandles", )
            for(i in 0..linePositions.size - 1) {
                // height is compared inversely duo to top position of height is 0
                if(candlePositions[i].candleClose < candlePositions[i].candleOpen) {
                    // draw line
                    canvas?.drawLine(linePositions[i].x, linePositions[i].yTop, linePositions[i].x, linePositions[i].yBot, mGreenLinePaint)
                    canvas?.drawLine(candlePositions[i].x, candlePositions[i].candleOpen, candlePositions[i].x, candlePositions[i].candleClose, mGreenPaint)

                } else {
                    canvas?.drawLine(linePositions[i].x, linePositions[i].yTop, linePositions[i].x, linePositions[i].yBot, mRedLinePaint)
                    canvas?.drawLine(candlePositions[i].x, candlePositions[i].candleOpen, candlePositions[i].x, candlePositions[i].candleClose, mRedPaint)
                }
                // Drawing high, low, current lines
                if(linePositions[i].yTop == maxValPosition){
                    canvas?.drawLine(linePositions[i].x,linePositions[i].yTop, mWidth, linePositions[i].yTop, mGreenLinePaint)
                    mTextPaint.color = ResourcesCompat.getColor(getResources(), R.color.green, null);
                    canvas?.drawText(roundAndFormatDouble(cryptoData[i].high.toDouble()), mWidth - 115F, linePositions[i].yTop - 5F, mTextPaint)
                }
                if(linePositions[i].yBot == minValPosition) {
                    canvas?.drawLine(linePositions[i].x,linePositions[i].yBot, mWidth, linePositions[i].yBot, mRedLinePaint)
                    mTextPaint.color = ResourcesCompat.getColor(getResources(), R.color.red, null);
                    canvas?.drawText(roundAndFormatDouble(cryptoData[i].low.toDouble()), mWidth - 115F, linePositions[i].yBot - 5F, mTextPaint)
                }
            }

            // Draw most recent price
            mTextPaint.color = ResourcesCompat.getColor(getResources(), R.color.light_gray, null);
            canvas?.drawLine(candlePositions.last().x,candlePositions.last().candleClose, mWidth, candlePositions.last().candleClose, mTextPaint)
            canvas?.drawText(roundAndFormatDouble(cryptoData.last().close.toDouble()), mWidth - 115F, candlePositions.last().candleClose - 5F, mTextPaint)
        }
    }

    class OnScrubListener(private val clickListener: (CryptoChart?) -> Unit) {
        fun onScrubbed(cryptoChart: CryptoChart?) = clickListener(cryptoChart)
    }

    private fun setScrubLine(x: Float) {
        scrubLinePath.reset()
        scrubLinePath.moveTo(x, paddingTop.toFloat())
        scrubLinePath.lineTo(x, (height - paddingBottom).toFloat())
        invalidate()
    }

    private fun getNearestIndex(xPoints: List<Float>, x: Float): Int {
        val index = binarySearch(xPoints, x)

        // if index non negative it means that x matches exact point
        if(index >= 0) return index

        // if index closest to left return first element
        if(index == -1) return index + 1

        // if index closest to right return last element
        if(abs(index) == xPoints.size) return xPoints.size - 1

        // otherwise return element closest to the x coordinate
        val point1Index = abs(index+1)
        val point2Index = abs(index)
        val distance1 = abs(x - xPoints[point1Index])
        val distance2 = abs(x - xPoints[point2Index])
        return if(distance1 <= distance2) {
             point1Index
        } else {
             point2Index
        }
    }

    override fun onScrubbed(x: Float, y: Float) {
//        if (adapter == null || adapter.getCount() == 0) return
        parent.requestDisallowInterceptTouchEvent(true)
        val xPoints = linePositions.map { it.x }
        val index = getNearestIndex(xPoints, x)
        if (scrubListener != null) {
            scrubListener!!.onScrubbed(cryptoData[index])
        }
        setScrubLine(xPoints[index])
    }

    override fun onScrubEnded() {
        scrubLinePath.reset()
        scrubListener?.onScrubbed(null)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
        canvas?.drawColor(Color.WHITE)
        drawCandles(canvas)
        canvas?.drawPath(scrubLinePath, scrubLinePaint)
    }
}