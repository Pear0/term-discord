package com.pear0.td.pane

import com.googlecode.lanterna.graphics.TextGraphics

/**
 * Created by william on 3/21/17.
 */
open class Pane {

    open val isDirty: Boolean = false

    open fun draw(g: TextGraphics) {
        g.fill(' ')
    }

}