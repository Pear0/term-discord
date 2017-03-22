package com.pear0.td

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.pear0.td.pane.LinearLayoutPane
import com.pear0.td.pane.LogPane
import com.pear0.td.pane.Pane
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.InterfacedEventManager
import net.dv8tion.jda.core.hooks.ListenerAdapter

/**
 * Created by william on 3/21/17.
 */
object TermDiscord : ListenerAdapter() {

    val log = LogPane()

    fun start(args: Array<String>) {

        val jda = JDABuilder(AccountType.CLIENT).setToken(args[0]).addListener(this).buildBlocking()


        println(jda.guilds.map { it.name })

        val terminal = DefaultTerminalFactory().createTerminal()
        val screen = TerminalScreen(terminal)
        screen.startScreen()

        //screen.newTextGraphics().putString(0, 15, "Hello World")

        val layout = LinearLayoutPane()
        layout.orientation = LinearLayoutPane.Orientation.VERTICAL

        layout.children.add(object : Pane() {
            override val isDirty: Boolean
                get() = true

            override fun draw(g: TextGraphics) {
                g.fill('@')
            }
        })

        layout.children.add(log)

        while (true) {
            layout.draw(screen.newTextGraphics())

            screen.refresh()

            Thread.sleep(1)
        }

    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        log.append("[${event.channel.name}] ${event.author.name}: ${event.message.strippedContent}")
    }

    override fun onGenericEvent(event: Event?) {
        //log.append("$event\n")
        //t.toString()
        println(event)
    }
}