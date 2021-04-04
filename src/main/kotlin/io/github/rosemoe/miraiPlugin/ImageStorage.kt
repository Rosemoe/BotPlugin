package io.github.rosemoe.miraiPlugin

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.json.JSONArray
import org.json.JSONObject
import org.mozilla.javascript.Context
import org.mozilla.javascript.ScriptableObject
import java.io.File
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.URL
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.random.Random

private val json = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    serializersModule = SerializersModule {
        polymorphic(ImageStorage::class) {
            subclass(LocalImageStorage::class)
            subclass(OnlineJsonImageStorage::class)
            subclass(ScriptProxyImageStorage::class)
        }
    }
}

fun ImageStorage.serializeStorage() : String {
    return json.encodeToString(this)
}

fun deserializeStorage(data: String) : ImageStorage {
    return json.decodeFromString(data)
}

@Serializable
data class StorageWrapper(@Polymorphic val storage: ImageStorage)

/**
 * Image Storage for sending images
 */
@Serializable
sealed class ImageStorage {

    @Required
    var storageName: String = "unnamed"

    /**
     * Get a random image from the source
     */
    open fun obtainImage(): ExternalResource? {
        return null
    }

    /**
     * initialize
     */
    open fun init() {

    }

}

@Serializable
@SerialName("local")
class LocalImageStorage(private val path: String) : ImageStorage() {

    @Transient
    private val file = File(path)
    @Transient
    private val images = ArrayList<File>()
    @Transient
    private val lock = ReentrantReadWriteLock()
    @Transient
    private val random = Random(System.nanoTime() + System.currentTimeMillis())

    init {
        check(file.exists()) { "File does not exist" }
    }

    private fun File.isImageFile(): Boolean {
        val lowerCase = name.toLowerCase()
        return lowerCase.endsWith(".jpg") || lowerCase.endsWith(".png") || lowerCase.endsWith(".bmp") || lowerCase.endsWith(
            ".webp"
        )
    }

    override fun init() {
        lock.lockWrite()
        try {
            images.clear()
            fun search(file: File) {
                if (file.isFile && file.isImageFile()) {
                    images.add(file)
                } else {
                    file.listFiles()?.forEach {
                        search(it)
                    }
                }
            }
            search(file)
        } finally {
            lock.unlockWrite()
        }
    }

    override fun obtainImage(): ExternalResource? {
        lock.lockRead()
        try {
            if (images.isEmpty()) {
                return null
            }
            return images[random.nextInt(0, images.size)].toExternalResource()
        } finally {
            lock.unlockRead()
        }
    }

}

@Serializable
@SerialName("json")
class OnlineJsonImageStorage(private val requestUrl: String, private val jsonPathForUrl: String) : ImageStorage() {

    @Transient
    private val pathList = jsonPathForUrl.split("\\")

    override fun obtainImage(): ExternalResource {
        var obj: Any = JSONObject(getWebpageSource(requestUrl))
        for (name in pathList) {
            if (obj is JSONObject) {
                obj = obj.get(name)
            } else if (obj is JSONArray) {
                obj = obj.get(name.toInt())
            } else if (obj is String) {
                break
            } else {
                throw IllegalArgumentException("Unable to extract content from ${obj}")
            }
        }
        return URL(obj.toString()).openConnection().getInputStream().toExternalResource()
    }

    override fun init() {
        // Empty
    }

}

@Serializable
@SerialName("proxy")
class ScriptProxyImageStorage(private val script: String) : ImageStorage() {

    override fun obtainImage(): ExternalResource? {
        val context = Context.enter()
        try {
            val script = context.compileString(script, "<user_script>", 1, null)
            val scope = context.initStandardObjects()
            fun setJsObject(name: String, value: Any?) {
                val jsObj = Context.javaToJS(value, scope)
                ScriptableObject.putProperty(scope, name, jsObj)
            }

            val result = script.exec(context, scope)
            if (result == null) {
                logWarning("ImageStorage named '${storageName}' attempted to execute its script but got null result")
                return null
            } else if (result is ExternalResource) {
                return result
            }
            logWarning("ImageStorage named '${storageName}' attempted to execute its script but got unexpected result type ${result.javaClass.name}")
            return null
        } catch (e: Exception) {
            logError("ImageStorage named '${storageName}' failed to execute its script", e)
        } finally {
            Context.exit()
        }
        return null
    }

    override fun init() {
        // Empty
    }

}