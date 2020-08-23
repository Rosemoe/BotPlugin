package io.github.rosemoe.botPlugin

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.message.uploadImage
import java.io.File
import java.util.*
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

object LocalImageSender {
    private val sLock: ReadWriteLock = ReentrantReadWriteLock()
    private val images: MutableList<File> = ArrayList()
    @JvmStatic
    val imageCount: Int
        get() = images.size

    @JvmStatic
    fun clear() {
        images.clear()
    }

    @JvmStatic
    fun addPathToList(file: File) {
        try {
            sLock.writeLock().lock()
            addPathToListInternal(file)
        } finally {
            sLock.writeLock().unlock()
        }
    }

    private fun addPathToListInternal(file: File) {
        if (file.isDirectory) {
            val fileList = file.listFiles() ?: return
            for (sub in fileList) {
                addPathToListInternal(sub)
            }
            return
        }
        if (file.name.endsWith(".jpg") || file.name.endsWith(".png")) {
            images.add(file)
        }
    }

    private val sRandom = Random()
    @JvmStatic
    fun sendImageOnGroupMessage(msg: GroupMessageEvent, imgRecall: Long, recallManager: RecallManager) {
        sLock.readLock().lock()
        val target: File
        target = try {
            if (images.size == 0) {
                msg.sendGroupMsgAsync(messageChainOf(At(msg.sender), PlainText("窝找不到主人给我的图片啦~")))
                return
            }
            val index = sRandom.nextInt(images.size)
            images[index]
        } finally {
            sLock.readLock().unlock()
        }
        val receipt: MessageReceipt<Contact> = msg.sendMessage(
            messageChainOf(
                At(msg.sender), PlainText("此乃是宁要的图~"),
                runBlocking (msg.group.coroutineContext) {
                    msg.group.uploadImage(target)
                }
            )
        )
        if (imgRecall != 0L) {
            recallManager.scheduleRecall(receipt, imgRecall)
        }
    }
}