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

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.*
import org.json.JSONObject
import java.io.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

fun RosemoePlugin.applyProxySettings() {
    proxy = config.proxyEnabled
    proxyAddress = config.proxyAddress
    proxyPort = config.proxyPort
    proxyType = if (config.proxyType.lowercase().contentEquals("http")) Proxy.Type.HTTP else Proxy.Type.SOCKS
}



var proxy = false
var proxyType = Proxy.Type.HTTP
var proxyAddress = "127.0.0.1"
var proxyPort = 1080

@Throws(Throwable::class)
fun getArtworkSource(artworkId: Long): StringBuilder {
    val connection: HttpsURLConnection = if (proxy) {
        val proxyInstance = Proxy(proxyType, InetSocketAddress(proxyAddress, proxyPort))
        URL("https://www.pixiv.net/artworks/$artworkId").openConnection(proxyInstance) as HttpsURLConnection
    } else {
        URL("https://www.pixiv.net/artworks/$artworkId").openConnection() as HttpsURLConnection
    }
    connection.connectTimeout = 5000
    connection.connect()
    val br = BufferedReader(
        InputStreamReader(
            connection.inputStream,
            if (connection.contentEncoding == null) "UTF8" else connection.contentEncoding
        )
    )
    val sb = StringBuilder()
    var line: String?
    while (br.readLine().also { line = it } != null) {
        sb.append(line)
    }
    br.close()
    connection.disconnect()
    return sb
}

private fun trimSourceToJson(source: StringBuilder) {
    val beginMarker = "<meta name=\"preload-data\" id=\"meta-preload-data\" content='"
    val begin = source.indexOf(beginMarker)
    source.delete(0, begin + beginMarker.length)
    val endMarker = "'><"
    val end = source.lastIndexOf(endMarker)
    source.setLength(end)
}

@Throws(Throwable::class)
internal fun getArtworkInformation(gp: Contact, artworkId: Long): MessageChain {
    var sb: StringBuilder = getArtworkSource(artworkId)
    trimSourceToJson(sb)
    var artwork = JSONObject(sb.toString())
    artwork = artwork.getJSONObject("illust")
    artwork = artwork.getJSONObject(artworkId.toString())
    sb = StringBuilder()
    sb.append("Pixiv画作信息:\n")
        .append("插图ID:").append(artworkId).append('\n')
        .append("画作标题:").append(artwork.getString("illustTitle"))
        .append('\n') //.append("画作评论:\n").append(translateHtml(artwork.getString("illustComment"))).append('\n')
        .append("画师ID:").append(artwork.getString("userId")).append('\n')
        .append("画师:").append(artwork.getString("userName")).append('\n')
        .append("画作地址:").append("https://www.pixiv.net/artworks/").append(artworkId).append('\n')
        .append("原图地址:")
    val urls = artwork.getJSONObject("urls")
    sb.append(urls["original"].toString().replace("i.pximg.net", "i.pixiv.cat"))
    sb.append("\n标签:")
    val tags = artwork.getJSONObject("tags").getJSONArray("tags")
    var r18 = false
    if (!RosemoePlugin.config.allowR18ImageInPixiv) {
        for (i in 0 until tags.length()) {
            val tag = tags.getJSONObject(i)
            if (tag.getString("tag").lowercase().contains("r-18")) {
                r18 = true
            }
            sb.append(tag.getString("tag"))
            if (i + 1 != tag.length()) {
                sb.append(", ")
            }
        }
    }
    var msg= messageChainOf(PlainText(sb.toString()))
    if (r18) {
        msg = msg.plus("\n画作可能含有敏感内容,取消了图片的发送")
    } else {
        msg = msg.plus("\n")
        msg = msg.plus("预览图片:\n")
        val pageCount =
            artwork.getJSONObject("userIllusts").getJSONObject(artworkId.toString() + "").getInt("pageCount")
        val firstUrl = urls.getString("regular")
        for (index in 0 until pageCount) {
            var img: Image? = null
            try {
                img = getTargetImage(gp, firstUrl.replace("_p0", "_p$index"), artworkId, true)
            } catch (ex: IOException) {
                try {
                    img = getTargetImage(gp, firstUrl.replace("_p0", "_p$index"), artworkId, false)
                } catch (ex1: IOException) {
                    //ignored
                }
            }
            msg = if (img != null) {
                msg.plus(img)
            } else {
                msg.plus("\n第 ${index + 1} P获取失败")
            }
        }
    }
    return msg
}

