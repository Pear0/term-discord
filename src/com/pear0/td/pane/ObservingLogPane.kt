package com.pear0.td.pane

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by william on 3/22/17.
 */
class ObservingLogPane : LogPane() {

    var disposable: Disposable? = null

    fun setObservable(obs: Observable<String>) {
        disposable?.dispose()
        disposable = obs.observeOn(Schedulers.io()).subscribe { append(it) }
    }

}