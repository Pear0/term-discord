package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by william on 3/21/17.
 */
class LinearLayoutPane : Pane() {
    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }

    internal data class PaneEntry(val pane: Pane, var position: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER, val weight: Float = 1f, val minSize: Int = 0)

    private val children = ArrayList<PaneEntry>()

    var orientation = Orientation.HORIZONTAL

    override var needsRedraw = children.any { it.pane.needsRedraw }

    override var needsRelayout: Boolean
        get() = super.needsRelayout || children.any { it.pane.needsRelayout }
        set(value) { super.needsRelayout = value }

    fun addChild(pane: Pane, weight: Float = 1f, minSize: Int = 0) {
        children.add(PaneEntry(pane = pane, weight = weight, minSize = minSize))
        pane.parent = this
    }

    private fun partition(length_: Int): List<Int> {
        var weightSum = children.map { it.weight }.sum()
        var length = length_

        val ls = ArrayList<Int>(children.size)

        for (i in children.indices) {
            ls.add(Math.round(children[i].weight * length / weightSum))
            length -= ls[i]
            weightSum -= children[i].weight
        }

        return ls
    }

    override fun onLayoutChanged(/*pos: TerminalPosition,*/ size: TerminalSize) {
        super.onLayoutChanged(/*pos,*/ size)

        val lengths = partition(if (orientation == Orientation.HORIZONTAL) size.columns else size.rows)


        fun pos(i: Int) = when(orientation) {
            Orientation.HORIZONTAL -> TerminalPosition(i, 0)
            Orientation.VERTICAL -> TerminalPosition(0, i)
        }

        fun size(i: Int) = when(orientation) {
            Orientation.HORIZONTAL -> TerminalSize(i, size.rows)
            Orientation.VERTICAL -> TerminalSize(size.columns, i)
        }

        var acc = 0

        for (i in children.indices) {
            children[i].position = pos(acc)
            children[i].pane.onLayoutChanged(size(lengths[i]))

            acc += lengths[i]
        }

    }

    override fun draw(g: TextGraphics) {


        for ((pane, position) in children) {

            pane.draw(g.newTextGraphics(position, pane.size))
        }

    }

    override fun findPaneById(id: String): Pane? {
        return super.findPaneById(id) ?: children.asSequence().mapNotNull { it.pane.findPaneById(id) }.firstOrNull()
     }
}