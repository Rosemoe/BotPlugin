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

package io.github.rosemoe.miraiPlugin

import io.github.rosemoe.miraiPlugin.gifmaker.GifEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.contact.Group
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.max

private const val OUT_SIZE = 112//hand size
private const val MAX_FRAME = 5

private const val squish = 1.25
private const val scale = 0.875
private const val spriteY = 20.0
private const val duration = 16

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
    val outputFile = newFile("${userDirPath(id)}${File.separator}PetPet.gif")
    runInterruptible(Dispatchers.IO) {
        val head = ImageIO.read(FileInputStream(getUserHead(url, id)))
        val outputStream = FileOutputStream(outputFile)
        GifEncoder().run {
            delay = duration
            repeat = 0
            setTransparent(Color.TRANSLUCENT)
            start(outputStream)
            for (i in 0 until MAX_FRAME) {
                addFrame(generateFrame(head, i))
            }
            finish()
        }
    }
    group.sendMessage(group.uploadImageResource(outputFile))
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
        //color = Color.WHITE
        //drawRect(0, 0, OUT_SIZE, OUT_SIZE)
        //fillRect(0, 0, OUT_SIZE, OUT_SIZE)
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
        "${userDirPath(memberId)}${File.separator}avatar.jpg",
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
