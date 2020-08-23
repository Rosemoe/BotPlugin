package io.github.rosemoe.botPlugin

class Speed {
    private val startTime = System.currentTimeMillis()

    @Volatile
    var msgCount: Long = 0

    @kotlin.jvm.JvmField
    @Volatile
    var valid = true
    @Synchronized
    fun compute(): Boolean {
        val minutes = (System.currentTimeMillis() - startTime) / 1000 / 60 + 1
        if (msgCount / minutes > 30) {
            valid = false
            return false
        }
        msgCount++
        valid = true
        return true
    }
}