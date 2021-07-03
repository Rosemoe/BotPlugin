package io.github.rosemoe.miraiPlugin.commands

import io.github.rosemoe.miraiPlugin.*
import io.github.rosemoe.miraiPlugin.command.*
import io.github.rosemoe.miraiPlugin.utils.ScriptMethods
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import java.util.concurrent.locks.ReentrantReadWriteLock

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
    "Repeat",
    "Help"
)

val darklistLock = ReentrantReadWriteLock(true)

const val ITEM_COUNT_EACH_PAGE = 15

@Suppress("unused")
object Settings : Command(CommandDescription(
    arrayOf("settings"),  Permissions.GROUP.withManager() + Permissions.FRIEND.withManager() + Permissions.TEMP.withManager())
) {

    @Path("disable")
    fun disableModule(event: MsgEvent) {
        val rest = event.restContent
        event.sendAsync(if (RosemoePlugin.disableModule(rest.trim())) "成功禁用了:${rest.trim()}" else "禁用${rest.trim()}失败了")
    }

    @Path("enable")
    fun enableModule(event: MsgEvent) {
        val rest = event.restContent
        event.sendAsync(if (RosemoePlugin.enableModule(rest.trim())) "成功启用了:${rest.trim()}" else "启用${rest.trim()}失败了")
    }

    @Path("reload")
    fun reloadConfig(event: MsgEvent) {
        try {
            RosemoePlugin.initOrReloadConfig()
            event.sendAsync("重载成功!")
        } catch (e: Exception) {
            RosemoePlugin.logger.error("重载配置失败了！！！", e)
            event.sendAsync("重载失败:${getExceptionInfo(e)}")
        }
    }

    @Path("reloadBase")
    fun reloadBaseConfig(event: MsgEvent) {
        try {
            RosemoePlugin.reloadBaseConfig()
            event.sendAsync("轻重载成功!")
        } catch (e: Exception) {
            RosemoePlugin.logger.error("基础重载失败了！！！", e)
            event.sendAsync("轻重载失败:\n${getExceptionInfo(e)}")
        }
    }

    @Path("set/prefix")
    fun setCommandPrefix(event: MsgEvent) {
        RosemoePlugin.config.commandPrefix = event.restContent.trim()
        RosemoePlugin.dispatcher.prefix = RosemoePlugin.config.commandPrefix
        event.sendAsync("当前的前缀是:${RosemoePlugin.dispatcher.prefix}")
    }

    @Path("get/prefix")
    fun getCommandPrefix(event: MsgEvent) {
        event.sendAsync("当前的前缀是:${RosemoePlugin.dispatcher.prefix}")
    }

    @Path("set/recallDelay")
    fun setRecallDelay(event: MsgEvent) {
        val time = event.restContent.toLong(60000)
        RosemoePlugin.config.imageRecallDelay = time
        event.sendAsync("recallDelay的值设置为$time")
    }

    @Path("get/recallDelay")
    fun getRecallDelay(event: MsgEvent) {
        event.sendAsync("recallDelay的值为${RosemoePlugin.config.imageRecallDelay}")
    }

    @Path("set/recallInterval")
    fun setRecallInterval(event: MsgEvent) {
        val time = event.restContent.toLong(60000)
        RosemoePlugin.config.recallMinPeriod = time
        event.sendAsync("recallInterval的值设置为$time")
    }

    @Path("get/recallInterval")
    fun getRecallInterval(event: MsgEvent) {
        event.sendAsync("recallInterval的值为${RosemoePlugin.config.recallMinPeriod}")
    }

    @Path("set/repeatFactor")
    fun setRepeatFactor(event: MsgEvent) {
        RosemoePlugin.config.repeatFactor = event.restContent.toDouble(0.01)
        event.sendAsync("repeatFactor的值设置为${RosemoePlugin.config.repeatFactor}")
    }

    @Path("get/repeatFactor")
    fun getRepeatFactor(event: MsgEvent) {
        event.sendAsync("repeatFactor的值为${RosemoePlugin.config.repeatFactor}")
    }

    @Path("exec")
    fun executeScript(event: MsgEvent) {
        val code = event.restContent
        val context = Context.enter()
        try {
            val script = context.compileString(code, "<group_msg>", 1, null)
            val scope = context.initStandardObjects()
            fun setJsObject(name: String, value: Any?) {
                val jsObj = Context.javaToJS(value, scope)
                ScriptableObject.putProperty(scope, name, jsObj)
            }
            setJsObject("event", event)
            setJsObject("sender", event.sender)
            setJsObject("group", event.groupOrNull())
            setJsObject("bot", event.bot)
            setJsObject("dlg", ScriptMethods(event))
            context.evaluateString(scope, "function print(msg) { dlg.send(msg + \"\"); }", "<builtin_>", 1, null)
            script.exec(context, scope)
        } finally {
            Context.exit()
        }
    }

    @Path("set/joinMsg")
    fun setMsgOnJoin(event: MsgEvent) {
        RosemoePlugin.config.msgOnJoinFormat = event.restContent
        event.sendAsync("设置成功")
    }

    @Path("get/joinMsg")
    fun getMsgOnJoin(event: MsgEvent) {
        event.sendAsync("入群提示的模板为：\n${RosemoePlugin.config.msgOnJoinFormat}")
    }

    @Path("set/leaveMsg")
    fun setMsgOnLeave(event: MsgEvent) {
        RosemoePlugin.config.msgOnLeaveFormat = event.restContent
        event.sendAsync("设置成功")
    }

    @Path("get/leaveMsg")
    fun getMsgOnLeave(event: MsgEvent) {
        event.sendAsync("退群提示的模板为：\n${RosemoePlugin.config.msgOnLeaveFormat}")
    }

    fun RosemoePlugin.disableModule(name: String): Boolean {
        if (checkModuleName(name)) {
            config.states[name] = false
            return true
        }
        return false
    }

    fun RosemoePlugin.enableModule(name: String): Boolean {
        if (checkModuleName(name)) {
            config.states[name] = true
            return true
        }
        return false
    }

    fun checkModuleName(name: String): Boolean {
        return allowedModuleName.contains(name)
    }

}