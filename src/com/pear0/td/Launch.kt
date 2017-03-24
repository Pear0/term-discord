package com.pear0.td


/**
 * Created by william on 3/21/17.
 */

var ARGS: Array<String> = emptyArray()
    private set

fun main(args: Array<String>) {
    ARGS = args
    TermDiscord.start(args)
}
