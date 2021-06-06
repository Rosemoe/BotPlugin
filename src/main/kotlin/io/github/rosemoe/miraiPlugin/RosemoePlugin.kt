package io.github.rosemoe.miraiPlugin

import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.ArrayList
import kotlin.math.log

object RosemoePlugin : ListenerHost, KotlinPlugin(
    net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription(
        id = "io.github.rosemoe.miraiPlugin.RosemoePlugin",
        version = pluginVersion
    ) {
        name("RosemoeBotPlugin")
        author("Rosemoe")
    }
) {
    /**
     * Constant fields
     */
    private val IMAGE_REQUEST = arrayOf("来", "图")

    /**
     * Runtime fields
     */
    internal val config = RosemoePluginConfig

    internal val dispatcher = CommandDispatcher()
    internal val rootDispatcher = CommandDispatcher()
    internal val msgs = MessageStates()

    override fun onEnable() {
        super.onEnable()
        /*try {
            logger.verbose("正在检查插件更新...")
            val source = getWebpageSource("https://github.com/Rosemoe/BotPlugin/releases/latest")
        } catch (e: Exception) {
            logger.warning("检查插件更新失败", e)
        }*/
        initOrReloadConfig()
        globalEventChannel(this.coroutineContext).registerListenerHost(this)
        registerCommands()
        startRecallManager()
        msgs.launchClearer(this)
    }

    private fun registerCommands() {
        registerImageCommands()
        registerManageCommands()
        registerPingCommands()
        registerIpCommands()
        registerPixivCommands()
        registerHelps()
    }

    @EventHandler
    @Suppress("unused")
    fun onGroupMessage(event: GroupMessageEvent) {
        if (msgs.handle(event)) {
            try {
                // Settings are available everywhere
                rootDispatcher.dispatch(event)
                // Check group id
                if (isDarklistGroup(event)) {
                    return
                }
                // Dispatch message
                if (isModuleEnabled("ImageSender") && (event.message.containsTexts(IMAGE_REQUEST) || event.message.containsImage(
                        "B407F708-A2C6-A506-3420-98DF7CAC4A57"
                    ))
                ) {
                    sendImageForEvent(event)
                }
                randomRepeat(event)
                handleAtReply(event)
                dispatcher.dispatch(event)
            } catch (e: Throwable) {
                event.sendBackAsync(getExceptionInfo(e))
            }
        }
    }

    fun isDarklistGroup(event: GroupEvent): Boolean {
        return isDarklistGroup(event.group.id)
    }

    private fun isDarklistGroup(id: Long): Boolean {
        return config.darkListGroups.contains(id)
    }

    @EventHandler
    @Suppress("unused")
    suspend fun onMemberMute(event: MemberMuteEvent) {
        if (isDarklistGroup(event)) {
            return
        }
        if (!isModuleEnabled("MuteTip")) {
            return
        }
        event.group.sendMessage(
            messageChainOf(
                At(event.member), PlainText(
                    " 喝下了${
                        if (event.operator?.permission == MemberPermission.OWNER) "群主" else "管理"
                    }的红茶,睡了过去"
                )
            )
        )
    }

    @EventHandler
    @Suppress("unused")
    suspend fun onMemberUnmute(event: MemberUnmuteEvent) {
        if (isDarklistGroup(event)) {
            return
        }
        if (!isModuleEnabled("MuteTip")) {
            return
        }
        event.group.sendMessage(
            messageChainOf(
                At(event.member), PlainText(" 被先辈叫醒了")
            )
        )
    }

    @EventHandler
    @Suppress("unused")
    suspend fun onMemberJoin(event: MemberJoinEvent) {
        if (isDarklistGroup(event)) {
            return
        }
        if (!isModuleEnabled("Welcome")) {
            return
        }
        event.group.sendMessage("欢迎 ${event.member.nick} 加入本群!")
    }

    @EventHandler
    @Suppress("unused")
    suspend fun onMemberLeave(event: MemberLeaveEvent) {
        if (isDarklistGroup(event)) {
            return
        }
        if (!isModuleEnabled("Welcome")) {
            return
        }
        if (event is MemberLeaveEvent.Quit)
            event.group.sendMessage("${event.member.nick} (${event.member.id}) 滚蛋了,丢人!!!")
        else
            event.group.sendMessage("${event.member.nick} (${event.member.id}) 被飞出去了,丢人!!!")
    }

    @EventHandler
    @Suppress("unused")
    suspend fun onMemberNudge(event: NudgeEvent) {
        if (event.subject is Group) {
            val group = event.subject as Group
            if (isDarklistGroup(group.id) || !isModuleEnabled("PetPet")) {
                return
            }
            val url = event.target.avatarUrl.replace("s=640", "s=100")
            generateGifAndSend(url, group, event.target.id)
        }

    }

    internal fun isModuleEnabled(name: String): Boolean {
        return config.states.getOrDefault(name, true)
    }

    internal fun initOrReloadConfig() {
        reloadBaseConfig()
        initializeImageList()
    }

    internal fun reloadBaseConfig() {
        reloadPluginConfig(config)
        reloadPluginConfig(ImageSourceConfig)
        dispatcher.prefix = if (config.commandPrefix.isBlank()) "/" else config.commandPrefix
        rootDispatcher.prefix = dispatcher.prefix
        applyProxySettings()
    }

    suspend fun GroupMessageEvent.sendBack(reply: Message): MessageReceipt<Group> {
        return group.sendMessage(reply)
    }

    suspend fun GroupMessageEvent.sendBack(reply: String): MessageReceipt<Group> {
        return group.sendMessage(reply)
    }

    fun GroupMessageEvent.sendBackAsync(reply: Message) {
        pluginLaunch {
            group.sendMessage(reply)
        }
    }

    fun GroupMessageEvent.sendBackAsync(reply: String) {
        pluginLaunch {
            group.sendMessage(reply)
        }
    }

    internal fun pluginLaunch(action: suspend () -> Unit) {
        RosemoePlugin.launch {
            action()
        }
    }

}