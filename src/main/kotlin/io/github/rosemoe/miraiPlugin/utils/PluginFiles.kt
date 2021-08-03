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

import java.io.File

fun cacheDirPath() : String {
    return "${RosemoePlugin.dataFolderPath}${File.separator}Cache"
}

fun userDirPath(id: Long) : String {
    return "${cacheDirPath()}${File.separator}Users${File.separator}$id"
}
/*
fun userDir(id: Long) : File {
    return File(userDirPath(id)).also {
        it.mkdirs()
    }
}*/