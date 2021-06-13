package io.github.rosemoe.miraiPlugin.command

import net.mamoe.mirai.contact.User

interface Checker {

    fun shouldRunCommand(user: User, command: Command, group: Long) : Boolean

}