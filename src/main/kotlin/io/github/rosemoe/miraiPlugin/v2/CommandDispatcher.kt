package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import java.util.*

class CommandDispatcher {

    private val root = Node()

    var prefix = "/"

    private fun dfsRegister(
        depth: Int,
        aliasListMap: Array<Array<String>>,
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
                next.action = action
            } else {
                dfsRegister(depth + 1, aliasListMap, next, action)
            }
        }
    }

    fun register(path: String, action: (GroupMessageEvent,String) -> Unit) {
        register(path, object : Action {
            override fun invoke(event: GroupMessageEvent, restContent: String) {
                action(event, restContent)
            }
        })
    }

    fun register(path: String, action: (GroupMessageEvent) -> Unit) {
        register(path, object : Action {
            override fun invoke(event: GroupMessageEvent, restContent: String) {
                action(event)
            }
        })
    }

    private fun register(path: String, action: Action) {
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
        dfsRegister(0, aliasForEach, root, action)
    }

    private fun toString(msg: Message, sb: StringBuilder) {
        if (msg is MessageSource) {
            return
        }
        if (msg is MessageChain) {
            for (subMsg in msg) {
                toString(subMsg, sb)
            }
        } else {
            sb.append(msg.toString())
        }
    }

    @Throws(Throwable::class)
    fun dispatch(event: GroupMessageEvent) {
        var text = StringBuilder().also{
            toString(event.message, it)
        }.toString()
        if (text.startsWith(prefix)) {
            text = text.substring(prefix.length)
            var regionStart = getNextWordStart(0, text)
            var regionEnd = getWordEnd(regionStart, text)
            var node = root
            while (regionStart < regionEnd) {
                val name = text.substring(regionStart, regionEnd)
                val next = node.children[name]
                if (next == null) {
                    node.action?.invoke(event, text.substring(regionStart))
                    break
                } else {
                    node = next
                }
                regionStart = getNextWordStart(regionEnd, text)
                regionEnd = getWordEnd(regionStart, text)
            }
            if (regionStart >= regionEnd) {
                node.action?.invoke(event, "")
            }
        }
    }

    private class Node {
        var children: MutableMap<String, Node> = HashMap()
        var action: Action? = null
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