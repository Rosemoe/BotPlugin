package io.github.rosemoe.miraiPlugin

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.toMessageChain
import java.util.*

private val random = Random()

@Synchronized
private fun nextDouble(): Double {
    return random.nextDouble()
}

fun RosemoePlugin.randomRepeat(event: GroupMessageEvent) {
    if (!isModuleEnabled("Repeat") || isDarklistGroup(event)) {
        return
    }
    if (nextDouble() < config.repeatFactor) {
        event.sendAsync(event.message.asSequence().filter { predicate -> !(predicate is MessageSource) }.toMessageChain())
    }
}
