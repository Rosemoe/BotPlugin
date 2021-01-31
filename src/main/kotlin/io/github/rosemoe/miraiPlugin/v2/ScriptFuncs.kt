package io.github.rosemoe.miraiPlugin.v2

import io.github.rosemoe.miraiPlugin.v2.RosemoePlugin.sendBack
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.At

@Suppress("unused")
class ScriptFuncs(val event : GroupMessageEvent) {

    fun send(content: String) {
        runBlocking(RosemoePlugin.coroutineContext) {
            event.sendBack(MiraiCode.deserializeMiraiCode(content, event.group))
        }
    }

    fun mute(content: String, duration: Int) {
        runBlocking(RosemoePlugin.coroutineContext) {
            val msg = MiraiCode.deserializeMiraiCode(content, event.group)
            val target = msg.get(0)
            if (target is At) {
                event.group.getMemberOrFail(target.target).mute(duration)
            } else {
                event.group.getMemberOrFail(content.toLong()).mute(duration)
            }
        }
    }

    fun unmute(content: String) {
        runBlocking(RosemoePlugin.coroutineContext) {
            val msg = MiraiCode.deserializeMiraiCode(content, event.group)
            val target = msg.get(0)
            if (target is At) {
                event.group.getMemberOrFail(target.target).unmute()
            } else {
                event.group.getMemberOrFail(content.toLong()).unmute()
            }
        }
    }

    fun nudge(content: String) {
        runBlocking(RosemoePlugin.coroutineContext) {
            val msg = MiraiCode.deserializeMiraiCode(content, event.group)
            val target = msg.get(0)
            val member = if (target is At) {
                event.group.getMemberOrFail(target.target)
            } else {
                event.group.getMemberOrFail(content.toLong())
            }
            member.sendNudge(MemberNudge(member))
        }
    }

}