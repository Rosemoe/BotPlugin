package io.github.rosemoe.botPlugin

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import java.util.concurrent.Future

fun GroupMessageEvent.sendMessage(msg: Message) : MessageReceipt<Group> {
    return runBlocking(group.coroutineContext) {
        group.sendMessage(msg)
    }
}

fun GroupMessageEvent.sendMessage(msg: String) : MessageReceipt<Group> {
    return runBlocking(group.coroutineContext) {
        group.sendMessage(msg)
    }
}

fun GroupMessageEvent.sendGroupMsg(msg: String) : MessageReceipt<Group> {
    var receipt : MessageReceipt<Group>
    runBlocking(group.coroutineContext) {
        receipt = group.sendMessage(msg)
    }
    return receipt
}

fun GroupMessageEvent.sendGroupMsgAsync(msg: String): Future<MessageReceipt<Contact>> {
    return group.__sendMessageAsyncForJava__(msg)
}

fun GroupMessageEvent.sendGroupMsgAsync(msg: Message): Future<MessageReceipt<Contact>> {
    return group.__sendMessageAsyncForJava__(msg)
}

private fun getSimpleTrace(e: Throwable, s: StringBuilder): StringBuilder {
    s.append(e.javaClass.name)
    e.message?.let {
        s.append(":").append(it)
        e
    }?.cause?.also {
        s.append("\n原因:")
        getSimpleTrace(it, s)
    }
    return s
}

fun sendErrorTrace(e: Throwable, gpMsg: GroupMessageEvent) {
    sendErrorTrace(e, gpMsg.group)
}

fun sendErrorTrace(e: Throwable, group: Group) {
    if (e is ThreadDeath) {
        return
    }
    runBlocking(group.coroutineContext) {
        group.sendMessage(getSimpleTrace(e, StringBuilder("错误:")).toString())
    }
}