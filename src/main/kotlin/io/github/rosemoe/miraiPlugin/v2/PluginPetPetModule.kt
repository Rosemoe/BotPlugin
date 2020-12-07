package io.github.rosemoe.miraiPlugin.v2

import com.squareup.gifencoder.GifEncoder
import com.squareup.gifencoder.ImageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.sendImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.max

const val OUT_SIZE = 112//hand size
const val MAX_FRAME = 5

private const val squish = 1.25
pruvate const val scale = 0.875
private const val spriteY = 20.0
private const val duration = 16L

private val frameOffsets = listOf(
    mapOf("x" to 0, "y" to 0, "w" to 0, "h" to 0),
    mapOf("x" to -4, "y" to 12, "w" to 4, "h" to -12),
    mapOf("x" to -12, "y" to 18, "w" to 12, "h" to -18),
    mapOf("x" to -8, "y" to 12, "w" to 4, "h" to -12),
    mapOf("x" to -4, "y" to 0, "w" to 0, "h" to 0)
)

private val hands: Array<BufferedImage> by lazy {
    val handImage = ImageIO.read(
        getTargetImage(
            "https://benisland.neocities.org/petpet/img/sprite.png",
            "${RosemoePlugin.dataFolderPath}${File.separator}hand.png"
        )
    )
    Array(MAX_FRAME) {
        handImage.getSubimage(it * OUT_SIZE, 0, OUT_SIZE, OUT_SIZE)
    }
}

suspend fun RosemoePlugin.generateGifAndSend(url: String, group: Group, id: Long) {
    val outputFile = File("${RosemoePlugin.dataFolderPath}${File.separator}${id}${File.separator}petpet.gif")
    runInterruptible(Dispatchers.IO) {
        logger.info("Generating in Dispatchers.IO")
        val head = ImageIO.read(FileInputStream(getUserHead(url, id)))
        val outputStream = FileOutputStream(outputFile)
        GifEncoder(outputStream, OUT_SIZE, OUT_SIZE, 0).run {
            val buffer = IntArray(OUT_SIZE * OUT_SIZE)
            val options = ImageOptions()
            for (i in 0 until MAX_FRAME) {
                generateFrame(head, i).getRGB(0, 0, OUT_SIZE, OUT_SIZE, buffer, 0, OUT_SIZE)
                addImage(buffer, OUT_SIZE, options)
            }
            finishEncoding()
        }
        outputStream.close()
    }
    group.sendImage(outputFile)
}

operator fun <K, V> Map<K, V>.minus(x: K): V {
    return getValue(x)
}

private fun getSpriteFrame(i: Int): Map<String, Int> {
    val offset = frameOffsets[i]
    return mapOf(
        "dx" to ((offset - "x") * squish * 0.4).toInt(),
        "dy" to (spriteY + (offset - "y") * squish * 0.9).toInt(),
        "dw" to ((OUT_SIZE + (offset - "w") * squish) * scale).toInt(),
        "dh" to ((OUT_SIZE + (offset - "h") * squish) * scale).toInt()
    )
}

private fun generateFrame(head: BufferedImage, i: Int): BufferedImage {
    val cf = getSpriteFrame(i)
    val result = BufferedImage(OUT_SIZE, OUT_SIZE, BufferedImage.TYPE_INT_ARGB)
    result.createGraphics().apply {
        color = Color.WHITE
        drawRect(0, 0, OUT_SIZE, OUT_SIZE)
        fillRect(0, 0, OUT_SIZE, OUT_SIZE)
        create().apply {
            translate(cf - "dx" + 15, cf - "dy" + 15)
            drawImage(head, 0, 0, ((cf - "dw") * 0.9).toInt(), ((cf - "dh") * 0.9).toInt(), null)
        }
        drawImage(hands[i], 0, max(0.0, ((cf - "dy") * 0.75 - max(0.0, spriteY) - 0.5)).toInt(), null, null)
    }
    return result
}

@Throws(IOException::class)
private fun getUserHead(url: String, memberId: Long): File {
    return getTargetImage(
        url,
        "${RosemoePlugin.dataFolderPath}${File.separator}${memberId}${File.separator}tx.png",
        false
    )
}

@Throws(IOException::class)
private fun getTargetImage(url: String, pathname: String, isUseCache: Boolean = true): File {
    val file = File(pathname)
    if (isUseCache && file.exists()) {
        return file
    }
    val connection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 5000
        setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"
        )
        setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
        setRequestProperty("Accept-Encoding", "utf-8")
        setRequestProperty("Connection", "keep-alive")
        connect()
    }
    if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }
    val `is` = connection.inputStream
    val fos = FileOutputStream(file)
    val buffer = ByteArray(8192 * 2)
    var count: Int
    while (`is`.read(buffer).also { count = it } != -1) {
        fos.write(buffer, 0, count)
    }
    `is`.close()
    fos.flush()
    fos.close()
    connection.disconnect()
    return file
}
