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

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.toMessageChain
import java.util.*

private val random = Random()

@Synchronized
private fun nextDouble(): Double {
    return random.nextDouble()
}

fun RosemoePlugin.randomRepeat(event: GroupMessageEvent) {
    if (!isModuleEnabled("Repeat") || isDarklistGroup(event)) {
        return
    }
    if (nextDouble() < config.repeatFactor) {
        event.sendAsync(event.message.asSequence().filter { predicate -> !(predicate is MessageSource) }.toMessageChain())
    }
}
