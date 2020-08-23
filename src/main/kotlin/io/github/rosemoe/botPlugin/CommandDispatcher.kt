package io.github.rosemoe.botPlugin

import net.mamoe.mirai.message.GroupMessageEvent
import java.lang.reflect.Method
import java.util.*

class CommandDispatcher {
    private val graphRoot: State
    private var commandPrefix: String? = null
    var target: Any? = null

    fun setCommandPrefix(commandPrefix: String) {
        this.commandPrefix = Objects.requireNonNull(commandPrefix)
    }

    private fun getPrefixLength(): Int {
        return commandPrefix!!.length
    }

    @Throws(Throwable::class)
    fun dispatch(groupMessage: GroupMessageEvent) {
        var text = groupMessage.message.toString()
        if (text.startsWith("[mirai")) {
            text = text.substring(text.indexOf("]") + 1)
        }
        if (text.startsWith(commandPrefix!!)) {
            text = text.substring(getPrefixLength())
            var regionStart = getNextWordStart(0, text)
            var regionEnd = getWordEnd(regionStart, text)
            var state = graphRoot
            while (regionStart < regionEnd) {
                val name = text.substring(regionStart, regionEnd)
                val next = state.children[name]
                if (next == null) {
                    if (state.action != null && state.asFallbackAction) {
                        val method = state.action
                        method!!.isAccessible = true

                        if (method.parameterCount == 2)
                            method.invoke(target, groupMessage, text.substring(regionStart))
                        else
                            method.invoke(target, groupMessage)

                    }
                    break
                } else {
                    state = next
                    if (state.action != null && !state.asFallbackAction) {
                        val method = state.action

                        method!!.isAccessible = true
                        if (method.parameterCount == 2)
                            method.invoke(target, groupMessage, text.substring(regionEnd))
                        else
                            method.invoke(target, groupMessage)

                        break
                    }
                }
                regionStart = getNextWordStart(regionEnd, text)
                regionEnd = getWordEnd(regionStart, text)
            }
            if (regionStart >= regionEnd) {
                if (state.action != null && state.asFallbackAction) {
                    val method = state.action
                    method!!.isAccessible = true

                    if (method.parameterCount == 2)
                        method.invoke(target, groupMessage, text.substring(regionEnd))
                    else
                        method.invoke(target, groupMessage)
                }
            }
        }
    }

    fun registerAllMethods(clazz: Class<*>) {
        val methods = clazz.methods
        for (method in methods) {
            if (method.isAnnotationPresent(CommandMethod::class.java) && checkParameters(method)) {
                registerMethod(method)
            }
        }
    }

    private fun registerMethod(method: Method) {
        if (method.isAnnotationPresent(CommandMethod::class.java) && checkParameters(method)) {
            val annotation = method.getAnnotation(CommandMethod::class.java)
            val subs: Array<String> = annotation.path.split("/").toTypedArray()
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
            dfsRegister(0, "", aliasForEach, annotation.asFallbackMethod, method)
        } else {
            throw IllegalArgumentException("check your method declaration")
        }
    }

    private fun dfsRegister(
        depth: Int,
        currentPath: String,
        aliasListMap: Array<Array<String>>,
        asFallback: Boolean,
        method: Method?
    ) {
        if (depth == aliasListMap.size - 1) {
            for (element in aliasListMap[depth]) {
                registerMethodAs(currentPath + element, asFallback, method)
            }
        } else {
            for (element in aliasListMap[depth]) {
                dfsRegister(depth + 1, "$currentPath$element/", aliasListMap, asFallback, method)
            }
        }
    }

    private fun registerMethodAs(fullPath: String, asFallback: Boolean, method: Method?) {
        val path = fullPath.split("/").toTypedArray()
        var currentNode = graphRoot
        for (name in path) {
            var nextNode = currentNode.children[name]
            if (nextNode == null) {
                nextNode = State()
                currentNode.children[name] = nextNode
            }
            currentNode = nextNode
        }
        currentNode.action = method
        currentNode.asFallbackAction = asFallback
    }

    private class State {
        var children: MutableMap<String, State> = HashMap()
        var action: Method? = null
        var asFallbackAction = false
    }

    companion object {
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

        private fun checkParameters(method: Method): Boolean {
            if (method.parameterCount <= 2) {
                val types = method.parameterTypes
                return if (method.parameterCount == 1) types[0] == GroupMessageEvent::class.java else types[0] == GroupMessageEvent::class.java && types[1] == String::class.java
            }
            return false
        }
    }

    init {
        graphRoot = State()
        setCommandPrefix("/")
    }
}