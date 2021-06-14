package com.example.nekomanju.chart

import android.content.Context
import android.widget.TextView
import com.example.nekomanju.R
import com.example.nekomanju.util.Utility.Companion.formatPrecise
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class SimpleMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource){
    private lateinit var textView: TextView
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        textView = findViewById(R.id.tvSimple)
        textView.text = "${e?.y?.toDouble()?.formatPrecise(4)} ppm"
        super.refreshContent(e, highlight)
    }
    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat() - 20f)
    }
}
