package com.pear0.td

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import net.dv8tion.jda.core.entities.ISnowflake
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.TextChannel
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import org.apache.commons.collections4.map.ListOrderedMap
import java.util.*

/**
 * Created by William on 3/24/2017.
 */
object HistoryManager {
    class History internal constructor(val channel: MessageChannel) {
        private val messages = Collections.synchronizedSortedSet(TreeSet<Message>({ a, b -> a.id.compareTo(b.id) }))

        private val recentHistorySubject = ReplaySubject.createWithSize<Message>(500)

        init {

            Observable.concatArray(
                    // Retrieve some history
                    channel.history
                            .retrievePast(100)
                            .asSingle()
                            .map { it.reversed() }
                            .flattenAsObservable { it },

                    // Get all new history
                    TermDiscord.eventStream
                            .filter { it is MessageReceivedEvent }
                            .cast(MessageReceivedEvent::class.java)
                            .filter { it.channel.id == channel.id }
                            .map { it.message }
            )
                    .subscribeOn(Schedulers.io())
                    .subscribe { messages.add(it); recentHistorySubject.onNext(it) }
        }

        fun recentHistory(): Observable<Message> = recentHistorySubject

    }

    private val historyMap = HashMap<DiscordId, History>()

    fun getHistory(channel: MessageChannel) = historyMap.getOrPut(channel.id) { History(channel) }


}
