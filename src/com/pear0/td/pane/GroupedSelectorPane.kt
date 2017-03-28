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

    var selectedGroupId: Entry? = null
        private set
    var selectedItemId: Entry? = null
        private set

    var scrollOffset = 0

    override var needsRedraw = true

    fun resolve(): Pair<Entry, Entry>? {
        fixSelectedIds()
        return Pair(selectedGroupId ?: return null, selectedItemId ?: return null)
    }

    fun select(id: String) {
        for ((group, list) in groups) {

            for (item in list) {
                if (item.id == id) {
                    selectedGroupId = group
                    selectedItemId = item
                    return
                }

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

    private fun fixSelectedIds() {
        if (selectedGroupId == null || selectedGroupId!! !in groups.keys) {
            selectedGroupId = groups.keys.firstOrNull()
        }

        if (selectedGroupId != null && (selectedItemId == null || selectedItemId!! !in groups[selectedGroupId!!]!!)) {
            selectedItemId = groups[selectedGroupId!!]!!.firstOrNull()
        } else if (selectedGroupId == null) {
            selectedItemId = null
        }
    }

    fun move(amt: Int) {
        fixSelectedIds()

        if (amt > 0) repeat(amt) {
            val list = groups[selectedGroupId]!!

            val index = selectedItemId?.let { list.indexOf(it) }

            if (index == null || index == list.lastIndex) {
                val groupList = groups.keys.toList()

                val nextGroupIndex = groupList.indexOf(selectedGroupId!!) + 1

                if (nextGroupIndex in groupList.indices) {
                    selectedGroupId = groupList[nextGroupIndex]
                    selectedItemId = null
                }
            } else {
                selectedItemId = list[index + 1]
            }
        }

        if (amt < 0) repeat(-amt) {
            val list = groups[selectedGroupId]!!

            val index = selectedItemId?.let { list.indexOf(it) }

            if (index == null || index <= 0) {
                val groupList = groups.keys.toList()

                val prevGroupIndex = groupList.indexOf(selectedGroupId!!) - 1

                if (prevGroupIndex in groupList.indices) {
                    selectedGroupId = groupList[prevGroupIndex]
                    selectedItemId = groups[selectedGroupId!!]?.lastOrNull()
                }

            } else {
                selectedItemId = list[index - 1]
            }
        }

    }

}