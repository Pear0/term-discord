package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
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

    /* Events */

    open fun onFocused() {}

    open fun onUnfocused() {}

}