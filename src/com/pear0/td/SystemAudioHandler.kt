package com.pear0.td

import io.reactivex.schedulers.Schedulers
import net.dv8tion.jda.core.audio.AudioReceiveHandler
import net.dv8tion.jda.core.audio.AudioSendHandler
import net.dv8tion.jda.core.audio.CombinedAudio
import net.dv8tion.jda.core.audio.UserAudio
import java.io.PipedInputStream
import java.io.PipedOutputStream
import javax.sound.sampled.*

/**
 * Created by william on 3/29/17.
 */
class SystemAudioHandler : AudioSendHandler, AudioReceiveHandler {

    val micLine: TargetDataLine
    val sendBuffer = ByteArray(2 * 48000 / 50)

    val clip: Clip = AudioSystem.getClip()
    val audioOut = PipedOutputStream()

    init {

        val micInfo = DataLine.Info(TargetDataLine::class.java, AudioSendHandler.INPUT_FORMAT)

        if (!AudioSystem.isLineSupported(micInfo)) {
            TODO()
        }

        try {
            micLine = AudioSystem.getLine(micInfo) as TargetDataLine
            micLine.open(AudioSendHandler.INPUT_FORMAT)
        }catch (e: LineUnavailableException) {
            throw e
        }


        Schedulers.io().scheduleDirect {
            val audioIn = PipedInputStream(audioOut)
            clip.open(AudioInputStream(audioIn, AudioReceiveHandler.OUTPUT_FORMAT, AudioSystem.NOT_SPECIFIED.toLong()))
            clip.start()
        }

    }

    override fun provide20MsAudio(): ByteArray {

        var offset = 0

        while (offset != sendBuffer.size) {
            offset += micLine.read(sendBuffer, offset, sendBuffer.size - offset)
        }

        return sendBuffer
    }

    override fun canProvide(): Boolean = true

    override fun canReceiveUser() = false

    override fun handleCombinedAudio(combinedAudio: CombinedAudio) {
        audioOut.write(combinedAudio.getAudioData(1.0))
    }

    override fun handleUserAudio(userAudio: UserAudio) {
        TODO("not implemented")
    }

    override fun canReceiveCombined() = true

}