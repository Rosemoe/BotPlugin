package io.github.rosemoe.botPlugin

import net.mamoe.mirai.message.GroupMessageEvent
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * @author Rose
 */
object IpTask {
    @JvmStatic
    @Throws(Throwable::class)
    fun runCommandGeneral(gpMsg: GroupMessageEvent, msg: String) {
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder()
        output.append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP地址:")
        for (add in address) {
            output.append('\n').append(add.hostAddress)
        }
        gpMsg.sendGroupMsg(output.toString())
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun runCommandV4(gpMsg: GroupMessageEvent, msg: String) {
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder().append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP V4地址:")
        for (add in address) {
            if (add is Inet4Address) output.append('\n').append(add.getHostAddress())
        }
        gpMsg.sendGroupMsg(output.toString())
    }

    @JvmStatic
    @Throws(Throwable::class)
    fun runCommandV6(gpMsg: GroupMessageEvent, msg: String) {
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder().append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP V6地址:")
        for (add in address) {
            if (add is Inet6Address) output.append('\n').append(add.getHostAddress())
        }
        gpMsg.sendGroupMsg(output.toString())
    }
}