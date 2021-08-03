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

import io.github.rosemoe.miraiPlugin.command.CommandDispatcher
import io.github.rosemoe.miraiPlugin.command.Checker
import io.github.rosemoe.miraiPlugin.command.Command
import io.github.rosemoe.miraiPlugin.command.MsgEvent
import io.github.rosemoe.miraiPlugin.commands.*
import io.github.rosemoe.miraiPlugin.commands.Setu.initializeImageList
import io.github.rosemoe.miraiPlugin.commands.Setu.sendImageForEvent
import io.github.rosemoe.miraiPlugin.utils.startRecallManager
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.ListenerHost
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf

object RosemoePlugin : ListenerHost, KotlinPlugin(
    net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription(
        id = "io.github.rosemoe.miraiPlugin.RosemoePlugin",
        version = pluginVersion
    ) {
        name("RosemoeBotPlugin")
        author("Rosemoe")
    }
) , Checker {
    /**
     * Constant fields
     */
    private val IMAGE_REQUEST = arrayOf("来", "图")

    /**
     * Runtime fields
     */
    internal val config = RosemoePluginConfig

    internal val dispatcher = CommandDispatcher(this, coroutineContext)
    private val msgs = MessageStates()

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
        dispatcher.register(Blacklist, Help, Pixiv, Settings, Setu, Sources)
    }

    private fun isManagementCommand(command: Command) : Boolean {
        return command == Blacklist || command == Sources || command == Settings
    }

    override fun shouldRunCommand(user: User, command: Command, group: Long): Boolean {
        if (group != 0L && isDarklistGroup(group)) {
            return config.managers.contains(user.id) && isManagementCommand(command)
        } else {
            return true
        }
    }

    @EventHandler
    @Suppress("unused")
    fun onFriendMessage(event: FriendMessageEvent) {
        try {
            // Dispatch message
            if (isModuleEnabled("ImageSender") && (event.message.containsTexts(IMAGE_REQUEST) || event.message.containsImage(
                    "B407F708-A2C6-A506-3420-98DF7CAC4A57"
                ))
            ) {
                sendImageForEvent(MsgEvent(event))
            }
            dispatcher.dispatch(event)
        } catch (e: Throwable) {
            pluginLaunch {
                event.sender.sendMessage(getExceptionInfo(e))
            }
            logger.error(e)
        }
    }

    @EventHandler
    @Suppress("unused")
    fun onGroupTempMessage(event: GroupTempMessageEvent) {
        try {
            // Dispatch message
            if (isModuleEnabled("ImageSender") && (event.message.containsTexts(IMAGE_REQUEST) || event.message.containsImage(
                    "B407F708-A2C6-A506-3420-98DF7CAC4A57"
                ))
            ) {
                sendImageForEvent(MsgEvent(event))
            }
            dispatcher.dispatch(event)
        } catch (e: Throwable) {
            pluginLaunch {
                event.sender.sendMessage(getExceptionInfo(e))
            }
            logger.error(e)
        }
    }

    @EventHandler
    @Suppress("unused")
    fun onGroupMessage(event: GroupMessageEvent) {
        if (msgs.handle(event)) {
            try {
                // Dispatch message
                if (isModuleEnabled("ImageSender") && (event.message.containsTexts(IMAGE_REQUEST) || event.message.containsImage(
                        "B407F708-A2C6-A506-3420-98DF7CAC4A57"
                    ))
                ) {
                    sendImageForEvent(MsgEvent(event))
                }
                randomRepeat(event)
                handleAtReply(event)
                dispatcher.dispatch(event)
            } catch (e: Throwable) {
                event.sendAsync(getExceptionInfo(e))
                logger.error(e)
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

    private fun processFormat(format: String, event: GroupMemberEvent) : String {
        return format.replace("\$nick", event.user.nameCardOrNick).replace("\$id", event.user.id.toString())
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
        event.group.sendMessage(processFormat(config.msgOnJoinFormat, event))
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
        event.group.sendMessage(processFormat(config.msgOnLeaveFormat, event))
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
        applyProxySettings()
    }

    suspend fun GroupMessageEvent.sendBack(reply: Message): MessageReceipt<Group> {
        return group.sendMessage(reply)
    }

    suspend fun GroupMessageEvent.sendBack(reply: String): MessageReceipt<Group> {
        return group.sendMessage(reply)
    }

    fun GroupMessageEvent.sendAsync(reply: Message) {
        pluginLaunch {
            group.sendMessage(reply)
        }
    }

    fun GroupMessageEvent.sendAsync(reply: String) {
        pluginLaunch {
            group.sendMessage(reply)
        }
    }

    fun pluginLaunch(action: suspend () -> Unit) {
        RosemoePlugin.launch(coroutineContext) {
            action()
        }
    }

}