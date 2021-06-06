package io.github.rosemoe.miraiPlugin

import net.mamoe.mirai.event.events.GroupMessageEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

fun RosemoePlugin.registerPingCommands() {
    @Throws(Throwable::class)
    fun doPing(gpMsg: GroupMessageEvent, restContent: String) {
        if (!isModuleEnabled("Ping")) {
            return
        }
        val low = restContent.lowercase()
        if (low.contains("-t")) {
            throw SecurityException("Rejected Option:-t")
        }
        if (low.contains("-i")) {
            throw SecurityException("拒绝访问-i参数")
        }
        if (low.contains("shut") || low.contains("\n") || low.contains("|") || low.contains("power")
            || low.contains("dir") || low.contains("del") || low.contains("-n")
        ) {
            throw SecurityException("你的输入存在敏感词，访问被取消！")
        }
        val process = Runtime.getRuntime().exec("ping $restContent")
        process.waitFor()
        val output = StringBuilder()
        output.append("Ping结果:").append('\n')
        output.append("退出代码:").append(process.exitValue()).append('\n')
        readContentOfStream(output, process.inputStream)
        readContentOfStream(output, process.errorStream)
        gpMsg.sendBackAsync(output.toString())
    }

    dispatcher.register("ping", ::doPing)
}

@Throws(IOException::class)
private fun readContentOfStream(sb: StringBuilder, inputStream: InputStream) {
    val br = BufferedReader(InputStreamReader(inputStream, "GBK"))
    var line: String?
    while (br.readLine().also { line = it } != null) {
        sb.append(line).append('\n')
    }
    br.close()
}
