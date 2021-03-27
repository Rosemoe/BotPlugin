package io.github.rosemoe.miraiPlugin

import kotlinx.coroutines.runInterruptible
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonEncoder
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock

internal fun RosemoePlugin.registerImageCommands() {
    dispatcher.register("sendImage") { event, restContent ->
        if (isModuleEnabled("BatchImg") && isModuleEnabled("ImageSender")) {
            try {
                val count = restContent.trim().toInt()
                if (count > config.maxImageRequestCount) {
                    event.sendBackAsync(
                        messageChainOf(
                            At(event.sender),
                            PlainText("请求数量太多啦! 一次最多请求${kotlin.math.max(0, config.maxImageRequestCount)}张图片哦")
                        )
                    )
                } else {
                    for (i in 1..count) {
                        sendImageForEvent(event)
                    }
                }
            } catch (exception: NumberFormatException) {
                event.sendBackAsync(messageChainOf(At(event.sender), PlainText("数字格式不对,请重试")))
            }
        }
    }
}

private val imageStorages = ArrayList<ImageStorage>()
private val lock = ReentrantReadWriteLock()

internal fun RosemoePlugin.initializeImageList() {
    logger.verbose("Loading image sources")
    lock.lockWrite()
    try {
        imageStorages.clear()
        config.imagePathList.forEach {path ->
            imageStorages.add(LocalImageStorage(path).also {
                it.init()
                ImageSourceConfig.sources.add(Json.encodeToString(LocalImageStorage.serializer(), it))
            })
        }
        imageStorages.add(OnlineJsonImageStorage("https://hloli.cn/?m=1.json", "data").also { ImageSourceConfig.sources.add(Json.encodeToString(OnlineJsonImageStorage.serializer(), it)) })
    } finally {
        lock.unlockWrite()
    }
    logger.verbose("Loaded image sources")
}

private fun RosemoePlugin.randomImage(): ExternalResource? {
    lock.lockRead()
    val imageStorage = try {
        imageStorages.random()
    } finally {
        lock.unlockRead()
    }
    return imageStorage.obtainImage()
}

internal fun RosemoePlugin.sendImageForEvent(event: GroupMessageEvent) {
    val target = randomImage()
    if (target == null) {
        event.sendBackAsync(
            messageChainOf(
                At(event.sender),
                PlainText("没有可用的图片QAQ")
            )
        )
    } else {
        pluginLaunch {
            val receipt = event.sendBack(
                messageChainOf(
                    At(event.sender),
                    PlainText("这是宁要的图!\n"),
                    event.group.uploadImage(target)
                )
            )
            runInterruptible {
                target.close()
            }
            logger.verbose("Send Image ${target} to group ${event.group.name} (${event.group.id})")
            val delay = config.imageRecallDelay
            if (delay > 0) {
                scheduleRecall(receipt, delay)
            }
        }
    }
}