package io.github.rosemoe.botPlugin

import io.github.rosemoe.botPlugin.IpTask.runCommandGeneral
import io.github.rosemoe.botPlugin.IpTask.runCommandV4
import io.github.rosemoe.botPlugin.IpTask.runCommandV6
import io.github.rosemoe.botPlugin.LocalImageSender.addPathToList
import io.github.rosemoe.botPlugin.LocalImageSender.clear
import io.github.rosemoe.botPlugin.LocalImageSender.imageCount
import io.github.rosemoe.botPlugin.LocalImageSender.sendImageOnGroupMessage
import io.github.rosemoe.botPlugin.PingTask.runCommand
import io.github.rosemoe.botPlugin.PixivTask.sendArtworkInformation
import io.github.rosemoe.botPlugin.PixivTask.sendIllustWithIndex
import io.github.rosemoe.botPlugin.YuScriptTask.interruptSession
import io.github.rosemoe.botPlugin.YuScriptTask.reset
import io.github.rosemoe.botPlugin.YuScriptTask.runYuScript
import io.github.rosemoe.botPlugin.YuScriptTask.runYuScriptUnlimited
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.command.CommandManager.register
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.plugins.Config
import net.mamoe.mirai.console.plugins.PluginBase
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.MemberJoinEvent
import net.mamoe.mirai.event.events.MemberLeaveEvent.Quit
import net.mamoe.mirai.event.events.MemberMuteEvent
import net.mamoe.mirai.message.GroupMessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.recall
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import javax.imageio.ImageIO
import net.mamoe.mirai.message.uploadImage
import java.util.regex.Pattern

@SuppressWarnings("unused")
@Suppress("unused")
class TestPlugin : PluginBase(), RejectedExecutionHandler, RecallManager.Delegate {
    private var executor: ThreadPoolExecutor
    private val dispatcher: CommandDispatcher = CommandDispatcher()

    @Volatile
    private var config: Config? = null
    private val recallManager: RecallManager
    override val waitTime: Long
        get() = config!!.getLong("recallMinPeriod")

