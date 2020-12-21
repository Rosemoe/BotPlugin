package io.github.rosemoe.miraiPlugin.v2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.bot
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

val taskQueue = LinkedBlockingQueue<Request>(8192 * 2)

suspend fun <E : Any?> BlockingQueue<E>.awaitTake() : E = runInterruptible (Dispatchers.IO) { take() }

suspend fun recall(target: MessageReceipt<Group>) {
    net.mamoe.mirai.Mirai.recallMessage(target.bot, target.source)
}

/**
 * This is to work around the shit Tencent protocol
 * If we try to recall messages concurrently or too quickly, it will be denied
 * So we create this manager to recall messages in a single thread without concurrency but an extra interval time
 */
fun RosemoePlugin.startRecallManager() {
    launch (coroutineContext) {
        logger.info("RecallManager START!")
        while (!Thread.interrupted()) {
            val req = taskQueue.awaitTake()
            if (req.expectedTime <= System.currentTimeMillis()) {
                try {
                    recall(req.target)
                    logger.info("Recalled a message")
                    val interval = config.recallMinPeriod
                    delay(if(interval <= 0L) 180L else interval)
                }catch (e: Exception) {
                    logger.warning("Recall messages failed")
                    if (req.createTime + 60000 * 2 /*2 min*/ < System.currentTimeMillis()) {
                        runInterruptible (Dispatchers.IO) { taskQueue.put(req) }
                        continue
                    }
                    logger.warning("Un-recalled message is outdated")
                }
            } else {
                runInterruptible (Dispatchers.IO) { taskQueue.put(req) }
                delay(30)
            }
        }
    }
}

fun scheduleRecall(receipt: MessageReceipt<Group>, delay: Long) {
    taskQueue.put(Request(delay, receipt))
}

class Request constructor(delay: Long, receipt: MessageReceipt<Group>) {

    val createTime = System.currentTimeMillis()

    val expectedTime = delay + createTime

    val target = receipt

}