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

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawPositionalBarSwap(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, 2)
    val sf2 : Float = sf.divideScale(1, 2)
    val hGap : Float = h / (2 * bars + 1)
    val yStart : Float = hGap
    var y : Float = yStart
    for (j in 1..(bars - 1)) {
        val sf2j : Float = sf2.divideScale(j, bars - 1)
        y += 2 * hGap * sf2j
        save()
        translate(0f, yStart + 2 * hGap * j - 2 * hGap * sf2j)
        drawRect(RectF(0f, 0f, w * sf1, hGap), paint)
        restore()
    }
    save()
    translate(0f, y)
    drawRect(RectF(0f, 0f, w * sf1, hGap), paint)
    restore()
}

fun Canvas.drawPBSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = Color.parseColor(colors[i])
    drawPositionalBarSwap(scale, w, h, paint)
}

class PositionalBarSwapView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var prevScale : Float = 0f, var dir : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class PBSNode(var i : Int, val state : State = State()) {

        private var next : PBSNode? = null
        private var prev : PBSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = PBSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawPBSNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : PBSNode {
            var curr : PBSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class PositionalBarSwap(var i : Int) {

        private var curr : PBSNode = PBSNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : PositionalBarSwapView) {

        private val animator : Animator = Animator(view)
        private val pbs : PositionalBarSwap = PositionalBarSwap(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            pbs.draw(canvas, paint)
            animator.animate {
                pbs.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            pbs.startUpdating {
                animator.start()
            }
        }
    }
}