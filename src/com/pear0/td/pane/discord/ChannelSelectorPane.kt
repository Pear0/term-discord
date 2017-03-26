package com.pear0.td.pane.discord

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.DiscordId
import com.pear0.td.lambda
import com.pear0.td.pane.GroupedSelectorPane
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by William on 3/26/2017.
 */
class ChannelSelectorPane : GroupedSelectorPane() {

    private val selectionSubject = PublishSubject.create<DiscordId>()

    private var currentSelection: Pair<DiscordId, DiscordId>? = null

    val selectionObservable: Observable<DiscordId> = selectionSubject

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