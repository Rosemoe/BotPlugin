/*
 *     RosemoeBotPlugin
 *     Copyright (C) 2020-2021  Rosemoe
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.rosemoe.miraiPlugin.command

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import java.util.HashMap
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.jvmErasure

class CommandDispatcher constructor(
    private val permissionChecker: CommandPermissionChecker,
    override val coroutineContext: CoroutineContext
) : CoroutineScope {

    var prefix = "/"

    private val commands: MutableList<Command> = mutableListOf()
    private val commandTree: MutableMap<Command, Node> = mutableMapOf()
    private val commandFallback = mutableMapOf<Command, KFunction<*>>()

    fun register(vararg commands: Command) {
        commands.forEach {
            register(command = it)
        }
    }

    fun register(command: Command) {
        if (!commands.contains(command)) {
            commands.add(command)
        }
        val node = Node()
        //println("Reg: $command")
        command::class.memberFunctions.forEach { func ->
            func.targetAnnotation()?.let {
                if ((func.visibility == null || func.visibility == KVisibility.PUBLIC) && func.parameters.size == 2 && func.parameterClassAt(
                        1
                    ).qualifiedName == "io.github.rosemoe.miraiPlugin.command.MsgEvent"
                ) {
                    //println("Register method")
                    if (it.path == "") {
                        commandFallback[command] = func
                    } else {
                        register(node, it.path, func)
                    }
                } else {
                    //println("Func: ${func} ${func.parameters.size} ${if(func.parameters.size > 1) func.parameterClassAt(1) else null}")
                }
            }
        }
        commandTree[command] = node
    }

    fun dispatch(event: MessageEvent) {
        var text = StringBuilder().also {
            toString(event.message, it)
        }.toString()
        if (text.startsWith(prefix)) {
            text = text.substring(prefix.length)
            var regionStart = getNextWordStart(0, text)
            var regionEnd = getWordEnd(regionStart, text)
            var state = 0
            var node: Node? = null
            val optionStates = OptionStates()
            var fallback: KFunction<*>? = null
            lateinit var pendingOption: String
            lateinit var options: Array<Option>
            lateinit var command: Command
            while (regionStart < regionEnd) {
                val name = text.substring(regionStart, regionEnd)
                when (state) {
                    0 -> {
                        //println("State0: ${name}, ${node}")
                        // State 0: Find matching command
                        commands.forEach {
                            //  Check name and permissions
                            if (it.description.name.contains(name) && permissionCheck(
                                    event,
                                    it.description.permission,
                                    it
                                )
                            ) {
                                node = commandTree[it]!!
                                options = it.description.options
                                fallback = commandFallback[it]
                                command = it
                                //println("Match: ${it}, ${fallback}")
                                return@forEach
                            }
                        }
                        if (node == null) {
                            // No matching command
                            return
                        }
                        state = 1
                    }
                    1 -> {
                        // State 1: Collect options
                        if (name.startsWith("-")) {
                            val optionName = name.substring(1)
                            var notFound = true
                            options.forEach {
                                if (it.name == optionName) {
                                    // Option matches
                                    if (it.hasArgument) {
                                        // Switch to get option argument
                                        pendingOption = it.name
                                        state = 3
                                    } else {
                                        // Just set flag
                                        optionStates.optionStates[optionName] = ""
                                    }
                                    notFound = false
                                    return@forEach
                                }
                            }
                            // No option matches this name, switch to command types
                            if (notFound) {
                                state = 2
                                continue
                            }
                        } else {
                            // Not a valid option statement, switch to command types
                            state = 2
                            continue
                        }
                    }
                    2 -> {
                        // State 2: Start search branches
                        //println("State2: ${name}, ${node} ${node?.action}")
                        val next = node!!.children[name]
                        //println("Next: ${next}")
                        if (next == null) {
                            // Terminal node
                            launch(coroutineContext) {
                                //println("Call1: ${if (node!!.action == null) fallback else node!!.action}")
                                (if (node!!.action == null) fallback else node!!.action)?.callSuspend(
                                    command,
                                    MsgEvent(
                                        event,
                                        text.substring(regionStart),
                                        optionStates
                                    )
                                )
                            }
                            break
                        } else {
                            node = next
                        }
                    }
                    3 -> {
                        // State 3: Seek for option argument
                        optionStates.optionStates[pendingOption] = name
                        state = 1
                    }
                }
                regionStart = getNextWordStart(regionEnd, text)
                regionEnd = getWordEnd(regionStart, text)
            }
            if ((state == 2 || state == 1) && regionStart >= regionEnd) {
                launch(coroutineContext) {
                    //println("Call2: ${if (node!!.action == null) fallback else node!!.action}")
                    (if (node!!.action == null) fallback else node!!.action)?.callSuspend(
                        command,
                        MsgEvent(
                            event,
                            "",
                            optionStates
                        )
                    )
                }
            }
        }
    }

    private fun permissionCheck(event: MessageEvent, checker: Permission, command: Command): Boolean {
        return checker.isGranted(
            when (event) {
                is GroupMessageEvent -> SessionType.GROUP
                is GroupTempMessageEvent -> SessionType.TEMP
                is FriendMessageEvent -> SessionType.FRIEND
                else -> SessionType.UNKNOWN
            },
            if (event is GroupMessageEvent) event.sender.permission else null,
            RosemoePlugin.config.managers.contains(event.sender.id)
        ) &&
                permissionChecker.shouldRunCommand(
                    event.sender,
                    command,
                    if (event is GroupMessageEvent) event.group.id else 0
                )

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
            subs[index].split(",").toTypedArray()
        }
        dfsRegister(0, aliasForEach, root, action)
    }

    private fun <T> KFunction<T>.parameterClassAt(index: Int): KClass<*> {
        return parameters[index].type.jvmErasure
    }

    private fun <T> KFunction<T>.targetAnnotation(): CommandTriggerPath? {
        annotations.forEach {
            if (it is CommandTriggerPath) {
                return it
            }
        }
        return null
    }

    private class Node {
        var children: MutableMap<String, Node> = HashMap()
        var action: KFunction<*>? = null
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