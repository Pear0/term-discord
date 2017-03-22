package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import java.util.*

/**
 * Created by william on 3/21/17.
 */
class LinearLayoutPane : Pane() {
    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }

    val children = ArrayList<Pane>()

    var orientation = Orientation.HORIZONTAL

    override val isDirty = children.any(Pane::isDirty)

    private fun partition(length: Int): List<Int> {
        val ls = children.map { length / children.size }.toMutableList()
        for (i in 0 until length % children.size) { ls[i]++ }

        return ls
    }

    override fun draw(g: TextGraphics) {
        val lengths = partition(if (orientation == Orientation.HORIZONTAL) g.size.columns else g.size.rows)


        fun pos(i: Int) = when(orientation) {
            Orientation.HORIZONTAL -> TerminalPosition(i, 0)
            Orientation.VERTICAL -> TerminalPosition(0, i)
        }

        fun size(i: Int) = when(orientation) {
            Orientation.HORIZONTAL -> TerminalSize(i, g.size.rows)
            Orientation.VERTICAL -> TerminalSize(g.size.columns, i)
        }

        var acc = 0

        for (i in children.indices) {
            children[i].draw(g.newTextGraphics(pos(acc), size(lengths[i])))
            acc += lengths[i]
        }

    }
}