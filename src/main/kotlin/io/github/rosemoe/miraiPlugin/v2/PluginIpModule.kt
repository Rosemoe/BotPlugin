package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.event.events.GroupMessageEvent
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

fun RosemoePlugin.registerIpCommands() {
    @Throws(Throwable::class)
    fun runCommandGeneral(gpMsg: GroupMessageEvent, msg: String) {
        if (!isModuleEnabled("IpList")) {
            return
        }
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder()
        output.append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP地址:")
        for (add in address) {
            output.append('\n').append(add.hostAddress)
        }
        gpMsg.sendBackAsync(output.toString())
    }

    @Throws(Throwable::class)
    fun runCommandV4(gpMsg: GroupMessageEvent, msg: String) {
        if (!isModuleEnabled("IpList")) {
            return
        }
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder().append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP V4地址:")
        for (add in address) {
            if (add is Inet4Address) output.append('\n').append(add.getHostAddress())
        }
        gpMsg.sendBackAsync(output.toString())
    }

    @Throws(Throwable::class)
    fun runCommandV6(gpMsg: GroupMessageEvent, msg: String) {
        if (!isModuleEnabled("IpList")) {
            return
        }
        val address = InetAddress.getAllByName(msg.trim { it <= ' ' })
        val output = StringBuilder().append("查询到地址`").append(msg.trim { it <= ' ' }).append("`的所有IP V6地址:")
        for (add in address) {
            if (add is Inet6Address) output.append('\n').append(add.getHostAddress())
        }
        gpMsg.sendBackAsync(output.toString())
    }

    dispatcher.register("ipList", ::runCommandGeneral)
    dispatcher.register("ipList4", ::runCommandV4)
    dispatcher.register("ipList6", ::runCommandV6)
}