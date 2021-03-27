package io.github.rosemoe.miraiPlugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object ImageSourceConfig : AutoSavePluginConfig("images_storages") {

    var sources : MutableList<String> by value()

}
