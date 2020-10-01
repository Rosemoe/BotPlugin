package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

/**
 * Config for [RosemoePlugin]
 */
object RosemoePluginConfig : AutoSavePluginConfig() {

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
    var commandPrefix: String by value()

    /**
     * Paths for images
     */
    var imagePathList: MutableList<String> by value()

    /**
     * List of ignored groups ids
     */
    var darkListGroups: MutableList<Long> by value()

    /**
     * Delay for image recalling
     */
    var imageRecallDelay: Long by value()

    /**
     * Min interval for recalling a message
     */
    var recallMinPeriod: Long by value()

    /**
     * Max Image count for batch image sender (In one request)
     */
    var maxImageRequestCount: Int by value()

    var proxyEnabled: Boolean by value()

    var proxyType:String by value()

    var proxyAddress: String by value()

    var proxyPort:Int by value()

}