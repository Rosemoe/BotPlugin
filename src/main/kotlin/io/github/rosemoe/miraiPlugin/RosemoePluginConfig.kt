package io.github.rosemoe.miraiPlugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * Config for [RosemoePlugin]
 */
object RosemoePluginConfig : AutoSavePluginConfig("PluginConfig") {

    /**
     * Top-level users for User Access Control
     */
    var managers: MutableList<Long> by value()

    /**
     * Switches for modules and other functions
     */
    var states: MutableMap<String, Boolean> by value()

    /**
     * Prefix in group for commands outside
     */
    var commandPrefix by value("/")

    /**
     * List of ignored groups ids
     */
    var darkListGroups: MutableList<Long> by value()

    /**
     * Delay for image recalling
     */
    var imageRecallDelay by value(60000L)

    /**
     * Min interval for recalling a message
     */
    var recallMinPeriod by value(180L)

    /**
     * Max Image count for batch image sender (In one request)
     */
    var maxImageRequestCount by value(16)

    var proxyEnabled by value(false)

    var proxyType by value("http")

    var proxyAddress by value("127.0.0.1")

    var proxyPort by value(1080)

    var allowR18ImageInPixiv by value(false)

    var repeatFactor by value(0.05)

    var msgOnJoinFormat by value("欢迎\$nick加入本群~")

    var msgOnLeaveFormat by value("\$nick (\$id) 怎么溜号了，真你妈的怪啊！！！")

}