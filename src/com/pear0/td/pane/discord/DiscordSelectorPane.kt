package com.pear0.td.pane.discord

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.DiscordId
import com.pear0.td.TermDiscord
import com.pear0.td.TermSchedulers
import com.pear0.td.lambda
import com.pear0.td.pane.GroupedSelectorPane
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.TextChannel

/**
 * Created by William on 3/26/2017.
 */
abstract class DiscordSelectorPane<T>(val guildsStream: Observable<List<Guild>>, val channelObservable: (Guild) -> Observable<List<T>>) : GroupedSelectorPane() {

    private val selectionSubject = PublishSubject.create<DiscordId>()

    private var currentSelection: Pair<DiscordId, DiscordId>? = null

    val selectionObservable: Observable<DiscordId> = selectionSubject

    init {

        val channelDisposables = ArrayList<Disposable>()

        guildsStream
                .observeOn(TermSchedulers.uiThread)
                .subscribe { guilds ->

                    channelDisposables.forEach { it.dispose() }
                    channelDisposables.clear()

                    groups.clear()

                    for (guild in guilds) {

                        val guildEntry = Entry(guild.id, guild.name)

                        val disposable = channelObservable(guild)
                                .observeOn(TermSchedulers.uiThread)
                                .subscribe { channels ->
                                    groups.put(guildEntry, channels.mapNotNull { channelToEntry(guild, it) })
                                }

                        channelDisposables.add(disposable)
                    }


                }

    }

    abstract fun channelToEntry(guild: Guild, channel: T): Entry?

    fun getHighlightedGuildId() = resolve()?.first?.id

    fun getHighlightedChannelId() = resolve()?.second?.id

    override fun onFocused(context: Any?) {
        println("Focused!")
    }

    override fun onUnfocused() {
        currentSelection?.second?.let { select(it) }
    }

    fun setSelection(guild: DiscordId, channel: DiscordId) {
        currentSelection = Pair(guild, channel)
        selectionSubject.onNext(channel)
    }

    override fun formatEntry(entry: Entry): String {
        val isSelected = (currentSelection?.first == entry.id || currentSelection?.second == entry.id) && lambda {
            val r = resolve() ?: return@lambda false
            entry.id != r.first.id && entry.id != r.second.id
        }
        return (if (isSelected) "*" else "") + super.formatEntry(entry)
    }

    override fun onKeyTyped(key: KeyStroke) {

        when (key.keyType) {
            KeyType.ArrowUp -> move(-1)
            KeyType.ArrowDown -> move(1)
            KeyType.Enter -> {
                val pair = resolve()!!
                setSelection(pair.first.id, pair.second.id)

                unfocus()
            }
            else -> {
            }
        }

    }

}