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

package io.github.rosemoe.miraiPlugin.commands

import io.github.rosemoe.miraiPlugin.*
import io.github.rosemoe.miraiPlugin.command.*
import io.github.rosemoe.miraiPlugin.commands.Setu.initializeImageList

object Sources: Command(
    CommandDescription(
        arrayOf("sources"),  Permissions.GROUP.withManager() + Permissions.FRIEND.withManager() + Permissions.TEMP.withManager())
) {

    @Path("json")
    fun addJson(event: MsgEvent) {
        val array = event.restContent.trim().split(Regex("[\t\b\n\r ]+"))
        if (array.size == 3) {
            ImageSourceConfig.sources.add(
                OnlineJsonImageStorage(array[1], array[2]).also { it.storageName = array[0] }.serializeStorage()
            )
            event.sendAsync("已添加在线源")
        } else {
            event.sendAsync("参数个数只能是3个")
        }
    }

    @Path("path")
    fun addPath(event: MsgEvent) {
        val array = event.restContent.trim().split(Regex("[\t\b\n\r ]+"))
        if (array.size == 2) {
            ImageSourceConfig.sources.add(
                LocalImageStorage(array[1]).also { it.storageName = array[0] }.serializeStorage()
            )
            event.sendAsync("已添加路径")
        } else {
            RosemoePlugin.logger.warning(array.toString())
            event.sendAsync("参数个数只能是2个")
        }
    }

    @Path("remove")
    fun removeSource(event: MsgEvent) {
        ImageSourceConfig.sources.filter {
            deserializeStorage(it).storageName == event.restContent
        }.forEach {
            ImageSourceConfig.sources.remove(it)
        }
        event.sendAsync("执行成功")
    }

    @Path("refresh")
    fun refresh(event: MsgEvent) {
        RosemoePlugin.initializeImageList()
        event.sendAsync("图片源已刷新")
    }

}