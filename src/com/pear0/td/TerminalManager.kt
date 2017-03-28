package com.pear0.td

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import com.googlecode.lanterna.terminal.TerminalFactory
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.awt.Font

/**
 * Created by william on 3/24/17.
 */
class TerminalManager {
    companion object {

        private fun selectFonts(size: Float): Array<Font> {

            var fonts = AWTTerminalFontConfiguration::class.java.getDeclaredMethod("selectDefaultFont").let {
                it.isAccessible = true
                @Suppress("UNCHECKED_CAST")
                it.invoke(null) as Array<Font>
            }

            fonts = AWTTerminalFontConfiguration.filterMonospaced(*fonts)

            fonts = (listOf(Font("Fira Mono", Font.PLAIN, 12)) + fonts).filterNotNull().map { it.deriveFont(size) }.toTypedArray()

            return fonts
        }

        private fun createTerminalFactory(): TerminalFactory {

            val terminalFontConfiguration = SwingTerminalFontConfiguration.newInstance(*selectFonts(12f))

            return DefaultTerminalFactory()
                    .setTerminalEmulatorFontConfiguration(terminalFontConfiguration)
        }

    }

    val terminal: Terminal = createTerminalFactory().createTerminal()
    val screen = TerminalScreen(terminal)

    init {
        screen.startScreen()


    }

    val keyStream: Observable<KeyStroke> = Observable
            .create<KeyStroke> { e ->
                while (!e.isDisposed) {
                    e.onNext(terminal.readInput())
                }
            }
            .subscribeOn(Schedulers.io())
            .share()



}