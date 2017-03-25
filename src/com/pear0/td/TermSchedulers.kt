package com.pear0.td

import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * Created by william on 3/24/17.
 */

object TermSchedulers {

    val uiThread: Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())

}
