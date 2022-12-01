/*
 *     RosemoeBotPlugin
 *     Copyright (C) 2020-2022  Rosemoe
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

package io.github.rosemoe.miraiPlugin.modules

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ConcurrentHashMap
import java.util.LinkedList
import java.util.Queue

private val messagePool = ConcurrentHashMap<Long, Queue<LocalMessageRecord>>()

fun RosemoePlugin.onDisableRepeatBreakingForGroup(group: Long) {
    messagePool.remove(group)
}

fun RosemoePlugin.handleGpMessageForRepeating(event: GroupMessageEvent) {
    if (!config.repeatDetectGroups.contains(event.group.id)) {
        return
    }
    val groupMsgPool = messagePool.computeIfAbsent(event.group.id) { _ ->
        return@computeIfAbsent LinkedList<LocalMessageRecord>()
    }
    if (config.messageRecordSize <= groupMsgPool.size) {
        groupMsgPool.poll()
    }
    groupMsgPool.offer(LocalMessageRecord(event.sender.id, event.message.contentToString()))
    var msgToDel: String? = null
    if (groupMsgPool.size >= config.messageDuplicateLimit) {
        // Check duplicated messages
        for (msg in groupMsgPool) {
            val filtered = groupMsgPool.filter { msg.messageText == it.messageText }
            if (filtered.size >= config.messageDuplicateLimit) {
                val luckyDog = filtered.random().senderId
                event.sendAsync(messageChainOf(PlainText("幸运复读群员："), event.group[luckyDog]?.at() ?: PlainText("@$luckyDog")))
                msgToDel = msg.messageText
                break
            }
        }
    }
    msgToDel?.let {
        groupMsgPool.removeIf { it.messageText == msgToDel }
    }
}

private data class LocalMessageRecord(
    val senderId: Long,
    val messageText: String
)