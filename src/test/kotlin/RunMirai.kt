package org.example.mirai.plugin

import io.github.rosemoe.miraiPlugin.v2.RosemoePlugin
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader

suspend fun main() {
    MiraiConsoleTerminalLoader.startAsDaemon()

    RosemoePlugin.load()
    RosemoePlugin.enable()

    val bot = MiraiConsole.addBot(123123, "password") {
        fileBasedDeviceInfo()
    }.alsoLogin()

    MiraiConsole.job.join()
 }