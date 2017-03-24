package com.pear0.td

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by william on 3/24/17.
 */
class TerminalManager {


    val terminal: Terminal = DefaultTerminalFactory().createTerminal()
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