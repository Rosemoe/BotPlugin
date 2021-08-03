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

package io.github.rosemoe.miraiPlugin.command

import net.mamoe.mirai.contact.MemberPermission

/**
 * Describes a command
 * @param name The names of command
 * @param options The options of command
 */
class CommandDescription constructor(val name: Array<String>, val permission: Permission, val options: Array<Option> = arrayOf())

/**
 * Describes an option of command
 * @param name The unique name of the option, such as -XX:useConcurrentGC
 * @param hasArgument Whether there is an argument for the option
 */
class Option constructor(val name: String, val hasArgument: Boolean)

/**
 * Describes the permission of a command
 */
interface Permission {
    /**
     * @param permission this will be given only in group
     */
    fun isGranted(source: SessionType, permission: MemberPermission?, isPluginManager: Boolean): Boolean

    operator fun plus(another: Permission) : Permission {
        val list = mutableListOf<Permission>()
        if (this is CompoundPermission) {
            list.addAll(this.subPermissions)
        } else {
            list.add(this)
        }
        if (another is CompoundPermission) {
            list.addAll(another.subPermissions)
        } else {
            list.add(another)
        }
        return CompoundPermission(list)
    }
}

class Permissions constructor(
    private val group: Boolean,
    private val permission: MemberPermission,
    private val pluginManager: Boolean = false,
    private val friend: Boolean = false,
    private val temp: Boolean = false,
) : Permission {
    override fun isGranted(source: SessionType, permission: MemberPermission?, isPluginManager: Boolean): Boolean {
        // Deny for plugin permission issue
        if (pluginManager && !isPluginManager) {
            return false
        }
        // Check type and group permission
        return when (source) {
            SessionType.GROUP ->
                group && (permission!!.level >= this.permission.level)
            SessionType.FRIEND ->
                friend
            SessionType.TEMP ->
                temp
            SessionType.UNKNOWN->
                false
        }
    }

    fun withManager() : Permissions {
        return Permissions(group, permission, true, friend, temp)
    }

    companion object {
        val GROUP = Permissions(true, MemberPermission.MEMBER)
        val GROUP_ADMIN = Permissions(true, MemberPermission.ADMINISTRATOR)
        val GROUP_OWNER = Permissions(true, MemberPermission.OWNER)
        val FRIEND = Permissions(false, MemberPermission.MEMBER, false,true)
        val TEMP = Permissions(false, MemberPermission.MEMBER, false, false, true)
    }
}

class CompoundPermission constructor(val subPermissions: List<Permission>) : Permission {

    override fun isGranted(source: SessionType, permission: MemberPermission?, isPluginManager: Boolean): Boolean {
        subPermissions.forEach {
            if (it.isGranted(source, permission, isPluginManager)) {
                return true
            }
        }
        return false
    }

}

enum class SessionType {
    GROUP,
    FRIEND,
    TEMP,
    UNKNOWN
}