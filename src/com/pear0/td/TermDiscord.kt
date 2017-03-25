package com.pear0.td

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.action.ComposeMessageAction
import com.pear0.td.action.UserAction
import com.pear0.td.pane.*
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File

/**
 * Created by william on 3/21/17.
 */
object TermDiscord : ListenerAdapter() {

    private var jda_: JDA? = null

    val jda: JDA get() = jda_ ?: throw UninitializedPropertyAccessException("JDA has not been initialized!")

    val log = ObservingLogPane()
    val guilds = GroupedSelectorPane()

    val eventStream: PublishSubject<Event> = PublishSubject.create<Event>()

    private fun retrieveToken(args: Array<String>): String {
        return args.getOrNull(0) ?: File("local_token.txt").let { if (it.exists()) it.readText() else null } ?: throw RuntimeException("Could not find token")
    }

    fun start(args: Array<String>) {

        jda_ = JDABuilder(AccountType.CLIENT).setToken(retrieveToken(args)).addListener(this).buildBlocking()

        for (guild in jda.guilds) {

            guilds.groups.put(GroupedSelectorPane.Entry(guild.id, guild.name), guild.textChannels.map { GroupedSelectorPane.Entry(it.id, it.name) })

        }

        val terminal = TerminalManager()

        //screen.newTextGraphics().putString(0, 15, "Hello World")

        val paneManager = PaneManager()

        paneManager.root = StatusLayoutPane().apply {
            id = "status_layout"; setChild(lambda {
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
        })
        }

        fun handleKeyTyped(key: KeyStroke) {
            //key.character?.let { c -> paneManager.root!!.findPane<StatusLayoutPane>("status_layout")!!.let { it.status = it.status.copy(left = it.status.left + c) } }

            if (paneManager.onKeyTyped(key)) {
                return
            }


            when (key.keyType.let {
                @Suppress("IMPLICIT_CAST_TO_ANY")
                if (it == KeyType.Character) key.character else it
            }) {
                KeyType.ArrowUp, KeyType.ArrowDown -> {
                    val oldIndex = guilds.index

                    guilds.move(if (key.keyType == KeyType.ArrowUp) -1 else 1)

                    if (oldIndex != guilds.index) {
                        val pair = guilds.resolve()
                        if (pair != null) {

                            log.clear()
                            log.setObservable(HistoryManager.getHistory(jda.getTextChannelById(pair.second.id)).recentHistory().map { "${it.author.name}: ${it.strippedContent}" })

                        }
                    }
                }
                'm' -> paneManager.setFocus(paneManager.root!!.findPane("status_layout"), context =
                ComposeMessageAction(jda.getTextChannelById(guilds.resolve()!!.second.id)))

            }



        }

        terminal.keyStream
                .observeOn(TermSchedulers.uiThread)
                .subscribe(::handleKeyTyped)



        while (true) {

            paneManager.root!!.onLayoutChanged(terminal.screen.terminalSize)
            paneManager.root!!.draw(terminal.screen.newTextGraphics())

            terminal.screen.refresh()

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