package com.anwesh.uiprojects.positionalbarswapview

/**
 * Created by anweshmishra on 01/07/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.app.Activity
import android.content.Context

val colors : Array<String> = arrayOf("#3F51B5", "#4CAF50", "#03A9F4", "#F44336", "#009688")
val bars : Int = 5
val scGap : Float = 0.02f / bars
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20
