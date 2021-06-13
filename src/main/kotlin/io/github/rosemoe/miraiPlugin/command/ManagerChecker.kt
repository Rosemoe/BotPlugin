package io.github.rosemoe.miraiPlugin.command

import net.mamoe.mirai.contact.User

interface ManagerChecker {

    fun isPluginManager(user: User) : Boolean

}