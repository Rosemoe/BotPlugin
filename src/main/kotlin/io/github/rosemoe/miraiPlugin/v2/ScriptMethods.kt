package io.github.rosemoe.miraiPlugin.v2

import io.github.rosemoe.miraiPlugin.v2.RosemoePlugin.sendBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.At

@Suppress("unused")
class ScriptMethods(val event : GroupMessageEvent) {

    private fun runBlocking(scope: suspend CoroutineScope.() -> Unit) {
        runBlocking(RosemoePlugin.coroutineContext, scope)
    }

    fun send(content: String) {
        runBlocking {
            event.sendBack(MiraiCode.deserializeMiraiCode(content, event.group))
        }
    }

    fun mute(user: String, duration: Int) {
        runBlocking {
            member(user).mute(duration)
        }
    }

    fun unmute(user: String) {
        runBlocking {
            member(user).unmute()
        }
    }

    fun kick(user: String, msg: String) {
        runBlocking {
            member(user).kick(msg)
        }
    }

    fun nudge(user: String) {
        runBlocking {
            event.group.sendNudge(member(user).nudge())
        }
    }

    fun member(user: String): NormalMember {
        val msg = MiraiCode.deserializeMiraiCode(user, event.group)
        val target = msg.get(0)
        return if (target is At) {
            event.group.getMemberOrFail(target.target)
        } else {
            event.group.getMemberOrFail(user.toLong())
        }
    }

    fun muteAll() {
        event.group.settings.isMuteAll = true
    }

    fun unmuteAll() {
        event.group.settings.isMuteAll = false
    }

    fun find(keyword: String) : NormalMember? {
        event.group.members.forEach {
            if (it.nameCard.contains(keyword, true) || it.nick.contains(keyword, true)) {
                return it
            }
        }
        return null
    }

    fun findAll(keyword: String) : Array<NormalMember> {
        val list = ArrayList<NormalMember>()
        event.group.members.forEach {
            if (it.nameCard.contains(keyword, true) || it.nick.contains(keyword, true)) {
                list.add(it)
            }
        }
        return list.toArray(Array<NormalMember?>(0) {null})
    }

    fun gkd() {
        RosemoePlugin.sendImageForEvent(event)
    }

}