package io.github.rosemoe.miraiPlugin.v2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.uploadImage
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO

private val random = Random()

fun RosemoePlugin.handleAtReply(event: GroupMessageEvent) {
    val beingCalled = AtomicBoolean(false)
    val msg = event.message

    suspend fun sendBack() {
        if (beingCalled.get()) {
            var chain = messageChainOf()
            var replyQuote: QuoteReply? = null
            var quoteIndex = -2
            for (i in 0 until msg.size) {
                val sub = msg[i]
                if (sub is MessageMetadata) {
                    if (sub is QuoteReply) {
                        if (sub.source.fromId == event.bot.id) {
                            replyQuote = QuoteReply(event.source)
                            quoteIndex = i
                        }
                    }
                    continue
                }
                if (sub is PlainText || sub is Face || sub is VipFace) {
                    chain = chain.plus(sub)
                } else if (sub is AtAll) {
                    chain = chain.plus("@全体成员 ")
                } else if (sub is At) {
                    if (sub.target == event.bot.id) {
                        if (i != quoteIndex + 1) {
                            chain = chain.plus(At(event.sender))
                        }
                    } else {
                        chain = chain.plus(sub)
                    }
                } else if (sub is Image) {
                    try {
                        chain = if (isModuleEnabled("ReverseAtReplyImage")) chain.plus(
                            event.group.uploadImage(
                                runInterruptible(Dispatchers.IO) { rotateImage(sub) }
                            )
                        ) else chain.plus(sub)
                    } catch (e: IOException) {
                        chain = chain.plus(sub)
                        e.printStackTrace()
                    }
                }
            }
            if (isModuleEnabled("ReverseAtReply")) {
                val reversedChain = AtomicReference(messageChainOf())
                chain.forEach { element: SingleMessage ->
                    if (element is PlainText) {
                        reversedChain.set(PlainText(StringBuilder(element.content).reverse()) + reversedChain.get())
                    } else {
                        reversedChain.set(element + reversedChain.get())
                    }
                }
                chain = reversedChain.get()
            }
            if (replyQuote != null) {
                chain = replyQuote.plus(chain)
            }
            event.group.sendMessage(chain)
        }
    }

    if (isModuleEnabled("AtReply")) {
        msg.forEach { subMsg: Message? ->
            if (subMsg is At && subMsg.target == event.bot.id) {
                beingCalled.set(true)
            }
        }
        pluginLaunch {
            sendBack()
        }
    }
}

@Throws(IOException::class)
private fun rotateImage(image: Image): BufferedImage {
    return rotateImage(
        ImageIO.read(URL(runBlocking { image.queryUrl() }).openConnection().getInputStream()),
        random.nextInt() % 2 == 0
    )
}