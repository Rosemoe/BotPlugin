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
    var commandPrefix: String by value("/")

    /**
     * List of ignored groups ids
     */
    var darkListGroups: MutableList<Long> by value()

    /**
     * Delay for image recalling
     */
    var imageRecallDelay: Long by value(60000L)

    /**
     * Min interval for recalling a message
     */
    var recallMinPeriod: Long by value(180L)

    /**
     * Max Image count for batch image sender (In one request)
     */
    var maxImageRequestCount: Int by value(16)

    var proxyEnabled: Boolean by value(false)

    var proxyType: String by value("http")

    var proxyAddress: String by value("127.0.0.1")

    var proxyPort: Int by value(1080)

    var allowR18ImageInPixiv: Boolean by value(false)

    var repeatFactor: Double by value(0.05)

}