package io.github.rosemoe.botPlugin

import com.rose.yuscript.YuContext
import com.rose.yuscript.YuInterpreter
import io.github.rosemoe.botPlugin.Common.monitor
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.GroupMessageEvent
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * @author Rose
 */
object YuScriptTask {
    @Volatile
    private var threadIdAlloc = 0
    private val threadMap: MutableMap<Int, WeakReference<Thread>> = ConcurrentHashMap()
    @Synchronized
    private fun nextId(): Int {
        return threadIdAlloc++
    }

    @JvmStatic
    fun runYuScriptUnlimited(gpMsg: GroupMessageEvent, code: String?) {
        val id = nextId()
        val td = Thread {
            try {
                val interpreter = YuInterpreter(gpMsg.group.id)
                interpreter.group = gpMsg.group
                interpreter.eval(code)
            } catch (e: Throwable) {
                if (!(e is InterruptedException || e.cause is InterruptedException)) {
                    gpMsg.sendGroupMsg("YuScript - Session $id 出错停止")
                    sendErrorTrace(e, gpMsg)
                } else {
                    gpMsg.sendGroupMsg("YuScript - Session $id 已停止")
                }
            }
            try {
                threadMap.remove(id)
            } catch (ignored: Throwable) {
            }
        }
        val reference = WeakReference(td)
        threadMap[id] = reference
        td.start()
        gpMsg.sendGroupMsg("已经开始执行代码,Session = $id")
    }

    @JvmStatic
    fun interruptSession(gpMsg: GroupMessageEvent, session: Int) {
        val reference = threadMap[session]
        if (reference == null) {
            gpMsg.sendGroupMsg("线程不存在或已经回收")
            return
        }
        val td = reference.get()
        if (td == null) {
            gpMsg.sendGroupMsg("线程不存在或已经回收")
            return
        }
        td.interrupt()
        gpMsg.sendGroupMsg("已经对目标发送了停止信号")
    }

    @JvmStatic
    fun runYuScript(gpMsg: GroupMessageEvent, msg: String?) {
        val local = Thread(Runnable {
            try {
                runYuScriptInternal(gpMsg.group, msg)
            } catch (e: Throwable) {
                if (e is ThreadDeath || e.cause is ThreadDeath) {
                    return@Runnable
                }
                sendErrorTrace(e, gpMsg)
            }
        })
        local.start()
        if (monitor(local, 2500)) {
            gpMsg.sendGroupMsg("时间超限!你的代码在2.5s后仍在执行.")
        }
    }

    @Throws(Throwable::class)
    internal fun runYuScriptInternal(group: Group, msg: String?) {
        val interpreter = YuInterpreter(group.id)
        interpreter.group = group
        interpreter.eval(msg)
    }

    @JvmStatic
    fun reset(gpMsg: GroupMessageEvent) {
        YuContext.clear(gpMsg.group.id)
        YuContext.clear()
        gpMsg.sendGroupMsg("全局变量和局部变量已经成功清除.")
    }
}