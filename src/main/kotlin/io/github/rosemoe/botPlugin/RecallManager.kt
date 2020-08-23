package io.github.rosemoe.botPlugin

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.recall
import net.mamoe.mirai.message.recallIn
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class RecallManager {
    private val recallQueue: BlockingQueue<RecallRequest>

    private class RecallRequest constructor(
        var target: MessageReceipt<Contact>,
        var expectedRecallTime: Long
    ) {
        var sendTime: Long = System.currentTimeMillis()
    }

    fun scheduleRecall(target: MessageReceipt<Contact>, delay: Long) {
        try {
            recallQueue.put(RecallRequest(target, System.currentTimeMillis() + delay))
        } catch (e: InterruptedException) {
            target.recallIn(delay, target.target.coroutineContext)
        }
    }

    interface Delegate {
        val waitTime: Long
    }

    fun loop(logger: MiraiLogger, delegate: Delegate) {
        logger.debug("RecallManager start!")
        while (!Thread.interrupted()) {
            try {
                val target = recallQueue.take()
                val restTime = target.expectedRecallTime - System.currentTimeMillis()
                if (restTime > 0) {
                    try {
                        recallQueue.put(target)
                        continue
                    } catch (e2: InterruptedException) {
                        logger.error("Failed to put a fresh request back, recalling")
                        try {
                            runBlocking (target.target.target.coroutineContext) {
                                target.target.recall()
                            }
                            logger.info("Recall succeeded")
                        } catch (e: Exception) {
                            logger.error("Failed to recall message:", e)
                        }
                    }
                } else {
                    logger.info("Trying to recall a message")
                    try {
                        runBlocking (target.target.target.coroutineContext) {
                            target.target.recall()
                        }
                        logger.info("Message is recalled")
                    } catch (e: Exception) {
                        logger.error("Failed to recall message")
                        if (System.currentTimeMillis() - target.sendTime < 2 * 60 * 1000 && checkNotRecalled(e)) {
                            logger.info("Trying to push request back to queue")
                            try {
                                recallQueue.put(target)
                                logger.info("Succeeded in pushing request back")
                            } catch (e2: InterruptedException) {
                                logger.error("Failed to push request back (interrupted)")
                            }
                        } else {
                            logger.warning("Request is dirty. Cancel to push back:" + if (checkNotRecalled(e)) "Message is not available" else "Recall time limit exceeded")
                        }
                    }
                }
                val timeStartWait = System.currentTimeMillis()
                while (System.currentTimeMillis() - timeStartWait < delegate.waitTime) {
                    Thread.sleep(20)
                }
            } catch (e: InterruptedException) {
                logger.debug("RecallManager is being interrupted!")
                break
            }
        }
        logger.debug("RecallManager thread stop!")
    }

    companion object {
        fun checkNotRecalled(e: Exception): Boolean {
            return e.message != null && !e.message!!.contains("had already been recalled") && !e.message!!.contains("requirement")
        }
    }

    init {
        recallQueue = ArrayBlockingQueue(2048)
    }
}