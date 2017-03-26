package com.pear0.td.command

import com.pear0.td.TermDiscord
import com.pear0.td.UserException
import com.pear0.td.action.ComposeMessageAction

/**
 * Created by William on 3/25/2017.
 */
internal class DefaultCommandHandler : CommandHandler {
    internal enum class Commands(info: String, val func: () -> Unit, altName: String? = null) {
        QUIT("Quits term-discord.", TermDiscord::shutdown),
        MESSAGE("Message the current channel.", {
            TermDiscord.paneManager.setFocus(TermDiscord.getStatusPane(), context =
            ComposeMessageAction(TermDiscord.jda.getTextChannelById(TermDiscord.guilds.getHighlightedChannelId()!!)))
        }),
        CHANNELSELECT("Change the current channel", {
            TermDiscord.paneManager.setFocus(TermDiscord.getChannelsPane())
        }),
        PREV("Jump to previous page", { TermDiscord.getPagingPane().changePage(-1) }),
        NEXT("Jump to next page", { TermDiscord.getPagingPane().changePage(1) })

        ;

        val info = CommandInfo(altName ?: name, info)
    }


    override val id = "[default]"

    override val commandList = Commands.values().map { it.info }

    override fun handleString(text: String) {
        val command = Commands.values().find { it.info.name.equals(text, ignoreCase = true) } ?:
                throw UserException("Unknown command: $text")
        command.func()
    }

}