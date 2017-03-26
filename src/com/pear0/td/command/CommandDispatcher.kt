package com.pear0.td.command

import org.apache.commons.collections4.map.ListOrderedMap

/**
 * Created by William on 3/25/2017.
 */
class CommandDispatcher {
    companion object {

        val defaultDispatcher by lazy { CommandDispatcher().apply { register(DefaultCommandHandler()) } }

    }

    private val handlers = ListOrderedMap<String, CommandHandler>()

    fun register(handler: CommandHandler) {
        if (handler.id in handlers) {
            throw IllegalArgumentException("CommandHandler with the same id `${handler.id}` already registered.")
        }

        handlers.put(handler.id, handler)
    }

    fun findHandlerForCommand(text: String): CommandHandler? {
        if ("\\" in text) {
            return handlers[text.substringBefore("\\")]
        } else {
            return handlers.values.asSequence().find { it.canHandleString(text) }
        }
    }

    fun canDispatchCommand(text: String): Boolean = findHandlerForCommand(text)?.canHandleString(text) ?: false

    fun dispatchCommand(text: String) = findHandlerForCommand(text)?.handleString(text) ?: Unit

    fun asHandler() = object : CommandHandler {
        override val id: String
            get() = handlers.keyList().joinToString("~")

        override val commandList: List<CommandInfo>
            get() = handlers.values.flatMap { it.commandList }.distinct()

        override fun canHandleString(text: String) = canDispatchCommand(text)

        override fun handleString(text: String) = dispatchCommand(text)
    }

}