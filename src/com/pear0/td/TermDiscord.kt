package com.pear0.td

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.pear0.td.pane.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by william on 3/21/17.
 */
object TermDiscord : ListenerAdapter() {

    val log = ObservingLogPane()
    val guilds = GroupedSelectorPane()

    val eventStream: PublishSubject<Event> = PublishSubject.create<Event>()

    private fun retrieveToken(args: Array<String>): String {
        return args.getOrNull(0) ?: File("local_token.txt").let { if (it.exists()) it.readText() else null } ?: throw RuntimeException("Could not find token")
    }

    fun start(args: Array<String>) {

        val jda = JDABuilder(AccountType.CLIENT).setToken(retrieveToken(args)).addListener(this).buildBlocking()

        for (guild in jda.guilds) {

            guilds.groups.put(GroupedSelectorPane.Entry(guild.id, guild.name), guild.textChannels.map { GroupedSelectorPane.Entry(it.id, it.name) })

        }

        val terminal = DefaultTerminalFactory().createTerminal()
        val screen = TerminalScreen(terminal)
        screen.startScreen()

        //screen.newTextGraphics().putString(0, 15, "Hello World")

        val layout = StatusLayoutPane().apply { setChild(lambda {
            val layout = LinearLayoutPane()
            layout.orientation = LinearLayoutPane.Orientation.HORIZONTAL

            layout.addChild(guilds)

            layout.addChild(LinearLayoutPane().apply {
                orientation = LinearLayoutPane.Orientation.VERTICAL

                this.addChild(object : Pane() {
                    override var needsRedraw: Boolean = true

                    override fun draw(g: TextGraphics) {
                        g.fill('@')
                    }
                })

                this.addChild(log)
            }, 2f)

            layout
        }) }


        while (true) {
            while (true) {
                val key = terminal.pollInput() ?: break

                val oldIndex = guilds.index

                when (key.keyType) {
                    KeyType.ArrowUp -> {
                        guilds.move(-1)
                    }
                    KeyType.ArrowDown -> {
                        guilds.move(1)
                    }
                }

                if (oldIndex != guilds.index) {
                    val pair = guilds.resolve()
                    if (pair != null) {

                        val obs = Observable.concatArray(
                                Single.just(jda.getTextChannelById(pair.second.id).history)
                                        .flatMapObservable {
                                            if (it.retrievedHistory.size > 0) Observable.fromIterable(it.retrievedHistory.reversed())
                                            else {
                                                Single.create(SingleOnSubscribe<List<Message>> { e ->
                                                    it.retrievePast(100).queue {
                                                        e.onSuccess(it.reversed())
                                                    }
                                                }).flattenAsObservable { it }
                                                        .delaySubscription(150, TimeUnit.MILLISECONDS)
                                            }
                                        },
                                eventStream
                                        .filter { it is MessageReceivedEvent }
                                        .cast(MessageReceivedEvent::class.java)
                                        .filter { it.channel.id == pair.second.id }.map { it.message }
                        )

                        log.clear()
                        log.setObservable(obs.map { "${it.author.name}: ${it.strippedContent}" })

                    }
                }
            }


            layout.onLayoutChanged(screen.terminalSize)
            layout.draw(screen.newTextGraphics())

            screen.refresh()

            Thread.sleep(1)
        }

    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        //log.append("[${event.channel.name}] ${event.author.name}: ${event.message.strippedContent}")
    }

    override fun onGenericEvent(event: Event?) {
        eventStream.onNext(event)
        //log.append("$event\n")
        //t.toString()
        println(event)
    }
}