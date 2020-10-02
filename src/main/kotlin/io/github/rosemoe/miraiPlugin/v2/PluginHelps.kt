package io.github.rosemoe.miraiPlugin.v2

fun RosemoePlugin.registerHelps() {
    fun registerHelpItem(simplePath: String, content: String) {
        dispatcher.register(if (simplePath.isBlank()) "help" else "help/$simplePath") { event, rest ->
            if (rest.isBlank()) {
                event.sendBackAsync(content)
            } else {
                event.sendBackAsync("在路径help/${simplePath}下找不到'$rest'")
            }
        }
    }

    registerHelpItem("", """
        RosemoePlugin - 帮助
        指令表:
        settings - 设置
        sendImage - 请求多张图片
        pixiv - Pixiv相关功能
        ping - 运行Ping
        ipList,ipList4,ipList6 - 获取网站IP地址
        darklist - 控制群聊黑名单
        发送' help <下级指令1> <下级指令2> ... ' 可以获取它们的用法
        比如:
        help settings
        help settings set
        其它:
        - 被At时回復
        - 入群时欢迎,退群时祖安
        - 禁言时提示
        关于:
        BotPlugin (https://github.com/Rosemoe/BotPlugin) for Mirai Console, which is create by Rosemoe
        Version 2.0.1 (Build 2020/10/2 16:01)
        使用GitHub上的邮件地址联系我.
        运行于 Mirai (https://github.com/mamoe/mirai) 平台
    """.trimIndent())

    registerHelpItem("settings", """
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
    """.trimIndent())

    registerHelpItem("settings/enable", """
        帮助:settings enable:启用指定的功能
        用法:settings enable <功能名称>
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
    """.trimIndent())

    registerHelpItem("settings/disable", """
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
    """.trimIndent())

    registerHelpItem("settings/set", """
        帮助:settings set:设置一些配置
        用法:settings set <配置名称> <配置内容>
        配置说明:
        Prefix - 指令前缀,默认为'/'
        RecallDelay - 发图之后撤回的间隔,0为不撤回,default为默认,单位为毫秒
        RecallInterval - 撤回消息的最小操作间隔时间,0或default都为默认值,单位为毫秒.本项请勿随意修改
        ImagePathList - 图片路径表,可以有多个路径,用';'分割
    """.trimIndent())

    registerHelpItem("settings/get", """
        帮助:settings get:获取配置内容
        用法:settings get <配置名称>
        配置说明:
        Prefix - 指令前缀
        RecallDelay - 发图之后撤回的间隔,单位为毫秒
        RecallInterval - 撤回消息的最小操作间隔时间,单位为毫秒
        ImagePathList - 图片路径表,可以有多个路径,用';'分割
    """.trimIndent())

    registerHelpItem("settings/reload", """
        帮助:settings reload
        重载配置,所有配置都会被刷新
    """.trimIndent())

    registerHelpItem("settings/reloadBase", """
        帮助:settings reload
        重载配置
        但是不刷新图片索引
        对图片路径的修改不会生效,其它设置会被即时更新
    """.trimIndent())

    registerHelpItem("ping", """
        帮助:ping
        用法:ping <网址>
        说明: 运行操作系统的ping指令. 一些风险指令已经被屏蔽.
        直接发送ping可以查看系统ping的帮助
    """.trimIndent())

    registerHelpItem("sendImage", """
        帮助:sendImage
        用法:sendImage <图片数目>
        说明:发送指定数量的本地图片
    """.trimIndent())

    val ipListHelp = """
        帮助:ipList/ipList4/ipList6
        用法:
        ipList <网址>
        ipList4 <网址>
        ipList6 <网址>
        说明:获取网站的IP地址列表
        ipList4只获取IP v4地址
        ipList6只获取IP v6地址
        IP v6需要本地网络连接有v6网络访问权限才可以
    """.trimIndent()

    registerHelpItem("ipList", ipListHelp)
    registerHelpItem("ipList4", ipListHelp)
    registerHelpItem("ipList6", ipListHelp)

    registerHelpItem("pixiv", """
        帮助:pixiv
        - 子命令
        illust - 插图相关
    """.trimIndent())

    registerHelpItem("pixiv/illust", """
        帮助:pixiv illust
        [1]pixiv illust <画作ID>
        获取画作概览信息,和预览图片(小图)
        [2]pixiv illust <画作ID> <插图索引>
        发送指定的画作中的指定图片的大图
    """.trimIndent())

    registerHelpItem("darklist", """
        帮助:darklist
        - add <群号或this>
        添加黑名单
        - remove <群号或this>
        移除黑名单
        - list [页码]
        查看黑名单列表
        页码可不填
    """.trimIndent())


}