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

    var msgOnLeaveFormat by value("\$nick (\$id) 离开了我们...")

    var repeatDetectGroups: MutableList<Long> by value()

    var messageRecordSize by value(12)

    var messageDuplicateLimit by value(6)

}