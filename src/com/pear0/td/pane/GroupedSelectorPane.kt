package com.pear0.td.pane

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.graphics.TextGraphics
import java.util.*

/**
 * Created by william on 3/22/17.
 */
open class GroupedSelectorPane : Pane() {
    data class Entry(val id: String, val name: String)

    var groups = LinkedHashMap<Entry, List<Entry>>()
    var index = 0
        private set

    var scrollOffset = 0

    override var needsRedraw = true

    fun resolve(): Pair<Entry, Entry>? {
        var offset = 0
        for ((group, list) in groups) {
            if (index - offset >= list.size) {
                offset += list.size
            } else {
                return Pair(group, list[index - offset])
            }
        }
        return null
    }

    fun select(id: String) {

        var offset = 0
        for ((group, list) in groups) {
            if (group.id == id) {
                index = offset
                return
            }

            for ((id1) in list) {
                if (id1 == id) {
                    index = offset
                    return
                }
                offset++
            }
        }
    }

    open fun formatEntry(entry: Entry): String = entry.name

    override fun draw(g: TextGraphics) {
        super.draw(g)

        var row = -scrollOffset

        val selected = resolve()

        for ((group, list) in groups) {
            if (selected?.first == group) {
                g.putString(0, row++, formatEntry(group), SGR.BOLD)


                for (item in list) {
                    if (item == selected.second) {
                        if (row >= g.size.rows / 2 + 1) {
                            scrollOffset++
                            draw(g)
                            return
                        }else if (row < g.size.rows / 2 - 1 && scrollOffset > 0) {
                            scrollOffset--
                            if (scrollOffset < 0) scrollOffset = 0
                            draw(g)
                            return
                        }

                        g.putString(0, row++, "  " + formatEntry(item), SGR.BOLD)

                    } else {
                        g.putString(0, row++, "  " + formatEntry(item))
                    }
                }

            } else {
                g.putString(0, row++, formatEntry(group))
            }
        }
    }

    fun move(amt: Int) {
        index += amt
        if (index < 0) index = 0
        val totalSize = groups.map { it.value.size }.sum()
        if (totalSize in 1..index) index = totalSize - 1
    }

}