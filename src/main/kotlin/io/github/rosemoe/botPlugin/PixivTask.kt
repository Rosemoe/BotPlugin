package io.github.rosemoe.botPlugin

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data._____newChain______
import net.mamoe.mirai.message.uploadImage
import org.json.JSONObject
import java.io.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * @author Rose
 */
object PixivTask {
    @JvmStatic
    fun sendIllustWithIndex(msg: GroupMessageEvent, artworkId: Long, index: Int) {
        try {
            val s = getArtworkImage(msg.group, artworkId, index)
            msg.sendMessage(s)
        } catch (e: Throwable) {
            msg.sendGroupMsg("Pixiv图片(ID = $artworkId, Index = $index):\n获取失败.可能是图片不存在或网路问题")
        }
    }

    @Throws(Throwable::class)
    private fun getArtworkImage(gp: Group, artworkId: Long, index: Int): Message {
        val sb: StringBuilder = try {
            getArtworkSource(artworkId, true)
        } catch (e: IOException) {
            getArtworkSource(artworkId, false)
        }
        trimSourceToJson(sb)
        var artwork = JSONObject(sb.toString())
        artwork = artwork.getJSONObject("illust")
        artwork = artwork.getJSONObject(artworkId.toString())
        val urls = artwork.getJSONObject("urls")
        val originUrl = urls["original"].toString()
        val tags = artwork.getJSONObject("tags").getJSONArray("tags")
        var r18 = false
        for (i in 0 until tags.length()) {
            val tag = tags.getJSONObject(i)
            if (tag.getString("tag").toLowerCase().contains("r-18")) {
                r18 = true
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

    @Throws(Throwable::class)
    internal fun getArtworkSource(artworkId: Long, proxy: Boolean): StringBuilder {
        installIfNot()
        val connection: HttpsURLConnection
        connection = if (proxy) {
            val proxyK = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 1080))
            URL("https://www.pixiv.net/artworks/$artworkId").openConnection(proxyK) as HttpsURLConnection
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
        val endMarker = "'></head>"
        val end = source.lastIndexOf(endMarker)
        source.setLength(end)
    }

    @JvmStatic
    fun sendArtworkInformation(gpMsg: GroupMessageEvent, artworkId: Long) {
        try {
            val s = getArtworkInformation(gpMsg.group, artworkId)
            gpMsg.sendMessage(s)
        } catch (e: Throwable) {
            gpMsg.sendGroupMsg("Pixiv画作信息(ID = $artworkId):\n获取失败.可能是画作不存在或网路问题")
        }
    }

    @Throws(Throwable::class)
    internal fun getArtworkInformation(gp: Group, artworkId: Long): Message {
        var sb: StringBuilder = try {
            getArtworkSource(artworkId, true)
        } catch (e: IOException) {
            getArtworkSource(artworkId, false)
        }
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
        for (i in 0 until tags.length()) {
            val tag = tags.getJSONObject(i)
            if (tag.getString("tag").toLowerCase().contains("r-18")) {
                r18 = true
            }
            sb.append(tag.getString("tag"))
            if (i + 1 != tag.length()) {
                sb.append(", ")
            }
        }
        var msg: Message = _____newChain______(sb.toString())
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

    @Throws(IOException::class)
    private fun getTargetImage(gp: Group, url: String, artworkId: Long, proxy: Boolean): Image {
        val file = File("D:\\LocalImageCache\\" + url.substring(url.lastIndexOf("/") + 1))
        val res : Image
        if (file.exists()) {
            runBlocking(gp.coroutineContext) {
                res = gp.uploadImage(file)
            }
            return res
        }
        val connection: HttpsURLConnection
        connection = if (proxy) {
            val proxyK = Proxy(Proxy.Type.HTTP, InetSocketAddress("127.0.0.1", 1080))
            URL(url).openConnection(proxyK) as HttpsURLConnection
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
            runBlocking (gp.coroutineContext) {
                res = gp.uploadImage(file)
            }
        } else {
            runBlocking {
                res = gp.uploadImage(tmp)
            }
            tmp.delete()
        }
        return res
    }
}