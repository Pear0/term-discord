package com.pear0.td.command

/**
 * Created by William on 3/25/2017.
 */
interface CommandHandler {

    val id: String

    val commandList: List<CommandInfo>

    fun canHandleString(text: String): Boolean = commandList.any { it.name.equals(text, ignoreCase = true) }

    fun handleString(text: String)

}