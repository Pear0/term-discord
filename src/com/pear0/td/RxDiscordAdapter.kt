package com.pear0.td

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.entities.VoiceChannel
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelCreateEvent
import net.dv8tion.jda.core.events.channel.voice.VoiceChannelDeleteEvent
import net.dv8tion.jda.core.events.guild.GenericGuildEvent
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.hooks.ListenerAdapter

/**
 * Created by william on 3/27/17.
 */
class RxDiscordAdapter : EventListener {

    private var jda: JDA? = null

    val events: Observable<Event> = PublishSubject.create()

    val guildEvents = events.filterCast<GenericGuildEvent>()

    val guilds: Observable<List<Guild>> =
            Observable.mergeArray(
                    Observable.defer { jda?.let { Observable.just(it.guilds) } ?: Observable.empty() },
                    guildEvents.filterCast<GuildJoinEvent>().map { it.jda.guilds.filter { g -> g.id != it.guild.id } + listOf(it.guild) },
                    guildEvents.filterCast<GuildLeaveEvent>().map { it.jda.guilds.filter { g -> g.id != it.guild.id } }
            )

    val allTextChannels: Observable<List<TextChannel>> =
            Observable.mergeArray(
                    guilds.map { it.flatMap { it.textChannels } },
                    events.filterCast<TextChannelCreateEvent>().map { it.jda.guilds.flatMap { it.textChannels }.filter { g -> g.id != it.channel.id } + listOf(it.channel) },
                    events.filterCast<TextChannelDeleteEvent>().map { it.jda.guilds.flatMap { it.textChannels }.filter { g -> g.id != it.channel.id } }
            )

    fun textChannels(guild: Guild): Observable<List<TextChannel>> =
            Observable.mergeArray(
                    Observable.defer { Observable.just(guild.textChannels) },
                    events.filterCast<TextChannelCreateEvent>().filter { it.guild == guild }.map { guild.textChannels.filter { g -> g.id != it.channel.id } + listOf(it.channel) },
                    events.filterCast<TextChannelDeleteEvent>().filter { it.guild == guild }.map { guild.textChannels.filter { g -> g.id != it.channel.id } }
            )

    fun voiceChannels(guild: Guild): Observable<List<VoiceChannel>> =
            Observable.mergeArray(
                    Observable.defer { Observable.just(guild.voiceChannels) },
                    events.filterCast<VoiceChannelCreateEvent>().filter { it.guild == guild }.map { guild.voiceChannels.filter { g -> g.id != it.channel.id } + listOf(it.channel) },
                    events.filterCast<VoiceChannelDeleteEvent>().filter { it.guild == guild }.map { guild.voiceChannels.filter { g -> g.id != it.channel.id } }
            )

    override fun onEvent(event: Event) {
        jda = event.jda
        (events as PublishSubject<Event>).onNext(event)
    }

}