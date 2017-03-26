package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.pear0.td.PaneManager

/**
 * Created by william on 3/21/17.
 */
open class Pane {


    open var needsRedraw: Boolean = false
    open var needsRelayout: Boolean = false

    //var position: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER
    var size: TerminalSize = TerminalSize.ZERO

    var id: String? = null

    var parent: Pane? = null
        internal set

    val root: Pane
        get() = parent?.root ?: this

    val manager: PaneManager?
        get() = PaneManager.findManager(root)

    val hasFocus: Boolean
        get() = manager?.hasFocus(this) ?: false

    open fun onLayoutChanged(/*pos: TerminalPosition,*/ size: TerminalSize) {
        //this.position = pos
        this.size = size
    }

    open fun draw(g: TextGraphics) {
        g.fill(' ')
    }

    open fun findPaneById(id: String): Pane? {
        return if (this.id == id) this else null
    }

    fun <T: Pane> findPane(id: String): T? {
        @Suppress("UNCHECKED_CAST")
        return findPaneById(id) as T?
    }

    fun focus() {
        PaneManager.findManager(root)?.setFocus(this)
    }

    fun unfocus() {
        val manager = PaneManager.findManager(root) ?: return
        if (manager.hasFocus(this)) {
            manager.setFocus(null)
        }
    }

    /* Events */

    open fun onFocused(context: Any? = null) {}

    open fun onUnfocused() {}

    open fun onKeyTyped(key: KeyStroke) {}

}