package io.github.rosemoe.miraiPlugin.v2

import io.github.rosemoe.util.IntPair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.coroutines.CoroutineContext

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