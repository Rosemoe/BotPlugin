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