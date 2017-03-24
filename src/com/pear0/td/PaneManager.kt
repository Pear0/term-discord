package com.pear0.td

import com.pear0.td.pane.Pane
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by william on 3/24/17.
 */

class PaneManager {
    companion object {
        private val managerMap = Collections.synchronizedMap(WeakHashMap<Pane, PaneManager>())

        fun findManager(root: Pane): PaneManager? = managerMap[root]
    }

    var root: Pane? = null
        set(value) {
            if (field != null) managerMap.remove(field)
            field = value
            if (field != null) managerMap[field] = this
        }

    private var focusedElementRef = WeakReference<Pane?>(null)

    fun setFocus(pane: Pane?) {
        if (pane == null) {
            focusedElementRef.get()?.onUnfocused()
            focusedElementRef = WeakReference(null)
            return
        }

        if (pane.root != root) {
            throw IllegalArgumentException("Cannot focus a Pane who's not a descendant of this manager's root.")
        }

        focusedElementRef.get()?.onUnfocused()
        focusedElementRef = WeakReference(pane)
        pane.onFocused()
    }

    fun hasFocus(pane: Pane) = focusedElementRef.get() == pane


}
