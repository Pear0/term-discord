package com.pear0.td.pane

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.graphics.TextGraphics
import java.util.*

/**
 * Created by william on 3/22/17.
 */
class GroupedSelectorPane : Pane() {
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

    override fun draw(g: TextGraphics) {
        super.draw(g)

        var row = -scrollOffset

        val selected = resolve()

        for ((group, list) in groups) {
            if (selected?.first == group) {
                g.putString(0, row++, group.name, SGR.BOLD)


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

                        g.putString(0, row++, "  " + item.name, SGR.BOLD)

                    } else {
                        g.putString(0, row++, "  " + item.name)
                    }
                }

            } else {
                g.putString(0, row++, group.name)
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