package io.github.rosemoe.miraiPlugin.v2
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.code.MiraiCode
import java.io.Writer

class GroupWriter(val group: Group) : Writer() {

    private val candidate = StringBuilder()

    override fun close() {
        flush()
    }

    override fun flush() {
        if (candidate.isEmpty()) {
            return
        }
        runBlocking(RosemoePlugin.coroutineContext) {
            group.sendMessage(MiraiCode.deserializeMiraiCode(candidate.toString(), group))
            candidate.setLength(0)
        }
    }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        candidate.append(cbuf, off, len)
        var send = false
        for (i in off..len+off) {
            val ch = cbuf[i]
            if (ch == '\r' || ch == '\n') {
                send = true
                break
            }
        }
        if (send) {
            flush()
        }
    }


}