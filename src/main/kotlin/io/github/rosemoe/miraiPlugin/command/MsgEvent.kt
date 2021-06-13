package io.github.rosemoe.miraiPlugin.command

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource

class MsgEvent constructor(val event: MessageEvent, val restContent: String, val optionStates: OptionStates) {

    val sender: User
        get() = event.sender

    val source: MessageSource
        get() = event.source

    val message: MessageChain
        get() = event.message

    suspend fun uploadImage(res: ExternalResource) : Image {
        return sender.uploadImage(res)
    }

    suspend fun GroupMessageEvent.sendMessage(reply: Message): MessageReceipt<Group> {
        return group.sendMessage(reply)
    }

    suspend fun GroupMessageEvent.sendMessage(reply: String): MessageReceipt<Group> = sendMessage(PlainText(reply))

    fun GroupMessageEvent.sendMessageAsync(reply: Message) {
        RosemoePlugin.pluginLaunch {
            group.sendMessage(reply)
        }
    }

    fun GroupMessageEvent.sendMessageAsync(reply: String) {
        RosemoePlugin.pluginLaunch {
            group.sendMessage(reply)
        }
    }

}