package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File
import java.lang.NumberFormatException

internal fun RosemoePlugin.registerImageCommands() {
    dispatcher.register("sendImage") { event, restContent ->
        if (isModuleEnabled("BatchImage") && isModuleEnabled("ImageSender")) {
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

internal fun RosemoePlugin.initializeImageList() {
    logger.verbose("Loading images, path count = ${config.imagePathList.size}")
    fun File.isImage(): Boolean {
        val title = name.toLowerCase()
        return title.endsWith(".jpg") || title.endsWith(".png") || title.endsWith(".webp")
    }

    fun searchForImageFiles(file: File) {
        if (file.isFile) {
            if (file.isImage()) {
                imageList.add(file)
            }
        } else {
            file.listFiles()?.forEach {
                if (it != null) {
                    searchForImageFiles(it)
                }
            }
        }
    }

    imageListLock.lockWrite()
    try {
        logger.verbose("Removing previous images...")
        imageList.clear()
        logger.verbose("Indexing images...")
        config.imagePathList.forEach {
            searchForImageFiles(File(it))
        }

        logger.info("Image load succeeded. Indexed image count = ${imageList.size}")
    } finally {
        imageListLock.unlockWrite()
    }
}

private fun RosemoePlugin.randomImageFile(): File? {
    imageListLock.lockRead()
    try {
        val size = imageList.size
        if (size > 0) {
            return imageList[imageRandom.nextInt(size)]
        }
    } finally {
        imageListLock.unlockRead()
    }
    return null
}

internal fun RosemoePlugin.sendImageForEvent(event: GroupMessageEvent) {
    val target = randomImageFile()
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
                    event.group.uploadImage(target.toExternalResource())
                )
            )
            logger.verbose("Send Image ${target.path} to group ${event.group.name} (${event.group.id})")
            val delay = config.imageRecallDelay
            if (delay > 0) {
                scheduleRecall(receipt, delay)
            }
        }
    }
}