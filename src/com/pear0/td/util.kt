package com.pear0.td

import com.googlecode.lanterna.TextCharacter
import io.reactivex.Single
import net.dv8tion.jda.core.requests.RestAction

/**
 * Created by william on 3/23/17.
 */

inline fun <T> lambda(func: () -> T): T = func()

typealias DiscordId = String

typealias TerminalString = Array<TextCharacter>

fun <T> RestAction<T>.asSingle(): Single<T> = Single.defer { Single.fromFuture(this.submit()) }
