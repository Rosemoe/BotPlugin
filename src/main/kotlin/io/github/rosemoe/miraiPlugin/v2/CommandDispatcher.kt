package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.message.GroupMessageEvent
import java.util.HashMap

class CommandDispatcher {

    private val root = Node()

    var prefix = "/"

    private fun dfsRegister(
        depth: Int,
        aliasListMap: Array<Array<String>>,
        asFallback: Boolean,
        node: Node,
        action: Action
    ) {
        for (element in aliasListMap[depth]) {
            var next = node.children[element]
            if (next == null) {
                next = Node()
                node.children[element] = next
            }
            if (depth + 1 == aliasListMap.size) {
                next.asFallbackAction = asFallback
                next.action = action
            } else {
                dfsRegister(depth + 1, aliasListMap, asFallback, next, action)
            }
        }
    }

    fun register(path: String, action: (GroupMessageEvent,String) -> Unit, asFallbackAction: Boolean = false) {
        register(path, object : Action {
            override fun invoke(event: GroupMessageEvent, restContent: String) {
                action(event, restContent)
            }
        }, asFallbackAction)
    }

    fun register(path: String, action: (GroupMessageEvent) -> Unit, asFallbackAction: Boolean = false) {
        register(path, object : Action {
            override fun invoke(event: GroupMessageEvent, restContent: String) {
                action(event)
            }
        }, asFallbackAction)
    }

    private fun register(path: String, action: Action, asFallbackAction: Boolean = false) {
        val subs: Array<String> = path.split("/").toTypedArray()
        val aliasForEach = Array(subs.size) { index ->
            val origin = subs[index]
            val lt = origin.indexOf("<")
            val gt = origin.indexOf(">")
            if (lt in 0..gt && gt == origin.length - 1) {
                arrayOf(origin.substring(0, lt), origin.substring(lt + 1, gt))
            } else {
                arrayOf(origin)
            }
        }
        dfsRegister(0, aliasForEach, asFallbackAction, root, action)
    }

    @Throws(Throwable::class)
    fun dispatch(groupMessage: GroupMessageEvent) {
        var text = groupMessage.message.toString()
        if (text.startsWith("[mirai:source")) {
            text = text.substring(text.indexOf("]") + 1)
        }
        if (text.startsWith(prefix)) {
            text = text.substring(prefix.length)
            var regionStart = getNextWordStart(0, text)
            var regionEnd = getWordEnd(regionStart, text)
            var state = root
            while (regionStart < regionEnd) {
                val name = text.substring(regionStart, regionEnd)
                val next = state.children[name]
                if (next == null) {
                    if (state.action != null && state.asFallbackAction) {
                        state.action?.invoke(groupMessage, text.substring(regionStart))
                    }
                    break
                } else {
                    state = next
                    if (state.action != null && !state.asFallbackAction) {
                        state.action?.invoke(groupMessage, text.substring(regionEnd))
                        break
                    }
                }
                regionStart = getNextWordStart(regionEnd, text)
                regionEnd = getWordEnd(regionStart, text)
            }
            if (regionStart >= regionEnd) {
                if (state.action != null && state.asFallbackAction) {
                    state.action?.invoke(groupMessage, text.substring(regionEnd))
                }
            }
        }
    }

    private class Node {
        var children: MutableMap<String, Node> = HashMap()
        var action: Action? = null
        var asFallbackAction = false
    }

    interface Action {

        @Throws(Throwable::class)
        fun invoke(event : GroupMessageEvent, restContent : String)

    }

    private fun getNextWordStart(i: Int, msg: String): Int {
        var idx = i
        while (idx < msg.length && Character.isWhitespace(msg[idx])) {
            idx++
        }
        return idx
    }

    private fun getWordEnd(i: Int, msg: String): Int {
        var idx = i
        while (idx < msg.length && !Character.isWhitespace(msg[idx])) {
            idx++
        }
        return idx
    }

}