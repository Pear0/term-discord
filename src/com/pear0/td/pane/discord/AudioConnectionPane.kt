package com.pear0.td.pane.discord

import com.googlecode.lanterna.graphics.TextGraphics
import com.pear0.td.TermDiscord
import com.pear0.td.TermSchedulers
import com.pear0.td.pane.Pane
import net.dv8tion.jda.core.events.guild.voice.GenericGuildVoiceEvent
import kotlin.coroutines.experimental.buildSequence
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by william on 3/29/17.
 */
class AudioConnectionPane : Pane() {

    /*
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {}
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {}
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {}
    public void onGuildVoiceMute(GuildVoiceMuteEvent event) {}
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {}
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {}
    public void onGuildVoiceGuildDeafen(GuildVoiceGuildDeafenEvent event) {}
    public void onGuildVoiceSelfMute(GuildVoiceSelfMuteEvent event) {}
    public void onGuildVoiceSelfDeafen(GuildVoiceSelfDeafenEvent event) {}
    public void onGuildVoiceSuppress(GuildVoiceSuppressEvent event) {}
     */

    override var needsRedraw = true

    private @Volatile var description = ""

    init {

        TermDiscord.rxDiscord.voiceEvents
                .observeOn(TermSchedulers.uiThread)
                .subscribe(this::onVoiceEvent)

    }

    private fun onVoiceEvent(event: GenericGuildVoiceEvent) {

        description = buildString {
            if (event.voiceState.inVoiceChannel()) {

                if (event.voiceState.isMuted) {
                    append("[muted] ")
                }

                if (event.voiceState.isDeafened) {
                    append("[deafened] ")
                }

                append(event.voiceState.channel.name)
            }else {
                append("No Channel")
            }
        }

    }

    override fun draw(g: TextGraphics) {
        super.draw(g)

        g.putString(5, 5, description)

    }

}