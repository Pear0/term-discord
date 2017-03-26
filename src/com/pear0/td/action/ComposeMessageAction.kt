package com.pear0.td.action

import net.dv8tion.jda.core.entities.MessageChannel

/**
 * Created by William on 3/24/2017.
 */
class ComposeMessageAction(val channel: MessageChannel) : StringAction {

    override fun complete(text: String) {
        channel.sendMessage(text).queue()
    }
}