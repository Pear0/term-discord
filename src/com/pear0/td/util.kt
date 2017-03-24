package com.pear0.td

/**
 * Created by william on 3/23/17.
 */

inline fun <T> lambda(func: () -> T): T = func()
