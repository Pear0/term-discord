package com.pear0.td.pane

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics

/**
 * Created by William on 3/26/2017.
 */
class PagingLayoutPane : Pane() {

    private val children = ArrayList<Pane>()
    private var currentIndex = 0

    private var hasRedrawnSincePageChange = false

    override var needsRedraw = children.getOrNull(0)?.let(Pane::needsRedraw) ?: !hasRedrawnSincePageChange

    override var needsRelayout: Boolean
        get() = super.needsRelayout || children.any(Pane::needsRelayout)
        set(value) {
            super.needsRelayout = value
        }

    fun addChild(pane: Pane) {
        children.add(pane)
        pane.parent = this
        hasRedrawnSincePageChange = false
    }

    override fun onLayoutChanged(/*pos: TerminalPosition,*/ size: TerminalSize) {
        super.onLayoutChanged(/*pos,*/ size)

        for (child in children) {
            child.onLayoutChanged(size)

        }

    }

    override fun draw(g: TextGraphics) {
        val index = currentIndex

        if (index in children.indices) {
            children[index].draw(g)
        }else {
            g.fill(' ')
        }

        hasRedrawnSincePageChange = true

    }

    fun setPage(index: Int) {
        currentIndex = index
        hasRedrawnSincePageChange = false
    }

    fun changePage(amt: Int) {
        var index = currentIndex + amt
        if (index < 0) index = 0
        if (index >= children.size) index = children.size - 1
        setPage(index)
    }

    override fun findPaneById(id: String): Pane? {
        return super.findPaneById(id) ?: children.asSequence().mapNotNull { it.findPaneById(id) }.firstOrNull()
    }

}