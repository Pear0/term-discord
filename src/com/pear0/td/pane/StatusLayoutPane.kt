package com.pear0.td.pane

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.PaneManager
import com.pear0.td.TermDiscord
import com.pear0.td.action.ComposeMessageAction
import com.pear0.td.action.FormattingStringAction
import com.pear0.td.action.StringAction
import com.pear0.td.action.UserAction

/**
 * Created by william on 3/23/17.
 */
class StatusLayoutPane : Pane() {
    interface StatusBarEntry {
        fun draw(g: TextGraphics)
    }

    class StatusBar(var buffer: String, var right: StatusBarEntry)

    internal class EmptyStatusBarEntry : StatusBarEntry {
        override fun draw(g: TextGraphics) {}
    }

    private var child: Pane? = null
    val status: StatusBar = StatusBar("", EmptyStatusBarEntry())
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
        child.parent = this
        child.onLayoutChanged(lessRow(size))
    }

    override fun onLayoutChanged(size: TerminalSize) {
        super.onLayoutChanged(size)
        child?.onLayoutChanged(lessRow(size))
    }

    override fun draw(g: TextGraphics) {
        super.draw(g)
        child?.draw(g.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, child!!.size))

        val statusGraphics = g.newTextGraphics(TerminalPosition(0, size.rows - 1), TerminalSize(size.columns, 1))

        val action = this.action

        statusGraphics.putString(0, 0, when (action) {
            is FormattingStringAction -> action.format(status.buffer)
            else -> status.buffer
        })

        // status.left.draw(statusGraphics)
        status.right.draw(statusGraphics)


        //g.putString(size.columns - size.columns - 1, size.rows - 1, status.right)
    }

    override fun onFocused(context: Any?) {
        if (context is UserAction) {
            action = context
        }
    }

    override fun onUnfocused() {
        action = null
        status.buffer = ""
    }

    override fun onKeyTyped(key: KeyStroke) {

        when (key.keyType) {
            KeyType.Enter -> {
                val action = this.action
                this.action = null

                // an action can set another action
                when (action) {
                    is StringAction -> {
                        action.complete(status.buffer)
                    }
                    else -> {}
                }

                if (this.action == null) {
                    unfocus()
                }

            }
            KeyType.Backspace -> {
                status.let { it.buffer = it.buffer.dropLast(1) }
            }
            else -> {
                status.buffer += if (key.keyType == KeyType.Character) key.character.toString() else "<${key.keyType.name}>"

            }
        }

    }

    override fun findPaneById(id: String): Pane? {
        return super.findPaneById(id) ?: child?.findPaneById(id)
    }
}