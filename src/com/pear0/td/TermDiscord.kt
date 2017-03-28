package com.pear0.td

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.pear0.td.action.CommandAction
import com.pear0.td.command.CommandDispatcher
import com.pear0.td.pane.*
import com.pear0.td.pane.discord.ChannelSelectorPane
import com.pear0.td.pane.discord.DiscordSelectorPane
import com.pear0.td.pane.discord.VoiceSelectorPane
import io.reactivex.subjects.PublishSubject
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.io.File
import javax.imageio.ImageIO

/**
 * Created by william on 3/21/17.
 */
object TermDiscord : ListenerAdapter() {

    private var jda_: JDA? = null

    val jda: JDA get() = jda_ ?: throw UninitializedPropertyAccessException("JDA has not been initialized!")

    val log = ObservingLogPane()

    val rxDiscord = RxDiscordAdapter()

    val eventStream: PublishSubject<Event> = PublishSubject.create<Event>()

    val terminal = TerminalManager()

    //screen.newTextGraphics().putString(0, 15, "Hello World")

    val paneManager = PaneManager()

    private fun retrieveToken(args: Array<String>): String {
        return args.getOrNull(0) ?: File("local_token.txt").let { if (it.exists()) it.readText() else null } ?: throw RuntimeException("Could not find token")
    }

    fun start(args: Array<String>) {

        jda_ = JDABuilder(AccountType.CLIENT).setToken(retrieveToken(args)).addListener(this).addListener(rxDiscord).buildBlocking()

        rxDiscord.guilds.subscribe { println(it.map { it.name }) }

        /*for (guild in jda.guilds) {
            println("$guild")
            if (guild.name != null) {
                guilds.groups.put(GroupedSelectorPane.Entry(guild.id, guild.name),
                        guild.textChannels.filter { guild.selfMember.hasPermission(it, Permission.MESSAGE_READ) }.map { GroupedSelectorPane.Entry(it.id, it.name) })
            }
        }*/

        val rick = ImageIO.read(File("rick-astley.jpg"))

        paneManager.root = StatusLayoutPane().apply {
            id = "status_layout"

            setChild(PagingLayoutPane().apply {
                id = "paging_layout"

                addChild(LinearLayoutPane().apply {
                    orientation = LinearLayoutPane.Orientation.HORIZONTAL

                    addChild(ChannelSelectorPane(rxDiscord.guilds, rxDiscord::textChannels).apply { id = "channel_selector" })

                    addChild(LinearLayoutPane().apply {
                        orientation = LinearLayoutPane.Orientation.VERTICAL

                        this.addChild(object : Pane() {
                            override var needsRedraw: Boolean = true

                            override fun draw(g: TextGraphics) {
                                g.fill('@')
                            }
                        })

                        this.addChild(log)
                    }, 2f)
                })

                addChild(LinearLayoutPane().apply {
                    orientation = LinearLayoutPane.Orientation.HORIZONTAL

                    addChild(VoiceSelectorPane(rxDiscord.guilds, rxDiscord::voiceChannels).apply { id = "voice_selector" })

                    addChild(LinearLayoutPane().apply {
                        orientation = LinearLayoutPane.Orientation.VERTICAL

                        this.addChild(object : Pane() {
                            override var needsRedraw: Boolean = true

                            override fun draw(g: TextGraphics) {
                                g.fill('@')
                            }
                        })

                        this.addChild(log)
                    }, 2f)
                })

                addChild(object : Pane() {
                    override var needsRedraw: Boolean = true

                    override fun draw(g: TextGraphics) {

                        g.drawImage(TerminalPosition.TOP_LEFT_CORNER, ImageUtils.renderASCIIArt(rick, g.size))
                        //g.fill('~')
                    }
                })

            })
        }

        getChannelsPane().selectionObservable.subscribe {
            log.clear()
            log.setObservable(HistoryManager.getHistory(jda.getTextChannelById(it))
                    .recentHistory().map { "${it.author.name}: ${it.content}" })

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
                ':' -> {
                    val action = CommandAction(CommandDispatcher.defaultDispatcher.asHandler(), displayPrefix = ":")

                    paneManager.setFocus(paneManager.root!!.findPane("status_layout"), context = action)
                }
            }


        }

        terminal.keyStream
                .observeOn(TermSchedulers.uiThread)
                .subscribe(::handleKeyTyped)



        while (true) {

            terminal.screen.doResizeIfNecessary()

            paneManager.root!!.onLayoutChanged(terminal.screen.terminalSize)
            paneManager.root!!.draw(terminal.screen.newTextGraphics())

            terminal.screen.refresh()

            Thread.sleep(1)
        }

    }

    fun getStatusPane(): StatusLayoutPane = paneManager.root!!.findPane("status_layout")!!

    fun getPagingPane(): PagingLayoutPane = paneManager.root!!.findPane("paging_layout")!!

    fun getChannelsPane(): ChannelSelectorPane = paneManager.root!!.findPane("channel_selector")!!

    fun getVoicesPane(): VoiceSelectorPane = paneManager.root!!.findPane("voice_selector")!!

    override fun onMessageReceived(event: MessageReceivedEvent) {
        //log.append("[${event.channel.name}] ${event.author.name}: ${event.message.strippedContent}")
    }

    override fun onGenericEvent(event: Event?) {
        eventStream.onNext(event)
        //log.append("$event\n")
        //t.toString()
    }


    fun shutdown() {
        jda.shutdown()
        System.exit(0)
    }

}