@Throws(Throwable::class)
fun getArtworkImage(gp: Contact, artworkId: Long, index: Int): Message {
    val sb: StringBuilder = getArtworkSource(artworkId)
    trimSourceToJson(sb)
    var artwork = JSONObject(sb.toString())
    artwork = artwork.getJSONObject("illust")
    artwork = artwork.getJSONObject(artworkId.toString())
    val urls = artwork.getJSONObject("urls")
    val originUrl = urls["original"].toString()
    val tags = artwork.getJSONObject("tags").getJSONArray("tags")
    var r18 = false
    if (!RosemoePlugin.config.allowR18ImageInPixiv) {
        for (i in 0 until tags.length()) {
            val tag = tags.getJSONObject(i)
            if (tag.getString("tag").lowercase().contains("r-18")) {
                r18 = true
                break
            }
        }
    }
    return if (r18) {
        PlainText("画作可能含有敏感内容,取消了图片的发送")
    } else {
        val pageCount =
            artwork.getJSONObject("userIllusts").getJSONObject(artworkId.toString() + "").getInt("pageCount")
        if (index < 0 || index >= pageCount) {
            PlainText("超出画作的插图数目")
        } else {
            var img: Image? = null
            try {
                img = getTargetImage(gp, originUrl.replace("_p0", "_p$index"), artworkId, true)
            } catch (ex: IOException) {
                try {
                    img = getTargetImage(gp, originUrl.replace("_p0", "_p$index"), artworkId, false)
                } catch (ex1: IOException) {
                    //ignored
                }
            }
            checkNotNull(img)
            img
        }
    }
}

@Throws(IOException::class)
private fun getTargetImage(gp: Contact, url: String, artworkId: Long, proxy: Boolean): Image {
    val file = File("${cacheDirPath()}${File.separator}Pixiv${File.separator}${url.substring(url.lastIndexOf("/") + 1)}")
    val res : Image
    if (file.exists()) {
        runBlocking(RosemoePlugin.coroutineContext) {
            res = gp.uploadImageResource(file)
        }
        return res
    }
    val connection: HttpsURLConnection = if (proxy) {
        val proxyInstance = Proxy(proxyType, InetSocketAddress(proxyAddress, proxyPort))
        URL(url).openConnection(proxyInstance) as HttpsURLConnection
    } else {
        URL(url).openConnection() as HttpsURLConnection
    }
    connection.connectTimeout = 5000
    connection.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0"
    )
    connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
    connection.setRequestProperty("Accept-Encoding", "utf-8")
    connection.setRequestProperty("Connection", "keep-alive")
    connection.setRequestProperty(
        "Referer",
        "https://www.pixiv.net/member_illust.php?mode=medium&illust_id=$artworkId"
    )
    connection.connect()
    val tmp = File(file.absolutePath + ".tmp")
    if (!tmp.exists()) {
        check(!(!(tmp.parentFile.exists() || tmp.parentFile.mkdirs()) || !tmp.createNewFile())) { "failed to create tmp file" }
    }
    val `is` = connection.inputStream
    val fos = FileOutputStream(tmp)
    val buffer = ByteArray(8192 * 2)
    var count: Int
    while (`is`.read(buffer).also { count = it } != -1) {
        fos.write(buffer, 0, count)
    }
    `is`.close()
    fos.flush()
    fos.close()
    connection.disconnect()
    if (tmp.renameTo(file)) {
        runBlocking (RosemoePlugin.coroutineContext) {
            res = gp.uploadImageResource(file)
        }
    } else {
        runBlocking (RosemoePlugin.coroutineContext) {
            res = gp.uploadImageResource(tmp)
        }
        tmp.delete()
    }
    return res
}