package com.pear0.td.action

import com.pear0.td.command.CommandHandler

/**
 * Created by William on 3/25/2017.
 */
class CommandAction(private val handler: CommandHandler, private val displayPrefix: String = "") : FormattingStringAction {

    override fun complete(text: String) {
        println(displayPrefix + text)
        handler.handleString(text)
    }

    override fun format(text: String): String {
        return displayPrefix + text
    }
}