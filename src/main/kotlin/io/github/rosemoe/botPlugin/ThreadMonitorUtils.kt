package io.github.rosemoe.botPlugin

/**
 * @author Rose
 */
object Common {
    @JvmStatic
    fun monitor(thread: Thread, limit: Long): Boolean {
        val startTime = System.currentTimeMillis()
        var timeLimitExceeded = false
        while (thread.isAlive) {
            if (System.currentTimeMillis() - startTime > limit) {
                thread.stop()
                timeLimitExceeded = true
                break
            }
            try {
                Thread.sleep(50)
            } catch (e: InterruptedException) {
                break
            }
        }
        return timeLimitExceeded
    }
}