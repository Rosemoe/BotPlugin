package io.github.rosemoe.miraiPlugin.commands

import io.github.rosemoe.miraiPlugin.*
import io.github.rosemoe.miraiPlugin.command.*
import io.github.rosemoe.miraiPlugin.utils.scheduleRecall
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.utils.ExternalResource
import java.lang.Exception
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.max

@Suppress("unused")
object Setu : Command(
    CommandDescription(
        arrayOf("sendImage"),
        Permissions.FRIEND + Permissions.GROUP
    )
) {

    @Path("")
    fun sendSetu(event: MsgEvent) {
        if (RosemoePlugin.isModuleEnabled("BatchImg") && RosemoePlugin.isModuleEnabled("ImageSender")) {
            try {
                val count = event.restContent.trim().toInt()
                if (count > RosemoePlugin.config.maxImageRequestCount) {
                    event.sendAsync(
                        if (event.groupOrNull() == null)
                            messageChainOf(PlainText("请求数量太多啦! 一次最多请求${max(0, RosemoePlugin.config.maxImageRequestCount)}张图片哦"))
                        else
                            messageChainOf(
                                At(event.sender),
                                PlainText("请求数量太多啦! 一次最多请求${max(0, RosemoePlugin.config.maxImageRequestCount)}张图片哦")
                            )
                    )
                } else {
                    for (i in 1..count) {
                        RosemoePlugin.sendImageForEvent(event)
                    }
                }
            } catch (exception: NumberFormatException) {
                event.sendAsync(messageChainOf(At(event.sender), PlainText("数字格式不对,请重试")))
            }
        }
    }

    private val imageStorages = ArrayList<ImageStorage>()
    private val lock = ReentrantReadWriteLock()

    internal fun RosemoePlugin.initializeImageList() {
        logger.verbose("正在加载图片源")
        lock.lockWrite()
        try {
            imageStorages.clear()
            ImageSourceConfig.sources.forEach {
                try {
                    imageStorages.add(deserializeStorage(it).also {
                        it.init()
                    })
                } catch (e: Exception) {
                    logger.error("加载图片源时发生错误", e)
                }
            }
        } finally {
            lock.unlockWrite()
        }
        logger.verbose("图片源加载完毕")
    }

    private fun randomImage(): ExternalResource? {
        if (imageStorages.isEmpty()) {
            return null
        }
        lock.lockRead()
        val imageStorage = try {
            imageStorages.random()
        } finally {
            lock.unlockRead()
        }
        return imageStorage.obtainImage()
    }

    internal fun RosemoePlugin.sendImageForEvent(event: MsgEvent) {
        val target = randomImage()
        if (target == null) {
            event.sendAsync(
                if (event.groupOrNull() == null)
                    messageChainOf(PlainText("没有可用的图片QAQ"))
                else
                    messageChainOf(
                        At(event.sender),
                        PlainText("没有可用的图片QAQ")
                    )
            )
        } else {
            pluginLaunch {
                //val startTime = System.currentTimeMillis()
                val img = event.uploadImage(target)
                //logger.info("Uploaded image in ${System.currentTimeMillis() - startTime} ms")
                val receipt = event.send(
                    if (event.groupOrNull() == null)
                        messageChainOf(PlainText("这是宁要的图!\n"), img)
                    else
                        messageChainOf(
                            At(event.sender),
                            PlainText("这是宁要的图!\n"),
                            img
                        )
                )
                runInterruptible {
                    target.close()
                }
                val delay = config.imageRecallDelay
                if (delay > 0) {
                    scheduleRecall(receipt, delay)
                }
            }
        }
    }

}