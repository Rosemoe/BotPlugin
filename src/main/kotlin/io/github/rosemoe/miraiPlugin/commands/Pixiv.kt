package io.github.rosemoe.miraiPlugin.commands

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import io.github.rosemoe.miraiPlugin.command.*
import io.github.rosemoe.miraiPlugin.getArtworkImage
import io.github.rosemoe.miraiPlugin.getArtworkInformation
import io.github.rosemoe.miraiPlugin.toLong

object Pixiv : Command(
    CommandDescription(
        arrayOf("pixiv", "p"),
        Permissions.FRIEND + Permissions.GROUP
    )
) {

    @Path("illust,i")
    suspend fun fetchIllustration(event: MsgEvent) {
        if (!RosemoePlugin.isModuleEnabled("Pixiv")) {
            return
        }
        val args = event.restContent.trim().split(Regex("[ \\n\\t]+")).toMutableList()
        for ((index, arg) in args.withIndex()) {
            args[index] = arg.trim()
        }
        RosemoePlugin.pluginLaunch {
            when (args.size) {
                1 -> {
                    val artworkId = args[0].toLong(-1)
                    if (artworkId != -1L) {
                        try {
                            val s = getArtworkInformation(event.subject, artworkId)
                            event.send(s)
                        } catch (e: Throwable) {
                            event.send("Pixiv画作信息(ID = $artworkId):\n获取失败.可能是画作不存在或网路问题")
                            RosemoePlugin.logger.warning("Pixiv failure", e)
                        }
                    } else {
                        event.send("数字格式错误:${args[0]}")
                    }
                }
                2 -> {
                    val artworkId = args[0].toLong(-1)
                    val index = args[1].toLong(-1).toInt()
                    if (artworkId != -1L && index != -1) {
                        try {
                            val s = getArtworkImage(event.subject, artworkId, index)
                            event.send(s)
                        } catch (e: Throwable) {
                            event.send("Pixiv图片(ID = $artworkId, Index = $index):\n获取失败.可能是图片不存在或网路问题")
                            RosemoePlugin.logger.warning("Pixiv failure", e)
                        }
                    } else {
                        event.send("数字格式错误:${args[0]}")
                    }
                }
                else -> {
                    event.send("参数必须是1个或者2个")
                }
            }
        }
    }


}