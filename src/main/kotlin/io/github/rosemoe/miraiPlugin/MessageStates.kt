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

package io.github.rosemoe.miraiPlugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

class MessageStates {

    private val lock = ReentrantReadWriteLock()

    private val mapGroup = ConcurrentHashMap<Long, GroupMessages>()

    class GroupMessages {

        private val savedSigns = HashMap<Long, Boolean>()

        fun handleForSign(sign: Long): Boolean {
            synchronized(this) {
                if (savedSigns.containsKey(sign)) {
                    return false
                }
                savedSigns.put(sign, true)
                return true
            }
        }

        fun clear() {
            synchronized(this) {
                savedSigns.clear()
            }
        }

    }

    fun GroupMessageEvent.getSign(): Long {
        return sender.id.shl(29).or(source.time.toLong())
    }

    fun handle(event: GroupMessageEvent): Boolean {
        lock.lockRead()
        try {
            val groupMessages = mapGroup.computeIfAbsent(event.group.id) {
                GroupMessages()
            }
            return groupMessages.handleForSign(event.getSign())
        } finally {
            lock.unlockRead()
        }
    }

    fun clear() {
        lock.lockWrite()
        try {
            mapGroup.forEachValue(2) {
                it.clear()
            }
        } finally {
            lock.unlockWrite()
        }
    }

    fun launchClearer(scope: CoroutineScope): Job {
        return scope.launch {
            while (true) {
                clear()
                delay(30 * 1000) //30 seconds
            }
        }
    }

}