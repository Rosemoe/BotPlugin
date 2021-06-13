package io.github.rosemoe.miraiPlugin.command

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import java.util.HashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure

class CommandDispatcher {

    val prefix = "/"

    private val commands: MutableList<Command> = mutableListOf()
    private val commandTree: MutableMap<Command, Node> = mutableMapOf()

    fun register(command: Command) {
        if (!commands.contains(command)) {
            commands.add(command)
        }
        val node = Node()
        command::class.memberFunctions.forEach { func ->
            func.targetAnnotation()?.let {
                if (func.isExternal && func.parameters.size == 3 && func.parameterClassAt(0) == GroupMessageEvent::class && func.parameterClassAt(
                        1
                    ) == String::class && func.parameterClassAt(2) == OptionStates::class
                ) {
                    register(node, it.path, func)
                }
            }
        }
        commandTree.put(command, node)
    }

    fun dispatch(event: GroupMessageEvent, coroutineContext: CoroutineContext) {
        var text = StringBuilder().also {
            toString(event.message, it)
        }.toString()
        if (text.startsWith(prefix)) {
            text = text.substring(prefix.length)
            var regionStart = getNextWordStart(0, text)
            var regionEnd = getWordEnd(regionStart, text)
            var state = 0
            lateinit var command: Command
            while (regionStart < regionEnd) {
                val name = text.substring(regionStart, regionEnd)
                when (state) {
                    0 -> {
                        // State 0: Find matching command
                        commands.forEach {
                            //  Check name
                            if (it.description.name.contains(name)) {

                            }
                        }
                    }
                }
                regionStart = getNextWordStart(regionEnd, text)
                regionEnd = getWordEnd(regionStart, text)
            }

        }
    }

    private fun dfsRegister(
        depth: Int,
        aliasListMap: Array<Array<String>>,
        node: Node,
        action: KFunction<*>
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

    private fun register(root: Node, path: String, action: KFunction<*>) {
        val subs: Array<String> = path.split("/").toTypedArray()
        val aliasForEach = Array(subs.size) { index ->
            subs[index].split("/").toTypedArray()
        }
        dfsRegister(0, aliasForEach, root, action)
    }

    private fun <T> KFunction<T>.parameterClassAt(index: Int): KClass<*> {
        return parameters[index].type.jvmErasure
    }

    private fun <T> KFunction<T>.targetAnnotation(): CommandTarget? {
        annotations.forEach {
            if (it is CommandTarget) {
                return it
            }
        }
        return null
    }

    private class Node {
        var children: MutableMap<String, Node> = HashMap()
        lateinit var action: KFunction<*>
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

}