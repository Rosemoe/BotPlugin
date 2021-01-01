package io.github.rosemoe.miraiPlugin.v2

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.awt.image.BufferedImage
import java.io.File
import java.lang.StringBuilder
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.imageio.ImageIO

fun MessageChain.containsImage( id: String): Boolean {
    for (element in this) {
        if (element is Image) {
            if (element.imageId.contains(id)) {
                return true
            }
        }
    }
    return false
}

fun MessageChain.containsTexts(patterns: Array<String>): Boolean {
    val states = Array(patterns.size) {
        true
    }
    var matched = 0
    forEach { singleMsg ->
        if (singleMsg is PlainText) {
            for ((index, element) in patterns.withIndex()) {
                if (states[index] && singleMsg.content.contains(element, false)) {
                    states[index] = false
                    matched++
                }
            }
        }
    }
    return matched == patterns.size
}


fun rotateImage(src: java.awt.Image, type: Boolean) : BufferedImage {
    return if (type) rotateImage90(src) else rotateImage180(src)
}

private fun rotateImage90(src: java.awt.Image) : BufferedImage {
    val res = BufferedImage(src.getHeight(null), src.getWidth(null), BufferedImage.TYPE_INT_ARGB)
    res.accelerationPriority = 1f
    src.accelerationPriority = 1f
    res.createGraphics().apply {
        translate(src.getHeight(null) / 2.0, src.getWidth(null) / 2.0)
        rotate(Math.toRadians(90.0))
        translate(-src.getWidth(null) / 2.0, -src.getHeight(null) / 2.0)
        drawImage(src, null, null)
    }
    return res
}

private fun rotateImage180(src: java.awt.Image) : BufferedImage {
    val res = BufferedImage(src.getWidth(null), src.getHeight(null), BufferedImage.TYPE_INT_ARGB)
    res.accelerationPriority = 1f
    src.accelerationPriority = 1f
    res.createGraphics().apply {
        translate(src.getWidth(null) / 2.0, src.getHeight(null) / 2.0)
        rotate(Math.toRadians(180.0))
        translate(-src.getWidth(null) / 2.0, -src.getHeight(null) / 2.0)
        drawImage(src, null, null)
    }
    return res
}

fun getExceptionInfo(e: Throwable) : String {
    val info = StringBuilder()
    var it: Throwable? = e
    var first = true
    while (it != null) {
        if (!first) {
            info.append("\nCaused by:")
        }
        info.append(it.javaClass.name).append(':').append(it.message)
        first = false
        it = it.cause
    }
    return info.toString()
}

fun ReentrantReadWriteLock.lockRead() {
    readLock().lock()
}

fun ReentrantReadWriteLock.unlockRead() {
    readLock().unlock()
}

fun ReentrantReadWriteLock.lockWrite() {
    writeLock().lock()
}

fun ReentrantReadWriteLock.unlockWrite() {
    writeLock().unlock()
}

fun String.getLong() : Long {
    return try {
        toLong()
    } catch (e: NumberFormatException) {
        -1
    }
}

fun makeImageResource(image: BufferedImage) : ExternalResource {
    val file = File.createTempFile("buffered-", ".tmp", File("${RosemoePlugin.dataFolder.absolutePath}${File.separator}Cache${File.separator}Image").also { if(!it.exists()) it.mkdirs() })
    ImageIO.write(image, "png", file)
    file.deleteOnExit()
    return file.toExternalResource()
}