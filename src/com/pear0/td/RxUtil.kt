package com.pear0.td

import io.reactivex.Observable

/**
 * Created by william on 3/27/17.
 */

inline fun <reified T> Observable<*>.filterCast(): Observable<T> = filter { it is T }.cast(T::class.java)

