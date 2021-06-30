package io.github.rosemoe.miraiPlugin.utils

import io.github.rosemoe.miraiPlugin.RosemoePlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageSource
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

val taskQueue = LinkedBlockingQueue<Request>(8192 * 2)

suspend fun <E : Any?> BlockingQueue<E>.awaitTake() : E = runInterruptible (Dispatchers.IO) { take() }

suspend fun recall(target: MessageReceipt<*>) {
    target.recall()
}

fun resetRecallSign(source: MessageSource) {
    try {
        val internalClass = Class.forName("net.mamoe.mirai.internal.message.MessageSourceInternal")
        val method = internalClass.getDeclaredMethod("isRecalledOrPlanned")
        val value = method.invoke(source) as AtomicBoolean
        value.compareAndExchange(true, false)
    }catch (ignored: Exception) {

    }
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
                    logger.warning("Recall message failed")
                    logger.verbose(e)
                    if ((req.createTime + 120000) > System.currentTimeMillis()) {
                        val source = req.target.source
                        resetRecallSign(source)
                        launch (Dispatchers.IO) { taskQueue.put(req) }
                        delay(10000)
                        continue
                    }
                    delay(10000)
                    logger.warning("Un-recalled message is outdated: now: ${System.currentTimeMillis()} createTime: ${req.createTime}")
                }
            } else {
                runInterruptible (Dispatchers.IO) { taskQueue.put(req) }
                delay(20)
            }
        }
    }
}

fun scheduleRecall(receipt: MessageReceipt<*>, delay: Long) {
    taskQueue.put(Request(delay, receipt))
}

class Request constructor(delay: Long, receipt: MessageReceipt<*>) {

    val createTime = System.currentTimeMillis()

    val expectedTime = delay + createTime

    val target = receipt

}