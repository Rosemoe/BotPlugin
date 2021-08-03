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
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.GroupTempMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import java.lang.IllegalArgumentException

class MsgEvent constructor(val event: MessageEvent, val restContent: String = "", val optionStates: OptionStates = OptionStates()) {

    val sender: User
        get() = event.sender

    val source: MessageSource
        get() = event.source

    val message: MessageChain
        get() = event.message

    val bot: Bot
        get() = event.bot

    val subject: Contact
        get() {
            if (event is GroupMessageEvent) {
                return event.group
            } else {
                return sender
            }
        }

    fun group() : Group {
        if (event is GroupMessageEvent) {
            return event.group
        } else {
            throw IllegalArgumentException("非群聊环境无法获取群号")
        }
    }

    fun groupOrNull() : Group? {
        if (event is GroupMessageEvent) {
            return event.group
        } else {
            return null
        }
    }

    fun groupId() : Long {
        if (event is GroupMessageEvent) {
            return event.group.id
        } else {
            throw IllegalArgumentException("非群聊环境无法获取群号")
        }
    }

    suspend fun uploadImage(res: ExternalResource) : Image {
        return subject.uploadImage(res)
    }

    suspend fun send(reply: Message): MessageReceipt<*> {
        return when(event) {
            is GroupMessageEvent -> event.group.sendMessage(reply)
            is FriendMessageEvent -> event.friend.sendMessage(reply)
            is GroupTempMessageEvent -> event.group.sendMessage(reply)
            else -> throw IllegalArgumentException("Failed to send back for event object: $event")
        }
    }

    suspend fun send(reply: String): MessageReceipt<*> = send(PlainText(reply))

    fun sendAsync(reply: Message) {
        RosemoePlugin.pluginLaunch {
            send(reply)
        }
    }

    fun sendAsync(reply: String) {
        RosemoePlugin.pluginLaunch {
            send(reply)
        }
    }

}