    override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
        logger.error("Thread Pool Failure: Failed to request execution")
        if (r is SendImageRunnable) {
            r.event.sendGroupMsg("Action is rejected by executor")
        }
    }

    private fun check(name: String?) {
        if (!config!!.getBoolean(name!!)) {
            throw Disabled.INSTANCE
        }
    }

    //--------------------Help commands----------------------------
    @CommandMethod(path = "help")
    fun rootHelp(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        val helpMsg =
            "帮助:\n/ping -> 使用运行环境系统的Ping指令\n/ipList -> 获得网站的所有IP地址\n/ipList4 -> 获得网站的IP v4地址\n/ipList6 -> 获得网站的IP v6地址\n/yuscript -> 使用Rose开发的 YuScript(https://github.com/Rosemoe/YuScript) 执行一段代码\n/pixiv -> 获取Pixiv的图片详情\n/sendImg <count> -> 让机器人发送count数量的图片\n/config -> 调整插件的设置(需要开发者调用)\n小提示:\n使用 '/<命令名称> help' 可以获得对应指令的帮助~\n发送任何带有 '来' 以及 '图' 的文字会触发图片发送\n关于Rsmoe♪:\n  - 运行的插件由Rose开发\n  - 由开源多平台机器人框架 mirai(https://github.com/mamoe/mirai) 强力驱动\n"
        msg.sendMessage(helpMsg)
    }

    @CommandMethod(path = "yuscript/help")
    fun yuScriptHelp(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("yuscript")
        val helpMsg = """
            YuScript帮助:
            /yuscript exec <代码> -> 执行一段iyu代码
            /yuscript reset -> 清除群变量和全局变量
            """.trimIndent()
        msg.sendMessage(helpMsg)
    }

    @CommandMethod(path = "pixiv<p>/help")
    fun pixivHelp(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("pixiv")
        val helpMsg = """
            Pixiv帮助:
            /pixiv illust <画作ID>
            发送指定的画作
            
            """.trimIndent()
        msg.sendMessage(helpMsg)
    }

    //--------------------Pixiv Commands---------------------------------------------
    @CommandMethod(path = "pixiv<p>", asFallbackMethod = true)
    fun pixivFallback(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("pixiv")
        msg.sendMessage("pixiv:没有这样的命令啦~")
    }

    @CommandMethod(path = "pixiv<p>/illust<i>")
    fun sendIllust(msg: GroupMessageEvent, o: String) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("pixiv")
        val rest = o.trim()
        executor.execute {
            if (rest.contains(" ") || rest.contains("\n") || rest.contains("\r") || rest.contains("\t")) {
                val args = rest.split(Pattern.compile("[\b\t\n\r ]+"))
                if (args.size != 2) {
                    msg.sendMessage("参数不是1个或者2个")
                } else {
                    try {
                        sendIllustWithIndex(msg, args[0].toLong(), args[1].toInt())
                    } catch (e: NumberFormatException) {
                        msg.sendMessage("画作的ID或索引格式有误,获取失败QAQ")
                    }
                }
            } else {
                try {
                    sendArtworkInformation(msg, rest.trim().toLong())
                } catch (e: NumberFormatException) {
                    msg.sendMessage("画作的ID格式有误,获取失败QAQ")
                }
            }
        }
    }

    @CommandMethod(path = "sendImg", asFallbackMethod = true)
    fun sendImages(event: GroupMessageEvent, rest: String) {
        check("batchImg")
        if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
            return
        }
        val count = rest.trim().toInt()
        if (count < 0) {
            event.sendGroupMsgAsync("发送负数数目的图片你是真心的嘛?")
            return
        }
        if (count > 16) {
            event.sendGroupMsgAsync("做人不能太贪心哦:图片数目必须是[0,16]范围内的整数")
            return
        }
        var executor: ThreadPoolExecutor
        synchronized(this) { executor = this.executor }
        val pendingTaskCount = executor.queue.size
        if (pendingTaskCount + count > 482) {
            event.sendGroupMsgAsync("现在我很忙呐,请稍后再找我吧~")
            return
        }
        for (i in 0 until count) {
            executor.execute(SendImageRunnable(event))
        }
    }

    @CommandMethod(path = "sendImg/recreate")
    @Synchronized
    fun shutdownThreadPool(event: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
            return
        }
        checkPermission(event)
        executor.shutdownNow()
        executor = ThreadPoolExecutor(4, 20, 5, TimeUnit.MINUTES, ArrayBlockingQueue(512))
        event.sendGroupMsgAsync("[TaskExecutor] 线程池已刷新")
    }

    //----------------------YuScript Commands----------------------------------
    @CommandMethod(path = "yuscript", asFallbackMethod = true)
    fun yuScriptFallback(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("yuscript")
        msg.sendMessage("yuscript:没有这样的命令啦~")
    }

    @CommandMethod(path = "yuscript/reset")
    fun resetVar(msg: GroupMessageEvent) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("yuscript")
        reset(msg)
    }

    @CommandMethod(path = "yuscript/Exec")
    fun runScriptUnlimited(msg: GroupMessageEvent, rest: String?) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        checkPermission(msg)
        runYuScriptUnlimited(msg, rest)
    }

    @CommandMethod(path = "yuscript/stop")
    fun stopSession(msg: GroupMessageEvent, rest: String) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        checkPermission(msg)
        interruptSession(msg, rest.trim().toInt())
    }

    @CommandMethod(path = "yuscript/exec")
    fun runScript(msg: GroupMessageEvent, rest: String?) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("yuscript")
        runYuScript(msg, rest)
    }

    //--------------------IP and Ping Commands------------------------------
    @CommandMethod(path = "ipList")
    @Throws(Throwable::class)
    fun fetchIpList(msg: GroupMessageEvent, rest: String?) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("ipList")
        runCommandGeneral(msg, rest!!)
    }

    @CommandMethod(path = "ipList4")
    @Throws(Throwable::class)
    fun fetchIpListV4(msg: GroupMessageEvent, rest: String?) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("ipList")
        runCommandV4(msg, rest!!)
    }

    @CommandMethod(path = "ipList6")
    @Throws(Throwable::class)
    fun fetchIpListV6(msg: GroupMessageEvent, rest: String?) {
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        check("ipList")
        runCommandV6(msg, rest!!)
    }

    @CommandMethod(path = "ping")
    @Throws(Throwable::class)
    fun ping(msg: GroupMessageEvent, rest: String) {
        check("ping")
        if (!config!!.getBoolean("miraiHandle") && msg.group.id == 655057127L) {
            return
        }
        val sentMsg: MessageReceipt<Contact> = msg.sendMessage("正在运行:ping " + rest.trim { it <= ' ' })
        runCommand(msg, rest.trim())
        runBlocking(msg.group.coroutineContext) {
            sentMsg.recall()
        }
    }

    //------------------Config Commands--------------------------
    private fun checkPermission(msg: GroupMessageEvent?) {
        if (msg != null && msg.sender.id != 2073412493L) {
            logger.debug("permission denied: sender violent. the sender is ${msg.sender.id}")
            throw Disabled.INSTANCE
        }
    }

    @CommandMethod(path = "config/reload")
    fun reloadConfig(msg: GroupMessageEvent?) {
        checkPermission(msg)
        loadConfig()
    }

    @CommandMethod(path = "config/unset")
    fun disableModule(msg: GroupMessageEvent?, rest: String) {
        checkPermission(msg)
        when (val rest2 = rest.trim()) {
            "yuscript", "ping", "ipList", "sendLocalImg", "pixiv", "batchImg", "atReply", "welcome", "muteTip", "miraiHandle", "reverseAtReply", "reverseAtReplyImage" -> {
                config!![rest2] = false
                config!!.save()
            }
            else -> {
                msg?.sendMessage("config unset:非法名称：$rest")
                logger.error("config unset:非法名称：$rest")
                return
            }
        }
        msg?.sendMessage("config unset:设置 $rest 的值为false")
        logger.info("config unset:设置 $rest 的值为false")
    }

    @CommandMethod(path = "config/set")
    fun enableModule(msg: GroupMessageEvent?, rest: String) {
        checkPermission(msg)
        when (val rest2 = rest.trim()) {
            "yuscript", "ping", "ipList", "sendLocalImg", "pixiv", "batchImg", "atReply", "welcome", "muteTip", "miraiHandle", "reverseAtReply", "reverseAtReplyImage" -> {
                config!![rest2] = true
                config!!.save()
            }
            else -> {
                msg?.sendMessage("config unset:非法名称：$rest")
                logger.error("config unset:非法名称：$rest")
                return
            }
        }
        msg?.sendMessage("config set:设置 $rest 的值为true")
        logger.info("config set:设置 $rest 的值为true")
    }

    @CommandMethod(path = "config", asFallbackMethod = true)
    fun configFallback(msg: GroupMessageEvent) {
        checkPermission(msg)
        msg.sendMessage("config:找不到该指令哦~")
    }

    @CommandMethod(path = "slightReload")
    fun slightReload(event: GroupMessageEvent?) {
        checkPermission(event)
        loadBasicConfig()
        event?.sendGroupMsg("[BotCore] 插件轻重载成功")
    }

    //-----------------------Plugin methods----------------------------
    private fun loadBasicConfig() {
        config = loadConfig("properties.yml")
        logger.info("Config loaded successfully")
        val modules = arrayOf(
            "yuscript",
            "ping",
            "ipList",
            "sendLocalImg",
            "pixiv",
            "batchImg",
            "atReply",
            "welcome",
            "muteTip",
            "reverseAtReply",
            "reverseAtReplyImage"
        )
        for (module in modules) {
            config!!.setIfAbsent(module, true)
        }
        config!!.setIfAbsent("cmdPrefix", "/")
        dispatcher.setCommandPrefix(config!!.getString("cmdPrefix"))
        config!!.setIfAbsent("imagePaths", ArrayList<String>())
        config!!.setIfAbsent("miraiHandle", false)
        config!!.setIfAbsent("imgRecallDelay", 40000L)
        config!!.setIfAbsent("recallMinPeriod", 200L)
        config!!.save()
    }

    private fun loadConfig() {
        loadBasicConfig()
        clear()
        for (path in config!!.getStringList("imagePaths")) {
            try {
                val f = File(path)
                if (f.exists()) {
                    addPathToList(f)
                }
            } catch (e: Exception) {
                logger.error("Unable to add path to image list:$path", e)
            }
        }
        logger.info("Image loaded:$imageCount")
    }

    private inner class SendImageRunnable constructor(val event: GroupMessageEvent) : Runnable {
        override fun run() {
            try {
                sendImageOnGroupMessage(event, config!!.getLong("imgRecallDelay"), recallManager)
            } catch (t: Throwable) {
                logger.warning("Image Sender Failure", t)
                sendErrorTrace(t, event)
            }
        }
    }

    private fun handleImageSend(event: GroupMessageEvent) {
        val hasLai = AtomicBoolean(false)
        val hasTu = AtomicBoolean(false)
        event.message.forEach { sub: SingleMessage ->
            if (sub is PlainText) {
                val content = sub.contentToString()
                if (content.contains("来")) {
                    hasLai.set(true)
                }
                if (content.contains("图")) {
                    hasTu.set(true)
                }
            }
        }
        if ((hasLai.get() && hasTu.get() || hasImage(
                event.message,
                "B407F708-A2C6-A506-3420-98DF7CAC4A57"
            )) && config!!.getBoolean("sendLocalImg")
        ) {
            if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
                return
            }
            synchronized(this@TestPlugin) { executor.execute(SendImageRunnable(event)) }
        }
    }

    private suspend fun handleAtReply(event: GroupMessageEvent) {
        if (config!!.getBoolean("atReply")) {
            val msg = event.message
            val beingCalled = AtomicBoolean(false)
            msg.forEach { subMsg: Message? ->
                if (subMsg is At && subMsg.target == event.bot.id) {
                    beingCalled.set(true)
                }
            }
            if (beingCalled.get()) {
                var chain = messageChainOf()
                var replyQuote: QuoteReply? = null
                var quoteIndex = -2
                for (i in 0 until msg.size) {
                    val sub = msg[i]
                    if (sub is MessageMetadata) {
                        if (sub is QuoteReply) {
                            if (sub.source.fromId == event.bot.id) {
                                replyQuote = QuoteReply(event.source)
                                quoteIndex = i
                            }
                        }
                        continue
                    }
                    if (sub is PlainText || sub is Face || sub is VipFace) {
                        chain = chain.plus(sub)
                    } else if (sub is AtAll) {
                        chain = chain.plus("@全体成员 ")
                    } else if (sub is At) {
                        if (sub.target == event.bot.id) {
                            if (i != quoteIndex + 1) {
                                chain = chain.plus(At(event.sender))
                            }
                        } else {
                            chain = chain.plus(sub)
                        }
                    } else if (sub is Image) {
                        try {
                            chain = if (config!!.getBoolean("reverseAtReplyImage")) chain.plus(
                                event.group.uploadImage(
                                    rotateImage(event.group, sub)
                                )
                            ) else chain.plus(sub)
                        } catch (e: IOException) {
                            chain = chain.plus(sub)
                            e.printStackTrace()
                        }
                    }
                }
                if (config!!.getBoolean("reverseAtReply")) {
                    val reversedChain = AtomicReference(messageChainOf())
                    chain.forEach(Consumer { element: SingleMessage? ->
                        if (element is PlainText) {
                            reversedChain.set(PlainText(StringBuilder(element.content).reverse()).plus(reversedChain.get()))
                        } else {
                            reversedChain.set(messageChainOf(element!!, reversedChain.get()))
                        }
                    })
                    chain = reversedChain.get()
                }
                if (replyQuote != null) {
                    chain = replyQuote.plus(chain)
                }
                event.group.sendMessage(chain)
            }
        }
    }

    override fun onLoad() {
        loadConfig()
        register(this, object : Command {
            override val name: String
                get() = "configRose"
            override val alias: List<String>
                get() = emptyList()
            override val description: String
                get() = "Change config of Rose's plugin"
            override val usage: String
                get() = ""

            override suspend fun onCommand(sender: CommandSender, args: List<String>): Boolean {
                return try {
                    when (args[0]) {
                        "reload" -> reloadConfig(null)
                        "set" -> enableModule(null, args[1])
                        "unset" -> disableModule(null, args[1])
                        "slightReload" -> slightReload(null)
                    }
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }


        })
    }

    private fun onGroupMessage(event: GroupMessageEvent) {
        try {
            dispatcher.dispatch(event)
            handleImageSend(event)
            runBlocking {
                handleAtReply(event)
            }
        } catch (throwable: Throwable) {
            if (!(throwable is Disabled || throwable.cause is Disabled)) {
                if (throwable is NumberFormatException || throwable.cause is NumberFormatException) {
                    event.sendGroupMsg("数字格式错误")
                }
                logger.info(throwable)
                sendErrorTrace(throwable, event)
            }
        }
    }

    private fun onMemberJoin(event: MemberJoinEvent) {
        if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
            return
        }
        if (config!!.getBoolean("welcome")) {
            event.group.launch {
                event.group.sendMessage("欢迎 ${event.member.nick} 入群~")
            }
        }
    }

    private fun onMemberQuit(event: Quit) {
        if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
            return
        }
        if (config!!.getBoolean("welcome")) {
            event.group.launch {
                event.group.sendMessage(event.member.nick + "(" + event.member.id + ") 退群了, 丢人!")
            }
        }
    }

    private fun onMemberMute(event: MemberMuteEvent) {
        if (!config!!.getBoolean("miraiHandle") && event.group.id == 655057127L) {
            return
        }
        if (config!!.getBoolean("muteTip")) {
            event.group.launch {
                event.group.sendMessage(
                    messageChainOf(
                        At(event.member),
                        PlainText("喝下了" + (if (if (event.operator == null) event.group.botAsMember.permission.level == MemberPermission.OWNER.level else event.operator!!.id == event.group.owner.id) "群主" else "管理") + "的红茶, 睡了过去")
                    )
                )
            }
        }
    }

    private var recallThread: Thread? = null
    override fun onEnable() {
        super.onEnable()

        eventListener.subscribeAlways(GroupMessageEvent::class.java, ::onGroupMessage)
        eventListener.subscribeAlways(MemberJoinEvent::class.java, ::onMemberJoin)
        eventListener.subscribeAlways(Quit::class.java, ::onMemberQuit)
        eventListener.subscribeAlways(MemberMuteEvent::class.java, ::onMemberMute)
        recallThread = Thread { recallManager.loop(logger, this) }
        recallThread!!.name = "RecallWorker"
        recallThread!!.priority = Thread.NORM_PRIORITY + 2
        recallThread!!.start()
    }

    override fun onDisable() {
        super.onDisable()
        recallThread!!.interrupt()
        recallThread = null
    }

    companion object {
        private fun hasImage(msg: MessageChain, id: String): Boolean {
            for (element in msg) {
                if (element is Image) {
                    if (element.imageId.contains(id)) {
                        return true
                    }
                }
            }
            return false
        }

        private val random = Random()

        @Throws(IOException::class)
        private fun rotateImage(gp: Group, image: Image): BufferedImage {
            return rotateImage(
                ImageIO.read(URL(runBlocking(gp.coroutineContext) { image.queryUrl() }).openConnection().getInputStream()),
                random.nextBoolean()
            )
        }
    }

    init {
        dispatcher.registerAllMethods(TestPlugin::class.java)
        dispatcher.target = this
        recallManager = RecallManager()
        executor = ThreadPoolExecutor(4, 20, 5, TimeUnit.MINUTES, ArrayBlockingQueue(512))
    }
}