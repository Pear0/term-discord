package com.pear0.td.pane.discord

import io.reactivex.Observable
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel

/**
 * Created by william on 3/27/17.
 */
class VoiceSelectorPane(guildsStream: Observable<List<Guild>>, channelObservable: (Guild) -> Observable<List<VoiceChannel>>) :
        DiscordSelectorPane<VoiceChannel>(guildsStream, channelObservable) {

    override fun channelToEntry(guild: Guild, channel: VoiceChannel): Entry? {
        if (guild.selfMember.hasPermission(channel, Permission.VOICE_CONNECT)) return Entry(channel.id, channel.name)
        else return null
    }
}