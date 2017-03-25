package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.PaneManager
import com.pear0.td.TermDiscord
import com.pear0.td.action.ComposeMessageAction
import com.pear0.td.action.UserAction

/**
 * Created by william on 3/23/17.
 */
class StatusLayoutPane : Pane() {
    data class StatusBar(val left: String, val right: String)

    private var child: Pane? = null
    var status: StatusBar = StatusBar("", "")
    private var action: UserAction? = null

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

    override fun onFocused(context: Any?) {
        if (context is UserAction) {
            action = context
        }
    }

    override fun onUnfocused() {
        action = null
        status = status.copy(left = "")
    }

    override fun onKeyTyped(key: KeyStroke) {

        when (key.keyType) {
            KeyType.Enter -> {
                val action = this.action

                when (action) {
                    is ComposeMessageAction -> {
                        action.complete(status.left)
                    }
                    else -> {}
                }

                PaneManager.findManager(root)?.setFocus(null)
            }
            KeyType.Backspace -> {
                status = status.copy(left = status.left.dropLast(1))
            }
            else -> {
                status = status.copy(left = status.left + if (key.keyType == KeyType.Character) key.character.toString() else key.keyType.name)
            }
        }

    }

    override fun findPaneById(id: String): Pane? {
        return super.findPaneById(id) ?: child?.findPaneById(id)
    }
}