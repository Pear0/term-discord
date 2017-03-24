package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics

/**
 * Created by william on 3/23/17.
 */
class StatusLayoutPane : Pane() {
    data class StatusBar(val left: String, val right: String)

    private var child: Pane? = null
    var status: StatusBar = StatusBar("", "")


    override var needsRedraw: Boolean
        get() = super.needsRedraw || (child?.needsRedraw ?: false)
        set(value) { super.needsRedraw = value }

    override var needsRelayout: Boolean
        get() = super.needsRelayout || (child?.needsRelayout ?: false)
        set(value) { super.needsRelayout = value }

    private fun lessRow(size: TerminalSize) = if (size.rows == 0) size else size.withRelative(0, -1)

    fun setChild(child: Pane) {
        this.child = child
        child.onLayoutChanged(lessRow(size))
    }

    override fun onLayoutChanged(size: TerminalSize) {
        super.onLayoutChanged(size)
        child?.onLayoutChanged(lessRow(size))
    }

    override fun draw(g: TextGraphics) {
        super.draw(g)
        child?.draw(g.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, child!!.size))
        g.putString(0, size.rows - 1, status.left)
        g.putString(size.columns - size.columns - 1, size.rows - 1, status.right)
    }

    override fun findPaneById(id: String): Pane? {
        return super.findPaneById(id) ?: child?.findPaneById(id)
    }
}