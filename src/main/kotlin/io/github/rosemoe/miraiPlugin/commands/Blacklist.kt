package io.github.rosemoe.miraiPlugin.commands

import io.github.rosemoe.miraiPlugin.*
import io.github.rosemoe.miraiPlugin.command.*
import kotlin.math.ceil

@Suppress("unused")
object Blacklist: Command(
    CommandDescription(
    arrayOf("blacklist"),  Permissions.GROUP.withManager() + Permissions.FRIEND.withManager() + Permissions.TEMP.withManager())
) {

    @Path("add")
    fun addToDarkList(event: MsgEvent) {
        val restContent = event.restContent
        val target = restContent.trim()
        darklistLock.lockWrite()
        try {
            val groupId = if (target.contentEquals("this")) event.groupId() else try {
                target.toLong()
            } catch (e: NumberFormatException) {
                event.sendAsync("错误的参数:${restContent}")
                -1
            }
            if (groupId != -1L) {
                if (RosemoePlugin.config.darkListGroups.contains(groupId)) {
                    event.sendAsync("该群已经在黑名单了")
                } else {
                    RosemoePlugin.config.darkListGroups.add(groupId)
                    event.sendAsync("添加 $groupId 到黑名单成功")
                }
            }
        } finally {
            darklistLock.unlockWrite()
        }
    }

    @Path("remove")
    fun removeDarkListGroup(event: MsgEvent) {
        val restContent = event.restContent
        val target = restContent.trim()
        darklistLock.lockWrite()
        try {
            val groupId = if (target.contentEquals("this")) event.groupId() else try {
                target.toLong()
            } catch (e: NumberFormatException) {
                event.sendAsync("错误的参数:${restContent}")
                -1
            }
            if (groupId != -1L) {
                if (!RosemoePlugin.config.darkListGroups.contains(groupId)) {
                    event.sendAsync("该群不在黑名单")
                } else {
                    RosemoePlugin.config.darkListGroups.removeAt(RosemoePlugin.config.darkListGroups.indexOf(groupId))
                    event.sendAsync("移除 $groupId 的黑名单成功")
                }
            }
        } finally {
            darklistLock.unlockWrite()
        }
    }

    @Path("list")
    fun listDarklistGroups(event: MsgEvent) {
        val restContent = event.restContent
        darklistLock.lockRead()
        try {
            val groups = RosemoePlugin.config.darkListGroups
            val pageCount = ceil(groups.size / ITEM_COUNT_EACH_PAGE.toDouble()).toInt()
            val targetPage = if (restContent.isBlank()) 1 else {
                try {
                    restContent.trim().toInt()
                } catch (e: NumberFormatException) {
                    1
                }
            }
            if (targetPage < 1 || targetPage > pageCount) {
                event.sendAsync("页码错误:最大页数是 $pageCount, 最小页数是 1")
                return
            }
            val msg = StringBuilder("黑名单(当前页$targetPage/共${pageCount + 1}页)")
            for (index in ((targetPage - 1) * ITEM_COUNT_EACH_PAGE) until Integer.min(
                groups.size,
                targetPage * ITEM_COUNT_EACH_PAGE - 1
            )) {
                msg.append("\n").append(index + 1).append(":")
                val group = event.bot.getGroup(groups[index])
                if (group == null) {
                    msg.append(groups[index]).append(" (查询名称失败)")
                } else {
                    msg.append(group.name).append(" (").append(groups[index]).append(")")
                }
            }
            event.sendAsync(msg.toString())
        } finally {
            darklistLock.unlockRead()
        }
    }

}