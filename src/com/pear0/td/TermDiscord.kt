package com.pear0.td

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.pear0.td.pane.GroupedSelectorPane
import com.pear0.td.pane.LinearLayoutPane
import com.pear0.td.pane.ObservingLogPane
import com.pear0.td.pane.Pane
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File

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

        val layout = LinearLayoutPane()
        layout.orientation = LinearLayoutPane.Orientation.HORIZONTAL

        layout.children.add(guilds)

        layout.children.add(LinearLayoutPane().apply {
            orientation = LinearLayoutPane.Orientation.VERTICAL

            this.children.add(object : Pane() {
                override val isDirty: Boolean
                    get() = true

                override fun draw(g: TextGraphics) {
                    g.fill('@')
                }
            })

            this.children.add(log)
        })

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

                        //jda.getTextChannelById(pair.second.id).history.

                        log.clear()
                        log.setObservable(eventStream
                                .filter { it is MessageReceivedEvent }
                                .cast(MessageReceivedEvent::class.java)
                                .filter { it.channel.id == pair.second.id }
                                .map { "${it.author.name}: ${it.message.strippedContent}" })

                    }
                }
            }

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