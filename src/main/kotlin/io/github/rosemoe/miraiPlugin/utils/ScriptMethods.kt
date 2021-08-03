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

package io.github.rosemoe.miraiPlugin.utils

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import io.github.rosemoe.miraiPlugin.command.MsgEvent
import io.github.rosemoe.miraiPlugin.commands.Setu.sendImageForEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.getMemberOrFail
import net.mamoe.mirai.message.action.Nudge.Companion.sendNudge
import net.mamoe.mirai.message.code.MiraiCode
import net.mamoe.mirai.message.data.At

@Suppress("unused")
class ScriptMethods(val event : MsgEvent) {

    private fun runBlocking(scope: suspend CoroutineScope.() -> Unit) {
        runBlocking(RosemoePlugin.coroutineContext, scope)
    }

    fun send(content: String) {
        runBlocking {
            event.send(MiraiCode.deserializeMiraiCode(content, event.sender))
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
            event.group().sendNudge(member(user).nudge())
        }
    }

    fun member(user: String): NormalMember {
        val msg = MiraiCode.deserializeMiraiCode(user, event.group())
        val target = msg.get(0)
        return if (target is At) {
            event.group().getMemberOrFail(target.target)
        } else {
            event.group().getMemberOrFail(user.toLong())
        }
    }

    fun muteAll() {
        event.group().settings.isMuteAll = true
    }

    fun unmuteAll() {
        event.group().settings.isMuteAll = false
    }

    fun find(keyword: String) : NormalMember? {
        event.group().members.forEach {
            if (it.nameCard.contains(keyword, true) || it.nick.contains(keyword, true)) {
                return it
            }
        }
        return null
    }

    fun findAll(keyword: String) : Array<NormalMember> {
        val list = ArrayList<NormalMember>()
        event.group().members.forEach {
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