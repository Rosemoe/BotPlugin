package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import java.lang.Integer.min
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.ceil

val allowedModuleName = listOf(
    "ImageSender",
    "YuScript",
    "Ping",
    "IpList",
    "Pixiv",
    "BatchImg",
    "AtReply",
    "Welcome",
    "MuteTip",
    "ReverseAtReply",
    "ReverseAtReplyImage",
    "PetPet",
    "Repeat"
)

val darklistLock = ReentrantReadWriteLock(true)

const val ITEM_COUNT_EACH_PAGE = 15

internal fun RosemoePlugin.registerManageCommands() {
    fun disableModule(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        event.sendBackAsync(if (disableModule(restContent.trim())) "成功禁用了:${restContent.trim()}" else "禁用${restContent.trim()}失败了")
    }

    fun enableModule(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        event.sendBackAsync(if (enableModule(restContent.trim())) "成功启用了:${restContent.trim()}" else "启用${restContent.trim()}失败了")
    }

    fun reloadConfig(event: GroupMessageEvent) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        try {
            initOrReloadConfig()
            event.sendBackAsync("重载成功!")
        } catch (e: Exception) {
            logger.error("Reload config failed!", e)
            event.sendBackAsync("重载失败:${getExceptionInfo(e)}")
        }
    }

    @Suppress("unused")
    fun reloadBaseConfig(event: GroupMessageEvent) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        try {
            reloadBaseConfig()
            event.sendBackAsync("轻重载成功!")
        } catch (e: Exception) {
            logger.error("Reload basically config failed!", e)
            event.sendBackAsync("轻重载失败:\n${getExceptionInfo(e)}")
        }
    }

    fun addToDarkList(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        val target = restContent.trim()
        darklistLock.lockWrite()
        try {
            val groupId = if (target.contentEquals("this")) event.group.id else try {
                target.toLong()
            } catch (e: NumberFormatException) {
                event.sendBackAsync("错误的参数:${restContent}")
                -1
            }
            if (groupId != -1L) {
                if (config.darkListGroups.contains(groupId)) {
                    event.sendBackAsync("该群已经在黑名单了")
                } else {
                    config.darkListGroups.add(groupId)
                    event.sendBackAsync("添加 $groupId 到黑名单成功")
                }
            }
        } finally {
            darklistLock.unlockWrite()
        }
    }

    fun removeDarkListGroup(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        val target = restContent.trim()
        darklistLock.lockWrite()
        try {
            val groupId = if (target.contentEquals("this")) event.group.id else try {
                target.toLong()
            } catch (e: NumberFormatException) {
                event.sendBackAsync("错误的参数:${restContent}")
                -1
            }
            if (groupId != -1L) {
                if (!config.darkListGroups.contains(groupId)) {
                    event.sendBackAsync("该群不在黑名单")
                } else {
                    config.darkListGroups.removeAt(config.darkListGroups.indexOf(groupId))
                    event.sendBackAsync("移除 $groupId 的黑名单成功")
                }
            }
        } finally {
            darklistLock.unlockWrite()
        }
    }

    fun listDarklistGroups(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        darklistLock.lockRead()
        try {
            val groups = config.darkListGroups
            val pageCount = ceil(groups.size / ITEM_COUNT_EACH_PAGE.toDouble()).toInt()
            val targetPage = if (restContent.isBlank()) 1 else {
                try {
                    restContent.trim().toInt()
                } catch (e: NumberFormatException) {
                    1
                }
            }
            if (targetPage < 1 || targetPage > pageCount) {
                event.sendBackAsync("页码错误:最大页数是 $pageCount, 最小页数是 1")
                return
            }
            val msg = StringBuilder("黑名单(当前页$targetPage/共${pageCount + 1}页)")
            for (index in ((targetPage - 1) * ITEM_COUNT_EACH_PAGE) until min(
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
            event.sendBackAsync(msg.toString())
        } finally {
            darklistLock.unlockRead()
        }
    }

    fun setImagePathList(event: GroupMessageEvent, restContent: String) {
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return
        }
        config.imagePathList.clear()
        val list = restContent.split(';')
        for (item in list) {
            if (item.isNotBlank())
                config.imagePathList.add(item.trim())
        }
        initializeImageList()
        event.sendBackAsync("设置图片路径列表成功")
    }

    rootDispatcher.register("settings/disable", ::disableModule)
    rootDispatcher.register("settings/enable", ::enableModule)
    rootDispatcher.register("settings/reload", ::reloadConfig)
    rootDispatcher.register("settings/reloadBase", ::reloadBaseConfig)
    rootDispatcher.register("settings/set/imagePathList", ::setImagePathList)
    rootDispatcher.register("settings/get/imagePathList") { event, _ ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        val msg = StringBuilder()
        config.imagePathList.forEach {
            msg.append(it).append(';')
        }
        event.sendBackAsync(msg.toString())
    }
    rootDispatcher.register("settings/set/prefix") { event, prefix ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        config.commandPrefix = prefix.trim()
        dispatcher.prefix = config.commandPrefix
        rootDispatcher.prefix = config.commandPrefix
        event.sendBackAsync("当前的前缀是:${dispatcher.prefix}")
    }
    rootDispatcher.register("settings/get/prefix") { event, _ ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        event.sendBackAsync("当前的前缀是:${dispatcher.prefix}")
    }
    rootDispatcher.register("settings/set/recallDelay") { event, rest ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        val time = rest.toLong(60000)
        config.imageRecallDelay = time
        event.sendBackAsync("recallDelay的值设置为$time")
    }
    rootDispatcher.register("settings/get/recallDelay") { event, _ ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        event.sendBackAsync("recallDelay的值为${config.imageRecallDelay}")
    }
    rootDispatcher.register("settings/set/recallInterval") { event, rest ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        config.recallMinPeriod = rest.toLong(180)
        event.sendBackAsync("recallInterval的值设置为${config.recallMinPeriod}")
    }
    rootDispatcher.register("settings/get/recallInterval") { event, _ ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        event.sendBackAsync("recallInterval的值为${config.recallMinPeriod}")
    }
    rootDispatcher.register("settings/set/repeatFactor") { event, factor ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        config.repeatFactor = factor.toDouble(0.01)
        event.sendBackAsync("repeatFactor的值设置为${config.repeatFactor}")
    }
    rootDispatcher.register("settings/get/repeatFactor") { event, _ ->
        if (isNotManager(event.sender.id)) {
            if (!isDarklistGroup(event))
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("你当前不拥有此权限!")))
            return@register
        }
        event.sendBackAsync("repeatFactor的值为${config.repeatFactor}")
    }

    rootDispatcher.register("darklist/add", ::addToDarkList)
    rootDispatcher.register("darklist/remove", ::removeDarkListGroup)
    rootDispatcher.register("darklist/list", ::listDarklistGroups)

    rootDispatcher.register("exec") { event, code ->
        if (isNotManager(event.sender.id)) {
            return@register
        }
        val context = Context.enter()
        try {
            val script = context.compileString(code, "<group_msg>", 1, null)
            val scope = context.initStandardObjects()
            fun setJsObject(name: String, value: Any?) {
                val jsObj = Context.javaToJS(value, scope)
                ScriptableObject.putProperty(scope, name, jsObj)
            }
            setJsObject("event", event)
            event.sender
            setJsObject("group", event.group)
            setJsObject("bot", event.bot)
            setJsObject("dlg", ScriptMethods(event))
            context.evaluateString(scope, "function print(msg) { dlg.send(msg + \"\"); }", "<builtin_>", 1, null)
            script.exec(context, scope)
        } finally {
            Context.exit()
        }
    }
}

private fun RosemoePlugin.isNotManager(id: Long): Boolean {
    return !config.managers.contains(id)
}

private fun RosemoePlugin.disableModule(name: String): Boolean {
    if (checkModuleName(name)) {
        config.states[name] = false
        return true
    }
    return false
}

private fun RosemoePlugin.enableModule(name: String): Boolean {
    if (checkModuleName(name)) {
        config.states[name] = true
        return true
    }
    return false
}

internal fun checkModuleName(name: String): Boolean {
    return allowedModuleName.contains(name)
}
