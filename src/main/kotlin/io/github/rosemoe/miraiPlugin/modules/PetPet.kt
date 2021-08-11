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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.contact.Group
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

suspend fun RosemoePlugin.generateGifAndSend(url: String, group: Group, id: Long) {
    val outputFile = "${userDirPath(id)}${File.separator}PetPet.gif"
    var generationSuccess = true
    runInterruptible(Dispatchers.IO) {
        getUserHead(url, id)
        val head = "${userDirPath(id)}${File.separator}avatar.jpg"
        try {
            val process = Runtime.getRuntime().exec(".${File.separator}petpet ${head} ${outputFile} 10", arrayOf("RUST_BACKTRACE=1"]))
            if (process.waitFor() != 0) { // if errors occured at native
                generationSuccess = false
                throw Exception(process.getErrorStream().bufferedReader.readText()) // print Rust backtrace
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generationSuccess = false
        }
    }
    if (generationSuccess) {
        group.sendMessage(group.uploadImageResource(File(outputFile)))
    }
}

operator fun <K, V> Map<K, V>.minus(x: K): V {
    return getValue(x)
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
