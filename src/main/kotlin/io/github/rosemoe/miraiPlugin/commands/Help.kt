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

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import io.github.rosemoe.miraiPlugin.command.*
import io.github.rosemoe.miraiPlugin.pluginBuildTime
import io.github.rosemoe.miraiPlugin.pluginVersion

@Suppress("unused")
object Help : Command(
    CommandDescription(
        arrayOf("help"),
        Permissions.FRIEND + Permissions.GROUP
    )
) {

    private val items: MutableMap<String, String> = mutableMapOf()

    init {
        init()
    }

    private fun registerHelpItem(path: String, content: String) {
        items[path] = content
    }

    @CommandTriggerPath("")
    fun seekHelp(event: MsgEvent) {
        if (!RosemoePlugin.isModuleEnabled("Help")) {
            return
        }
        val content = items[event.restContent.trim().replace(Regex("[\b\t\n]+"), "/")]
        if (content == null) {
            event.sendAsync("找不到相关帮助（（（")
        } else {
            event.sendAsync(content)
        }
    }

    fun init() {
        registerHelpItem(
            "", """
        BotPlugin - 帮助
        指令表:
        settings - 设置
        sendImage - 请求多张图片
        pixiv - Pixiv相关功能
        blacklist - 控制群聊黑名单
        发送' help <下级指令1> <下级指令2> ... ' 可以获取它们的用法
        比如:
        help settings set
        关于:
        BotPlugin(https://github.com/Rosemoe/BotPlugin)(版本 $pluginVersion 编译时间 $pluginBuildTime),由 Rosemoe 创建
        运行于Mirai Console(https://github.com/mamoe/mirai-console)
    """.trimIndent()
        )

        registerHelpItem(
            "settings", """
        帮助:settings:调整或获取机器人的设置
        该指令下有如下子项目:
        - 含子命令的子命令:
        enable
        disable
        set
        get
        - 直接指令:
        reload - 重载插件配置
        reloadBase - 重载插件配置,但是不刷新图片索引
    """.trimIndent()
        )

        registerHelpItem(
            "settings/enable", """
        帮助:settings enable:启用指定的功能
        用法:settings enable <功能名称>
        可用的功能名称:
        AtReply - 被at时回復
        BatchImg - 请求多张图片,必须开启ImageSender
        ImageSender - 发送本地图库图片
        MuteTip - 禁言/解禁提示
        Pixiv - 获取Pixiv相关信息
        ReverseAtReply - 翻转at回復内容
        ReverseAtReplyImage - 随机旋转at回復的图片
        Welcome - 入群退群提示
    """.trimIndent()
        )

        registerHelpItem(
            "settings/disable", """
        帮助:settings disable:禁用指定的功能
        用法:settings disable <功能名称>
        可用的功能名称:
        AtReply - 被at时回復
        BatchImg - 请求多张图片,必须开启ImageSender
        ImageSender - 发送本地图库图片
        IpList - 获取网站IP表
        MuteTip - 禁言/解禁提示
        Ping - 运行系统Ping指令
        Pixiv - 获取Pixiv相关信息
        ReverseAtReply - 翻转at回復内容
        ReverseAtReplyImage - 随机旋转at回復的图片
        Welcome - 入群退群提示
    """.trimIndent()
        )

        registerHelpItem(
            "settings/set", """
        帮助:settings set:设置一些配置
        用法:settings set <配置名称> <配置内容>
        配置说明:
        Prefix - 指令前缀,默认为'/'
        RecallDelay - 发图之后撤回的间隔,0为不撤回,default为默认,单位为毫秒
        RecallInterval - 撤回消息的最小操作间隔时间,0或default都为默认值,单位为毫秒.本项请勿随意修改
        ImagePathList - 图片路径表,可以有多个路径,用';'分割
    """.trimIndent()
        )

        registerHelpItem(
            "settings/get", """
        帮助:settings get:获取配置内容
        用法:settings get <配置名称>
        配置说明:
        Prefix - 指令前缀
        RecallDelay - 发图之后撤回的间隔,单位为毫秒
        RecallInterval - 撤回消息的最小操作间隔时间,单位为毫秒
        ImagePathList - 图片路径表,可以有多个路径,用';'分割
    """.trimIndent()
        )

        registerHelpItem(
            "settings/reload", """
        帮助:settings reload
        重载配置,所有配置都会被刷新
    """.trimIndent()
        )

        registerHelpItem(
            "settings/reloadBase", """
        帮助:settings reload
        重载配置
        但是不刷新图片索引
        对图片路径的修改不会生效,其它设置会被即时更新
    """.trimIndent()
        )

        registerHelpItem(
            "ping", """
        帮助:ping
        用法:ping <网址>
        说明: 运行操作系统的ping指令. 一些风险指令已经被屏蔽.
        直接发送ping可以查看系统ping的帮助
    """.trimIndent()
        )

        registerHelpItem(
            "sendImage", """
        帮助:sendImage
        用法:sendImage <图片数目>
        说明:发送指定数量的本地图片
    """.trimIndent()
        )

        registerHelpItem(
            "pixiv", """
        帮助:pixiv
        - 子命令
        illust - 插图相关
    """.trimIndent()
        )

        registerHelpItem(
            "pixiv/illust", """
        帮助:pixiv illust
        [1]pixiv illust <画作ID>
        获取画作概览信息,和预览图片(小图)
        [2]pixiv illust <画作ID> <插图索引>
        发送指定的画作中的指定图片的大图
    """.trimIndent()
        )

        registerHelpItem(
            "blacklist", """
        帮助:darklist
        - add <群号或this>
        添加黑名单
        - remove <群号或this>
        移除黑名单
        - list [页码]
        查看黑名单列表
        页码可不填
    """.trimIndent()
        )
    }

}