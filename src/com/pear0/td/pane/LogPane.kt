package com.pear0.td.pane

import com.googlecode.lanterna.graphics.TextGraphics
import java.util.*

/**
 * Created by william on 3/21/17.
 */
class LogPane : Pane() {

    private val lines = LinkedList<String>()

    @Synchronized
    fun append(str: String) {
        lines.addAll(str.trimEnd().split('\n'))
        while (lines.size > 500) {
            lines.removeAt(0)
        }
    }

    override val isDirty = true

    @Synchronized
    override fun draw(g: TextGraphics) {
        super.draw(g)

        for (i in (1 .. Math.min(g.size.rows, lines.size))) {
            g.putString(0, g.size.rows - i, lines[lines.size - i])
        }

    }
}