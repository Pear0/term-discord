package com.pear0.td.pane.discord

import io.reactivex.Observable
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel

/**
 * Created by william on 3/27/17.
 */
class ChannelSelectorPane(guildsStream: Observable<List<Guild>>, channelObservable: (Guild) -> Observable<List<TextChannel>>) :
        DiscordSelectorPane<TextChannel>(guildsStream, channelObservable) {

    override fun channelToEntry(guild: Guild, channel: TextChannel): Entry? {
        if (guild.selfMember.hasPermission(channel, Permission.MESSAGE_READ)) return Entry(channel.id, channel.name)
        else return null
    }
}