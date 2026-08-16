package org.legacy.scriptengine.kotlin.api

import org.legacy.scriptengine.kotlin.runtime.ScriptXKotlinRuntime
import kotlin.jvm.JvmName

internal interface LseNativeBacked {
    val rawNativeValue: Any?
}

internal object LseKotlinApiRuntime {
    private fun unwrap(value: Any?): Any? =
        (value as? LseNativeBacked)?.rawNativeValue ?: value

    private fun unwrapArgs(args: Array<out Any?>): Array<Any?> =
        args.map(::unwrap).toTypedArray()

    fun global(name: String, vararg args: Any?): Any? =
        ScriptXKotlinRuntime.call(name, *unwrapArgs(args)).wrap()

    fun static(className: String, name: String, vararg args: Any?): Any? =
        ScriptXKotlinRuntime.call("__scriptx_api_${className}_s_$name", *unwrapArgs(args)).wrap()

    fun instance(className: String, name: String, receiver: Any?, vararg args: Any?): Any? =
        ScriptXKotlinRuntime.callInstance(className, name, unwrap(receiver), *unwrapArgs(args)).wrap()

    fun property(className: String, name: String, receiver: Any?): Any? =
        instance(className, "${name}_get", receiver)

    fun setProperty(className: String, name: String, receiver: Any?, value: Any?) {
        instance(className, "${name}_set", receiver, value)
    }

    fun construct(className: String, vararg args: Any?): Any? =
        ScriptXKotlinRuntime.construct(className, *unwrapArgs(args)).wrap()

    internal fun wrapNative(value: Any?): Any? = value.wrap()

    private class ListenerAdapter(
        private val callback: Any,
        private val dispatcher: ScriptXKotlinRuntime.Dispatcher
    ) : Function<Any?> {
        fun invoke(args: Array<Any?>): Any? {
            return ScriptXKotlinRuntime.withDispatcher(dispatcher) {
                val wrapped = args.map { it.wrap() }.toTypedArray()
                val method = callback.javaClass.methods.firstOrNull {
                    it.name == "invoke" && it.parameterCount == wrapped.size
                } ?: error("Kotlin mc.listen callback arity is unsupported: ${wrapped.size}")
                method.isAccessible = true
                method.invoke(callback, *wrapped)
            }
        }
    }

    fun prepareListenerArgs(args: Array<out Any?>): Array<Any?> {
        val copy = Array<Any?>(args.size) { args[it] }
        if (args.size < 2 || args[1] !is Function<*>) return copy
        if (args[1]!!.javaClass.name == "ScriptXKotlinHost\$NativeFunction") return copy
        copy[1] = ListenerAdapter(args[1]!!, ScriptXKotlinRuntime.capture())
        return copy
    }

    private fun Any?.wrap(): Any? {
        if (this == null || this is LseNativeBacked) return this
        return when (ScriptXKotlinRuntime.nativeClassName(this)) {
            "NbtByte" -> NbtByte(this)
            "LLSE_Command" -> LLSE_Command(this)
            "ParticleSpawner" -> ParticleSpawner(this)
            "JsonConfigFile" -> JsonConfigFile(this)
            "DBSession" -> DBSession(this)
            "HttpRequest" -> HttpRequest(this)
            "NbtFloat" -> NbtFloat(this)
            "NbtList" -> NbtList(this)
            "BinaryStream" -> BinaryStream(this)
            "WSClient" -> WSClient(this)
            "DirectionAngle" -> DirectionAngle(this)
            "HttpServer" -> HttpServer(this)
            "LLSE_Objective" -> LLSE_Objective(this)
            "DBStmt" -> DBStmt(this)
            "LLSE_Block" -> LLSE_Block(this)
            "HttpResponse" -> HttpResponse(this)
            "IntPos" -> IntPos(this)
            "LLSE_Packet" -> LLSE_Packet(this)
            "LLSE_CommandOrigin" -> LLSE_CommandOrigin(this)
            "LLSE_BlockEntity" -> LLSE_BlockEntity(this)
            "File" -> File(this)
            "LLSE_Entity" -> LLSE_Entity(this)
            "IniConfigFile" -> IniConfigFile(this)
            "LLSE_SimpleForm" -> LLSE_SimpleForm(this)
            "LLSE_CommandOutput" -> LLSE_CommandOutput(this)
            "NbtCompound" -> NbtCompound(this)
            "LLSE_Device" -> LLSE_Device(this)
            "NbtInt" -> NbtInt(this)
            "LLSE_CustomForm" -> LLSE_CustomForm(this)
            "NbtLong" -> NbtLong(this)
            "LLSE_Item" -> LLSE_Item(this)
            "LLSE_Container" -> LLSE_Container(this)
            "NbtDouble" -> NbtDouble(this)
            "NbtString" -> NbtString(this)
            "NbtShort" -> NbtShort(this)
            "NbtByteArray" -> NbtByteArray(this)
            "LLSE_Player" -> LLSE_Player(this)
            "FloatPos" -> FloatPos(this)
            "KVDatabase" -> KVDatabase(this)
            else -> this
        }
    }
}

fun log(vararg args: Any?): Any? = LseKotlinApiRuntime.global("log", *args)
fun colorLog(vararg args: Any?): Any? = LseKotlinApiRuntime.global("colorLog", *args)
fun fastLog(vararg args: Any?): Any? = LseKotlinApiRuntime.global("fastLog", *args)
fun setTimeout(vararg args: Any?): Any? = LseKotlinApiRuntime.global("setTimeout", *args)
fun setInterval(vararg args: Any?): Any? = LseKotlinApiRuntime.global("setInterval", *args)
fun clearInterval(vararg args: Any?): Any? = LseKotlinApiRuntime.global("clearInterval", *args)

object mc {
    fun addPlayerScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "addPlayerScore", *args)
    fun broadcast(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "broadcast", *args)
    fun clearDisplayObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "clearDisplayObjective", *args)
    fun cloneMob(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "cloneMob", *args)
    fun crash(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "crash", *args)
    fun deletePlayerNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "deletePlayerNbt", *args)
    fun deletePlayerScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "deletePlayerScore", *args)
    fun explode(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "explode", *args)
    fun getAllEntities(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getAllEntities", *args)
    fun getAllPlayerUuids(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getAllPlayerUuids", *args)
    fun getAllScoreObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getAllScoreObjective", *args)
    fun getAllScoreObjectives(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getAllScoreObjectives", *args)
    fun getBDSVersion(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getBDSVersion", *args)
    fun getBlock(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getBlock", *args)
    fun getDimensionId(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getDimensionId", *args)
    fun getDimensionName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getDimensionName", *args)
    fun getDisplayObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getDisplayObjective", *args)
    fun getDisplayObjectives(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getDisplayObjectives", *args)
    fun getEntities(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getEntities", *args)
    fun getEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getEntity", *args)
    fun getMaxNumPlayers(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getMaxNumPlayers", *args)
    fun getMotd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getMotd", *args)
    fun getOnlinePlayerNum(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getOnlinePlayerNum", *args)
    fun getOnlinePlayers(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getOnlinePlayers", *args)
    fun getPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getPlayer", *args)
    fun getPlayerNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getPlayerNbt", *args)
    fun getPlayerScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getPlayerScore", *args)
    fun getScoreObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getScoreObjective", *args)
    fun getServerProtocolVersion(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getServerProtocolVersion", *args)
    fun getStructure(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getStructure", *args)
    fun getTime(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getTime", *args)
    fun getWeather(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "getWeather", *args)
    fun listen(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "listen", *LseKotlinApiRuntime.prepareListenerArgs(args))
    fun loadMob(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "loadMob", *args)
    fun newCommand(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newCommand", *args)
    fun newCustomForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newCustomForm", *args)
    fun newFloatPos(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newFloatPos", *args)
    fun newIntPos(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newIntPos", *args)
    fun newItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newItem", *args)
    fun newParticleSpawner(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newParticleSpawner", *args)
    fun newScoreObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newScoreObjective", *args)
    fun newSimpleForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "newSimpleForm", *args)
    fun reducePlayerScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "reducePlayerScore", *args)
    fun regConsoleCmd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "regConsoleCmd", *args)
    fun regPlayerCmd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "regPlayerCmd", *args)
    fun removeScoreObjective(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "removeScoreObjective", *args)
    fun runcmd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "runcmd", *args)
    fun runcmdEx(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "runcmdEx", *args)
    fun sendCmdOutput(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "sendCmdOutput", *args)
    fun setBlock(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setBlock", *args)
    fun setMaxPlayers(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setMaxPlayers", *args)
    fun setMotd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setMotd", *args)
    fun setPlayerNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setPlayerNbt", *args)
    fun setPlayerNbtTags(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setPlayerNbtTags", *args)
    fun setPlayerScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setPlayerScore", *args)
    fun setStructure(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setStructure", *args)
    fun setTime(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setTime", *args)
    fun setWeather(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "setWeather", *args)
    fun spawnItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "spawnItem", *args)
    fun spawnMob(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "spawnMob", *args)
    fun spawnParticle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "spawnParticle", *args)
    fun spawnSimulatedPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "spawnSimulatedPlayer", *args)
    fun summonMob(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("mc", "summonMob", *args)
}

open class NbtCompound internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtCompound =
            LseKotlinApiRuntime.construct("NbtCompound", *args) as NbtCompound
    }
    fun destroy(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "destroy", this, *args)
    fun getData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "getData", this, *args)
    fun getKeys(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "getKeys", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "getTag", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "getType", this, *args)
    fun getTypeOf(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "getTypeOf", this, *args)
    fun hasTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "hasTag", this, *args)
    fun removeTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "removeTag", this, *args)
    fun setByte(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setByte", this, *args)
    fun setByteArray(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setByteArray", this, *args)
    fun setDouble(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setDouble", this, *args)
    fun setEnd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setEnd", this, *args)
    fun setFloat(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setFloat", this, *args)
    fun setInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setInt", this, *args)
    fun setLong(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setLong", this, *args)
    fun setShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setShort", this, *args)
    fun setString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setString", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "setTag", this, *args)
    fun toBinaryNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "toBinaryNBT", this, *args)
    fun toObject(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "toObject", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtCompound", "toString", this, *args)
}

open class LLSE_CommandOutput internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addMessage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOutput", "addMessage", this, *args)
    fun error(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOutput", "error", this, *args)
    fun success(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOutput", "success", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOutput", "toString", this, *args)
    @get:JvmName("get_empty")
    @set:JvmName("set_empty")
    var empty: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOutput", "empty", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOutput", "empty", this, value) }
    @get:JvmName("get_successCount")
    @set:JvmName("set_successCount")
    var successCount: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOutput", "successCount", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOutput", "successCount", this, value) }
}

open class NbtInt internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtInt =
            LseKotlinApiRuntime.construct("NbtInt", *args) as NbtInt
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtInt", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtInt", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtInt", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtInt", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtInt", "toString", this, *args)
}

object ParticleColor {
    @get:JvmName("get_Apricot")
    @set:JvmName("set_Apricot")
    var Apricot: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Apricot_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Apricot_set", value) }
    @get:JvmName("get_Black")
    @set:JvmName("set_Black")
    var Black: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Black_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Black_set", value) }
    @get:JvmName("get_Cocoa")
    @set:JvmName("set_Cocoa")
    var Cocoa: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Cocoa_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Cocoa_set", value) }
    @get:JvmName("get_Dark")
    @set:JvmName("set_Dark")
    var Dark: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Dark_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Dark_set", value) }
    @get:JvmName("get_Fawn")
    @set:JvmName("set_Fawn")
    var Fawn: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Fawn_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Fawn_set", value) }
    @get:JvmName("get_Green")
    @set:JvmName("set_Green")
    var Green: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Green_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Green_set", value) }
    @get:JvmName("get_Indigo")
    @set:JvmName("set_Indigo")
    var Indigo: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Indigo_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Indigo_set", value) }
    @get:JvmName("get_Lavender")
    @set:JvmName("set_Lavender")
    var Lavender: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Lavender_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Lavender_set", value) }
    @get:JvmName("get_Oatmeal")
    @set:JvmName("set_Oatmeal")
    var Oatmeal: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Oatmeal_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Oatmeal_set", value) }
    @get:JvmName("get_Pink")
    @set:JvmName("set_Pink")
    var Pink: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Pink_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Pink_set", value) }
    @get:JvmName("get_Red")
    @set:JvmName("set_Red")
    var Red: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Red_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Red_set", value) }
    @get:JvmName("get_Slate")
    @set:JvmName("set_Slate")
    var Slate: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Slate_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Slate_set", value) }
    @get:JvmName("get_Teal")
    @set:JvmName("set_Teal")
    var Teal: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Teal_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Teal_set", value) }
    @get:JvmName("get_Vatblue")
    @set:JvmName("set_Vatblue")
    var Vatblue: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Vatblue_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Vatblue_set", value) }
    @get:JvmName("get_White")
    @set:JvmName("set_White")
    var White: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "White_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "White_set", value) }
    @get:JvmName("get_Yellow")
    @set:JvmName("set_Yellow")
    var Yellow: Any?
        get() = LseKotlinApiRuntime.static("ParticleColor", "Yellow_get")
        set(value) { LseKotlinApiRuntime.static("ParticleColor", "Yellow_set", value) }
}

open class LLSE_Device internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    @get:JvmName("get_avgPacketLoss")
    @set:JvmName("set_avgPacketLoss")
    var avgPacketLoss: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "avgPacketLoss", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "avgPacketLoss", this, value) }
    @get:JvmName("get_avgPing")
    @set:JvmName("set_avgPing")
    var avgPing: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "avgPing", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "avgPing", this, value) }
    @get:JvmName("get_clientId")
    @set:JvmName("set_clientId")
    var clientId: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "clientId", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "clientId", this, value) }
    @get:JvmName("get_inputMode")
    @set:JvmName("set_inputMode")
    var inputMode: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "inputMode", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "inputMode", this, value) }
    @get:JvmName("get_ip")
    @set:JvmName("set_ip")
    var ip: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "ip", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "ip", this, value) }
    @get:JvmName("get_lastPacketLoss")
    @set:JvmName("set_lastPacketLoss")
    var lastPacketLoss: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "lastPacketLoss", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "lastPacketLoss", this, value) }
    @get:JvmName("get_lastPing")
    @set:JvmName("set_lastPing")
    var lastPing: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "lastPing", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "lastPing", this, value) }
    @get:JvmName("get_os")
    @set:JvmName("set_os")
    var os: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "os", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "os", this, value) }
    @get:JvmName("get_serverAddress")
    @set:JvmName("set_serverAddress")
    var serverAddress: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Device", "serverAddress", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Device", "serverAddress", this, value) }
}

open class LLSE_SimpleForm internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addButton(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "addButton", this, *args)
    fun addDivider(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "addDivider", this, *args)
    fun addHeader(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "addHeader", this, *args)
    fun addLabel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "addLabel", this, *args)
    fun setContent(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "setContent", this, *args)
    fun setTitle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_SimpleForm", "setTitle", this, *args)
}

open class File internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): File =
            LseKotlinApiRuntime.construct("File", *args) as File
    }
    fun clear(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "clear", this, *args)
    fun close(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "close", this, *args)
    fun errorCode(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "errorCode", this, *args)
    fun flush(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "flush", this, *args)
    fun isEOF(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "isEOF", this, *args)
    fun read(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "read", this, *args)
    fun readAll(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "readAll", this, *args)
    fun readAllSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "readAllSync", this, *args)
    fun readLine(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "readLine", this, *args)
    fun readLineSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "readLineSync", this, *args)
    fun readSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "readSync", this, *args)
    fun seekTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "seekTo", this, *args)
    fun setSize(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "setSize", this, *args)
    fun write(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "write", this, *args)
    fun writeLine(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "writeLine", this, *args)
    fun writeLineSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "writeLineSync", this, *args)
    fun writeSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("File", "writeSync", this, *args)
    @get:JvmName("get_absolutePath")
    @set:JvmName("set_absolutePath")
    var absolutePath: Any?
        get() = LseKotlinApiRuntime.property("File", "absolutePath", this)
        set(value) { LseKotlinApiRuntime.setProperty("File", "absolutePath", this, value) }
    @get:JvmName("get_path")
    @set:JvmName("set_path")
    var path: Any?
        get() = LseKotlinApiRuntime.property("File", "path", this)
        set(value) { LseKotlinApiRuntime.setProperty("File", "path", this, value) }
    @get:JvmName("get_size")
    @set:JvmName("set_size")
    var size: Any?
        get() = LseKotlinApiRuntime.property("File", "size", this)
        set(value) { LseKotlinApiRuntime.setProperty("File", "size", this, value) }
}

open class LLSE_BlockEntity internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun getBlock(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_BlockEntity", "getBlock", this, *args)
    fun getCustomName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_BlockEntity", "getCustomName", this, *args)
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_BlockEntity", "getNbt", this, *args)
    fun setCustomName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_BlockEntity", "setCustomName", this, *args)
    fun setNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_BlockEntity", "setNbt", this, *args)
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_BlockEntity", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_BlockEntity", "name", this, value) }
    @get:JvmName("get_pos")
    @set:JvmName("set_pos")
    var pos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_BlockEntity", "pos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_BlockEntity", "pos", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_BlockEntity", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_BlockEntity", "type", this, value) }
}

object ll {
    fun checkVersion(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "checkVersion", *args)
    fun eval(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "eval", *args)
    fun exports(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "exports", *args)
    fun getAllPluginInfo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "getAllPluginInfo", *args)
    fun getCurrentPluginInfo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "getCurrentPluginInfo", *args)
    fun getPluginInfo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "getPluginInfo", *args)
    fun hasExported(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "hasExported", *args)
    fun imports(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "imports", *args)
    fun listPlugins(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "listPlugins", *args)
    fun onUnload(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "onUnload", *args)
    fun registerPlugin(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "registerPlugin", *args)
    fun require(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "require", *args)
    fun requireVersion(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "requireVersion", *args)
    fun version(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "version", *args)
    fun versionStatus(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "versionStatus", *args)
    fun versionString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("ll", "versionString", *args)
    @get:JvmName("get_isBeta")
    @set:JvmName("set_isBeta")
    var isBeta: Any?
        get() = LseKotlinApiRuntime.static("ll", "isBeta_get")
        set(value) { LseKotlinApiRuntime.static("ll", "isBeta_set", value) }
    @get:JvmName("get_isDebugMode")
    @set:JvmName("set_isDebugMode")
    var isDebugMode: Any?
        get() = LseKotlinApiRuntime.static("ll", "isDebugMode_get")
        set(value) { LseKotlinApiRuntime.static("ll", "isDebugMode_set", value) }
    @get:JvmName("get_isDev")
    @set:JvmName("set_isDev")
    var isDev: Any?
        get() = LseKotlinApiRuntime.static("ll", "isDev_get")
        set(value) { LseKotlinApiRuntime.static("ll", "isDev_set", value) }
    @get:JvmName("get_isRelease")
    @set:JvmName("set_isRelease")
    var isRelease: Any?
        get() = LseKotlinApiRuntime.static("ll", "isRelease_get")
        set(value) { LseKotlinApiRuntime.static("ll", "isRelease_set", value) }
    @get:JvmName("get_isWine")
    @set:JvmName("set_isWine")
    var isWine: Any?
        get() = LseKotlinApiRuntime.static("ll", "isWine_get")
        set(value) { LseKotlinApiRuntime.static("ll", "isWine_set", value) }
    @get:JvmName("get_language")
    @set:JvmName("set_language")
    var language: Any?
        get() = LseKotlinApiRuntime.static("ll", "language_get")
        set(value) { LseKotlinApiRuntime.static("ll", "language_set", value) }
    @get:JvmName("get_major")
    @set:JvmName("set_major")
    var major: Any?
        get() = LseKotlinApiRuntime.static("ll", "major_get")
        set(value) { LseKotlinApiRuntime.static("ll", "major_set", value) }
    @get:JvmName("get_minor")
    @set:JvmName("set_minor")
    var minor: Any?
        get() = LseKotlinApiRuntime.static("ll", "minor_get")
        set(value) { LseKotlinApiRuntime.static("ll", "minor_set", value) }
    @get:JvmName("get_pluginsRoot")
    @set:JvmName("set_pluginsRoot")
    var pluginsRoot: Any?
        get() = LseKotlinApiRuntime.static("ll", "pluginsRoot_get")
        set(value) { LseKotlinApiRuntime.static("ll", "pluginsRoot_set", value) }
    @get:JvmName("get_revision")
    @set:JvmName("set_revision")
    var revision: Any?
        get() = LseKotlinApiRuntime.static("ll", "revision_get")
        set(value) { LseKotlinApiRuntime.static("ll", "revision_set", value) }
    @get:JvmName("get_scriptEngineVersion")
    @set:JvmName("set_scriptEngineVersion")
    var scriptEngineVersion: Any?
        get() = LseKotlinApiRuntime.static("ll", "scriptEngineVersion_get")
        set(value) { LseKotlinApiRuntime.static("ll", "scriptEngineVersion_set", value) }
    @get:JvmName("get_status")
    @set:JvmName("set_status")
    var status: Any?
        get() = LseKotlinApiRuntime.static("ll", "status_get")
        set(value) { LseKotlinApiRuntime.static("ll", "status_set", value) }
}

object VaillanI18n {
    fun getCurrentLanguage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "getCurrentLanguage", *args)
    fun getSupportedLanguages(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "getSupportedLanguages", *args)
    fun loadLanguage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "loadLanguage", *args)
    fun loadLanguageFromFile(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "loadLanguageFromFile", *args)
    fun loadLanguagesFromDirectory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "loadLanguagesFromDirectory", *args)
    fun setCurrentLanguage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "setCurrentLanguage", *args)
    fun translate(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("VaillanI18n", "translate", *args)
}

open class IniConfigFile internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): IniConfigFile =
            LseKotlinApiRuntime.construct("IniConfigFile", *args) as IniConfigFile
    }
    fun delete(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "delete", this, *args)
    fun getBool(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "getBool", this, *args)
    fun getFloat(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "getFloat", this, *args)
    fun getInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "getInt", this, *args)
    fun getPath(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "getPath", this, *args)
    fun getStr(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "getStr", this, *args)
    fun init(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "init", this, *args)
    fun read(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "read", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "set", this, *args)
    fun write(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IniConfigFile", "write", this, *args)
}

open class LLSE_Entity internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addEffect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "addEffect", this, *args)
    fun addTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "addTag", this, *args)
    fun despawn(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "despawn", this, *args)
    fun distanceTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "distanceTo", this, *args)
    fun distanceToPos(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "distanceToPos", this, *args)
    fun distanceToSqr(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "distanceToSqr", this, *args)
    fun getAllEffects(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getAllEffects", this, *args)
    fun getAllTags(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getAllTags", this, *args)
    fun getArmor(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getArmor", this, *args)
    fun getBiomeId(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getBiomeId", this, *args)
    fun getBiomeName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getBiomeName", this, *args)
    fun getBlockFromViewVector(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getBlockFromViewVector", this, *args)
    fun getBlockStandingOn(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getBlockStandingOn", this, *args)
    fun getContainer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getContainer", this, *args)
    fun getCustomName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getCustomName", this, *args)
    fun getEntityFromViewVector(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getEntityFromViewVector", this, *args)
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getNbt", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "getTag", this, *args)
    fun hasContainer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "hasContainer", this, *args)
    fun hasTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "hasTag", this, *args)
    fun heal(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "heal", this, *args)
    fun hurt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "hurt", this, *args)
    fun isItemEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "isItemEntity", this, *args)
    fun isPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "isPlayer", this, *args)
    fun kill(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "kill", this, *args)
    fun quickEvalMolangScript(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "quickEvalMolangScript", this, *args)
    fun refreshItems(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "refreshItems", this, *args)
    fun remove(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "remove", this, *args)
    fun removeEffect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "removeEffect", this, *args)
    fun removeTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "removeTag", this, *args)
    fun setAbsorption(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setAbsorption", this, *args)
    fun setAttackDamage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setAttackDamage", this, *args)
    fun setCustomName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setCustomName", this, *args)
    fun setFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setFire", this, *args)
    fun setFollowRange(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setFollowRange", this, *args)
    fun setHealth(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setHealth", this, *args)
    fun setKnockbackResistance(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setKnockbackResistance", this, *args)
    fun setLavaMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setLavaMovementSpeed", this, *args)
    fun setLuck(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setLuck", this, *args)
    fun setMaxAttackDamage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setMaxAttackDamage", this, *args)
    fun setMaxHealth(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setMaxHealth", this, *args)
    fun setMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setMovementSpeed", this, *args)
    fun setNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setNbt", this, *args)
    fun setOnFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setOnFire", this, *args)
    fun setPosDelta(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setPosDelta", this, *args)
    fun setScale(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setScale", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setTag", this, *args)
    fun setUnderwaterMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "setUnderwaterMovementSpeed", this, *args)
    fun stopFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "stopFire", this, *args)
    fun teleport(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "teleport", this, *args)
    fun toItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "toItem", this, *args)
    fun toPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Entity", "toPlayer", this, *args)
    @get:JvmName("get_blockPos")
    @set:JvmName("set_blockPos")
    var blockPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "blockPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "blockPos", this, value) }
    @get:JvmName("get_canFly")
    @set:JvmName("set_canFly")
    var canFly: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "canFly", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "canFly", this, value) }
    @get:JvmName("get_canFreeze")
    @set:JvmName("set_canFreeze")
    var canFreeze: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "canFreeze", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "canFreeze", this, value) }
    @get:JvmName("get_canPickupItems")
    @set:JvmName("set_canPickupItems")
    var canPickupItems: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "canPickupItems", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "canPickupItems", this, value) }
    @get:JvmName("get_canSeeDaylight")
    @set:JvmName("set_canSeeDaylight")
    var canSeeDaylight: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "canSeeDaylight", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "canSeeDaylight", this, value) }
    @get:JvmName("get_direction")
    @set:JvmName("set_direction")
    var direction: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "direction", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "direction", this, value) }
    @get:JvmName("get_feetPos")
    @set:JvmName("set_feetPos")
    var feetPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "feetPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "feetPos", this, value) }
    @get:JvmName("get_health")
    @set:JvmName("set_health")
    var health: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "health", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "health", this, value) }
    @get:JvmName("get_id")
    @set:JvmName("set_id")
    var id: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "id", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "id", this, value) }
    @get:JvmName("get_inAir")
    @set:JvmName("set_inAir")
    var inAir: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inAir", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inAir", this, value) }
    @get:JvmName("get_inLava")
    @set:JvmName("set_inLava")
    var inLava: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inLava", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inLava", this, value) }
    @get:JvmName("get_inRain")
    @set:JvmName("set_inRain")
    var inRain: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inRain", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inRain", this, value) }
    @get:JvmName("get_inSnow")
    @set:JvmName("set_inSnow")
    var inSnow: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inSnow", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inSnow", this, value) }
    @get:JvmName("get_inWall")
    @set:JvmName("set_inWall")
    var inWall: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inWall", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inWall", this, value) }
    @get:JvmName("get_inWater")
    @set:JvmName("set_inWater")
    var inWater: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inWater", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inWater", this, value) }
    @get:JvmName("get_inWaterOrRain")
    @set:JvmName("set_inWaterOrRain")
    var inWaterOrRain: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inWaterOrRain", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inWaterOrRain", this, value) }
    @get:JvmName("get_inWorld")
    @set:JvmName("set_inWorld")
    var inWorld: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "inWorld", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "inWorld", this, value) }
    @get:JvmName("get_isAngry")
    @set:JvmName("set_isAngry")
    var isAngry: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isAngry", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isAngry", this, value) }
    @get:JvmName("get_isBaby")
    @set:JvmName("set_isBaby")
    var isBaby: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isBaby", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isBaby", this, value) }
    @get:JvmName("get_isDancing")
    @set:JvmName("set_isDancing")
    var isDancing: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isDancing", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isDancing", this, value) }
    @get:JvmName("get_isInsidePortal")
    @set:JvmName("set_isInsidePortal")
    var isInsidePortal: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isInsidePortal", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isInsidePortal", this, value) }
    @get:JvmName("get_isInvisible")
    @set:JvmName("set_isInvisible")
    var isInvisible: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isInvisible", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isInvisible", this, value) }
    @get:JvmName("get_isMoving")
    @set:JvmName("set_isMoving")
    var isMoving: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isMoving", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isMoving", this, value) }
    @get:JvmName("get_isOnFire")
    @set:JvmName("set_isOnFire")
    var isOnFire: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isOnFire", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isOnFire", this, value) }
    @get:JvmName("get_isOnGround")
    @set:JvmName("set_isOnGround")
    var isOnGround: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isOnGround", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isOnGround", this, value) }
    @get:JvmName("get_isOnHotBlock")
    @set:JvmName("set_isOnHotBlock")
    var isOnHotBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isOnHotBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isOnHotBlock", this, value) }
    @get:JvmName("get_isRiding")
    @set:JvmName("set_isRiding")
    var isRiding: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isRiding", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isRiding", this, value) }
    @get:JvmName("get_isSleeping")
    @set:JvmName("set_isSleeping")
    var isSleeping: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isSleeping", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isSleeping", this, value) }
    @get:JvmName("get_isTouchingDamageBlock")
    @set:JvmName("set_isTouchingDamageBlock")
    var isTouchingDamageBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isTouchingDamageBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isTouchingDamageBlock", this, value) }
    @get:JvmName("get_isTrading")
    @set:JvmName("set_isTrading")
    var isTrading: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isTrading", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isTrading", this, value) }
    @get:JvmName("get_isTrusting")
    @set:JvmName("set_isTrusting")
    var isTrusting: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "isTrusting", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "isTrusting", this, value) }
    @get:JvmName("get_maxHealth")
    @set:JvmName("set_maxHealth")
    var maxHealth: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "maxHealth", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "maxHealth", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "name", this, value) }
    @get:JvmName("get_pos")
    @set:JvmName("set_pos")
    var pos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "pos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "pos", this, value) }
    @get:JvmName("get_posDelta")
    @set:JvmName("set_posDelta")
    var posDelta: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "posDelta", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "posDelta", this, value) }
    @get:JvmName("get_runtimeId")
    @set:JvmName("set_runtimeId")
    var runtimeId: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "runtimeId", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "runtimeId", this, value) }
    @get:JvmName("get_speed")
    @set:JvmName("set_speed")
    var speed: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "speed", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "speed", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "type", this, value) }
    @get:JvmName("get_uniqueId")
    @set:JvmName("set_uniqueId")
    var uniqueId: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Entity", "uniqueId", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Entity", "uniqueId", this, value) }
}

open class LLSE_CustomForm internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addDivider(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addDivider", this, *args)
    fun addDropdown(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addDropdown", this, *args)
    fun addHeader(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addHeader", this, *args)
    fun addInput(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addInput", this, *args)
    fun addLabel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addLabel", this, *args)
    fun addSlider(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addSlider", this, *args)
    fun addStepSlider(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addStepSlider", this, *args)
    fun addSwitch(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "addSwitch", this, *args)
    fun setSubmitButton(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "setSubmitButton", this, *args)
    fun setTitle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CustomForm", "setTitle", this, *args)
}

object network {
    fun httpGet(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("network", "httpGet", *args)
    fun httpGetSync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("network", "httpGetSync", *args)
    fun httpPost(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("network", "httpPost", *args)
    fun newWebSocket(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("network", "newWebSocket", *args)
}

open class LLSE_Player internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addEffect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addEffect", this, *args)
    fun addExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addExperience", this, *args)
    fun addLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addLevel", this, *args)
    fun addMoney(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addMoney", this, *args)
    fun addScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addScore", this, *args)
    fun addTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "addTag", this, *args)
    fun clearItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "clearItem", this, *args)
    fun closeForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "closeForm", this, *args)
    fun crash(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "crash", this, *args)
    fun deleteScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "deleteScore", this, *args)
    fun delExtraData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "delExtraData", this, *args)
    fun disconnect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "disconnect", this, *args)
    fun distanceTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "distanceTo", this, *args)
    fun distanceToPos(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "distanceToPos", this, *args)
    fun distanceToSqr(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "distanceToSqr", this, *args)
    fun getAbilities(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getAbilities", this, *args)
    fun getAllEffects(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getAllEffects", this, *args)
    fun getAllItems(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getAllItems", this, *args)
    fun getAllTags(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getAllTags", this, *args)
    fun getArmor(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getArmor", this, *args)
    fun getAttributes(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getAttributes", this, *args)
    fun getBiomeId(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getBiomeId", this, *args)
    fun getBiomeName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getBiomeName", this, *args)
    fun getBlockFromViewVector(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getBlockFromViewVector", this, *args)
    fun getBlockStandingOn(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getBlockStandingOn", this, *args)
    fun getCurrentExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getCurrentExperience", this, *args)
    fun getDevice(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getDevice", this, *args)
    fun getEnderChest(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getEnderChest", this, *args)
    fun getEntityFromViewVector(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getEntityFromViewVector", this, *args)
    fun getExtraData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getExtraData", this, *args)
    fun getHand(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getHand", this, *args)
    fun getInventory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getInventory", this, *args)
    fun getLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getLevel", this, *args)
    fun getMoney(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getMoney", this, *args)
    fun getMoneyHistory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getMoneyHistory", this, *args)
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getNbt", this, *args)
    fun getOffHand(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getOffHand", this, *args)
    fun getRespawnPosition(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getRespawnPosition", this, *args)
    fun getScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getScore", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getTag", this, *args)
    fun getTotalExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getTotalExperience", this, *args)
    fun getXpNeededForNextLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "getXpNeededForNextLevel", this, *args)
    fun giveItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "giveItem", this, *args)
    fun hasTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "hasTag", this, *args)
    fun heal(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "heal", this, *args)
    fun hurt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "hurt", this, *args)
    fun isOP(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "isOP", this, *args)
    fun isSimulatedPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "isSimulatedPlayer", this, *args)
    fun isSprinting(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "isSprinting", this, *args)
    fun kick(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "kick", this, *args)
    fun kill(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "kill", this, *args)
    fun quickEvalMolangScript(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "quickEvalMolangScript", this, *args)
    fun reduceExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "reduceExperience", this, *args)
    fun reduceLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "reduceLevel", this, *args)
    fun reduceMoney(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "reduceMoney", this, *args)
    fun reduceScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "reduceScore", this, *args)
    fun refreshChunks(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "refreshChunks", this, *args)
    fun refreshItems(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "refreshItems", this, *args)
    fun removeBossBar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeBossBar", this, *args)
    fun removeEffect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeEffect", this, *args)
    fun removeItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeItem", this, *args)
    fun removeScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeScore", this, *args)
    fun removeSidebar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeSidebar", this, *args)
    fun removeTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "removeTag", this, *args)
    fun rename(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "rename", this, *args)
    fun resetLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "resetLevel", this, *args)
    fun runcmd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "runcmd", this, *args)
    fun sendCustomForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendCustomForm", this, *args)
    fun sendForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendForm", this, *args)
    fun sendModalForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendModalForm", this, *args)
    fun sendPacket(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendPacket", this, *args)
    fun sendSimpleForm(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendSimpleForm", this, *args)
    fun sendText(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendText", this, *args)
    fun sendToast(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "sendToast", this, *args)
    fun setAbility(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setAbility", this, *args)
    fun setAbsorption(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setAbsorption", this, *args)
    fun setAttackDamage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setAttackDamage", this, *args)
    fun setBossBar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setBossBar", this, *args)
    fun setCurrentExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setCurrentExperience", this, *args)
    fun setExtraData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setExtraData", this, *args)
    fun setFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setFire", this, *args)
    fun setFollowRange(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setFollowRange", this, *args)
    fun setGameMode(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setGameMode", this, *args)
    fun setHealth(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setHealth", this, *args)
    fun setHungry(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setHungry", this, *args)
    fun setKnockbackResistance(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setKnockbackResistance", this, *args)
    fun setLavaMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setLavaMovementSpeed", this, *args)
    fun setLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setLevel", this, *args)
    fun setLuck(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setLuck", this, *args)
    fun setMaxAttackDamage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setMaxAttackDamage", this, *args)
    fun setMaxHealth(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setMaxHealth", this, *args)
    fun setMoney(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setMoney", this, *args)
    fun setMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setMovementSpeed", this, *args)
    fun setNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setNbt", this, *args)
    fun setOnFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setOnFire", this, *args)
    fun setPermLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setPermLevel", this, *args)
    fun setRespawnPosition(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setRespawnPosition", this, *args)
    fun setScale(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setScale", this, *args)
    fun setScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setScore", this, *args)
    fun setSidebar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setSidebar", this, *args)
    fun setSprinting(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setSprinting", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setTag", this, *args)
    fun setTitle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setTitle", this, *args)
    fun setTotalExperience(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setTotalExperience", this, *args)
    fun setUnderwaterMovementSpeed(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "setUnderwaterMovementSpeed", this, *args)
    fun simulateAttack(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateAttack", this, *args)
    fun simulateDestroy(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateDestroy", this, *args)
    fun simulateDisconnect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateDisconnect", this, *args)
    fun simulateInteract(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateInteract", this, *args)
    fun simulateJump(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateJump", this, *args)
    fun simulateLocalMove(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateLocalMove", this, *args)
    fun simulateLookAt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateLookAt", this, *args)
    fun simulateMoveTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateMoveTo", this, *args)
    fun simulateNavigateTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateNavigateTo", this, *args)
    fun simulateRespawn(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateRespawn", this, *args)
    fun simulateSetBodyRotation(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateSetBodyRotation", this, *args)
    fun simulateSneak(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateSneak", this, *args)
    fun simulateStopDestroyingBlock(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateStopDestroyingBlock", this, *args)
    fun simulateStopInteracting(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateStopInteracting", this, *args)
    fun simulateStopMoving(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateStopMoving", this, *args)
    fun simulateStopSneaking(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateStopSneaking", this, *args)
    fun simulateStopUsingItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateStopUsingItem", this, *args)
    fun simulateUseItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateUseItem", this, *args)
    fun simulateWorldMove(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "simulateWorldMove", this, *args)
    fun stopFire(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "stopFire", this, *args)
    fun talkAs(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "talkAs", this, *args)
    fun talkTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "talkTo", this, *args)
    fun teleport(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "teleport", this, *args)
    fun tell(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "tell", this, *args)
    fun toEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "toEntity", this, *args)
    fun transMoney(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "transMoney", this, *args)
    fun transServer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Player", "transServer", this, *args)
    @get:JvmName("get_blockPos")
    @set:JvmName("set_blockPos")
    var blockPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "blockPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "blockPos", this, value) }
    @get:JvmName("get_canBeSeenOnMap")
    @set:JvmName("set_canBeSeenOnMap")
    var canBeSeenOnMap: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canBeSeenOnMap", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canBeSeenOnMap", this, value) }
    @get:JvmName("get_canFly")
    @set:JvmName("set_canFly")
    var canFly: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canFly", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canFly", this, value) }
    @get:JvmName("get_canFreeze")
    @set:JvmName("set_canFreeze")
    var canFreeze: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canFreeze", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canFreeze", this, value) }
    @get:JvmName("get_canPickupItems")
    @set:JvmName("set_canPickupItems")
    var canPickupItems: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canPickupItems", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canPickupItems", this, value) }
    @get:JvmName("get_canSeeDaylight")
    @set:JvmName("set_canSeeDaylight")
    var canSeeDaylight: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canSeeDaylight", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canSeeDaylight", this, value) }
    @get:JvmName("get_canShowNameTag")
    @set:JvmName("set_canShowNameTag")
    var canShowNameTag: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canShowNameTag", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canShowNameTag", this, value) }
    @get:JvmName("get_canSleep")
    @set:JvmName("set_canSleep")
    var canSleep: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canSleep", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canSleep", this, value) }
    @get:JvmName("get_canStartSleepInBed")
    @set:JvmName("set_canStartSleepInBed")
    var canStartSleepInBed: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "canStartSleepInBed", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "canStartSleepInBed", this, value) }
    @get:JvmName("get_direction")
    @set:JvmName("set_direction")
    var direction: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "direction", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "direction", this, value) }
    @get:JvmName("get_feetPos")
    @set:JvmName("set_feetPos")
    var feetPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "feetPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "feetPos", this, value) }
    @get:JvmName("get_gameMode")
    @set:JvmName("set_gameMode")
    var gameMode: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "gameMode", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "gameMode", this, value) }
    @get:JvmName("get_health")
    @set:JvmName("set_health")
    var health: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "health", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "health", this, value) }
    @get:JvmName("get_inAir")
    @set:JvmName("set_inAir")
    var inAir: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inAir", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inAir", this, value) }
    @get:JvmName("get_inClouds")
    @set:JvmName("set_inClouds")
    var inClouds: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inClouds", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inClouds", this, value) }
    @get:JvmName("get_inLava")
    @set:JvmName("set_inLava")
    var inLava: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inLava", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inLava", this, value) }
    @get:JvmName("get_inRain")
    @set:JvmName("set_inRain")
    var inRain: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inRain", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inRain", this, value) }
    @get:JvmName("get_inSnow")
    @set:JvmName("set_inSnow")
    var inSnow: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inSnow", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inSnow", this, value) }
    @get:JvmName("get_inWall")
    @set:JvmName("set_inWall")
    var inWall: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inWall", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inWall", this, value) }
    @get:JvmName("get_inWater")
    @set:JvmName("set_inWater")
    var inWater: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inWater", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inWater", this, value) }
    @get:JvmName("get_inWaterOrRain")
    @set:JvmName("set_inWaterOrRain")
    var inWaterOrRain: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inWaterOrRain", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inWaterOrRain", this, value) }
    @get:JvmName("get_inWorld")
    @set:JvmName("set_inWorld")
    var inWorld: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "inWorld", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "inWorld", this, value) }
    @get:JvmName("get_ip")
    @set:JvmName("set_ip")
    var ip: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "ip", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "ip", this, value) }
    @get:JvmName("get_isAdventure")
    @set:JvmName("set_isAdventure")
    var isAdventure: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isAdventure", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isAdventure", this, value) }
    @get:JvmName("get_isCrawling")
    @set:JvmName("set_isCrawling")
    var isCrawling: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isCrawling", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isCrawling", this, value) }
    @get:JvmName("get_isCreative")
    @set:JvmName("set_isCreative")
    var isCreative: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isCreative", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isCreative", this, value) }
    @get:JvmName("get_isDancing")
    @set:JvmName("set_isDancing")
    var isDancing: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isDancing", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isDancing", this, value) }
    @get:JvmName("get_isFlying")
    @set:JvmName("set_isFlying")
    var isFlying: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isFlying", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isFlying", this, value) }
    @get:JvmName("get_isGliding")
    @set:JvmName("set_isGliding")
    var isGliding: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isGliding", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isGliding", this, value) }
    @get:JvmName("get_isHungry")
    @set:JvmName("set_isHungry")
    var isHungry: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isHungry", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isHungry", this, value) }
    @get:JvmName("get_isHurt")
    @set:JvmName("set_isHurt")
    var isHurt: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isHurt", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isHurt", this, value) }
    @get:JvmName("get_isInsidePortal")
    @set:JvmName("set_isInsidePortal")
    var isInsidePortal: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isInsidePortal", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isInsidePortal", this, value) }
    @get:JvmName("get_isInvisible")
    @set:JvmName("set_isInvisible")
    var isInvisible: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isInvisible", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isInvisible", this, value) }
    @get:JvmName("get_isLoading")
    @set:JvmName("set_isLoading")
    var isLoading: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isLoading", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isLoading", this, value) }
    @get:JvmName("get_isMoving")
    @set:JvmName("set_isMoving")
    var isMoving: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isMoving", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isMoving", this, value) }
    @get:JvmName("get_isOnFire")
    @set:JvmName("set_isOnFire")
    var isOnFire: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isOnFire", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isOnFire", this, value) }
    @get:JvmName("get_isOnGround")
    @set:JvmName("set_isOnGround")
    var isOnGround: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isOnGround", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isOnGround", this, value) }
    @get:JvmName("get_isOnHotBlock")
    @set:JvmName("set_isOnHotBlock")
    var isOnHotBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isOnHotBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isOnHotBlock", this, value) }
    @get:JvmName("get_isRiding")
    @set:JvmName("set_isRiding")
    var isRiding: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isRiding", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isRiding", this, value) }
    @get:JvmName("get_isSleeping")
    @set:JvmName("set_isSleeping")
    var isSleeping: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isSleeping", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isSleeping", this, value) }
    @get:JvmName("get_isSneaking")
    @set:JvmName("set_isSneaking")
    var isSneaking: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isSneaking", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isSneaking", this, value) }
    @get:JvmName("get_isSpectator")
    @set:JvmName("set_isSpectator")
    var isSpectator: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isSpectator", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isSpectator", this, value) }
    @get:JvmName("get_isSurvival")
    @set:JvmName("set_isSurvival")
    var isSurvival: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isSurvival", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isSurvival", this, value) }
    @get:JvmName("get_isSwimming")
    @set:JvmName("set_isSwimming")
    var isSwimming: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isSwimming", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isSwimming", this, value) }
    @get:JvmName("get_isTouchingDamageBlock")
    @set:JvmName("set_isTouchingDamageBlock")
    var isTouchingDamageBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isTouchingDamageBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isTouchingDamageBlock", this, value) }
    @get:JvmName("get_isTrading")
    @set:JvmName("set_isTrading")
    var isTrading: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isTrading", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isTrading", this, value) }
    @get:JvmName("get_isTrusting")
    @set:JvmName("set_isTrusting")
    var isTrusting: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "isTrusting", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "isTrusting", this, value) }
    @get:JvmName("get_langCode")
    @set:JvmName("set_langCode")
    var langCode: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "langCode", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "langCode", this, value) }
    @get:JvmName("get_lastDeathPos")
    @set:JvmName("set_lastDeathPos")
    var lastDeathPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "lastDeathPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "lastDeathPos", this, value) }
    @get:JvmName("get_maxHealth")
    @set:JvmName("set_maxHealth")
    var maxHealth: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "maxHealth", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "maxHealth", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "name", this, value) }
    @get:JvmName("get_permLevel")
    @set:JvmName("set_permLevel")
    var permLevel: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "permLevel", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "permLevel", this, value) }
    @get:JvmName("get_pos")
    @set:JvmName("set_pos")
    var pos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "pos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "pos", this, value) }
    @get:JvmName("get_realName")
    @set:JvmName("set_realName")
    var realName: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "realName", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "realName", this, value) }
    @get:JvmName("get_runtimeId")
    @set:JvmName("set_runtimeId")
    var runtimeId: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "runtimeId", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "runtimeId", this, value) }
    @get:JvmName("get_sneaking")
    @set:JvmName("set_sneaking")
    var sneaking: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "sneaking", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "sneaking", this, value) }
    @get:JvmName("get_speed")
    @set:JvmName("set_speed")
    var speed: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "speed", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "speed", this, value) }
    @get:JvmName("get_uniqueId")
    @set:JvmName("set_uniqueId")
    var uniqueId: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "uniqueId", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "uniqueId", this, value) }
    @get:JvmName("get_uuid")
    @set:JvmName("set_uuid")
    var uuid: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "uuid", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "uuid", this, value) }
    @get:JvmName("get_xuid")
    @set:JvmName("set_xuid")
    var xuid: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Player", "xuid", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Player", "xuid", this, value) }
}

open class NbtByteArray internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtByteArray =
            LseKotlinApiRuntime.construct("NbtByteArray", *args) as NbtByteArray
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByteArray", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByteArray", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByteArray", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByteArray", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByteArray", "toString", this, *args)
}

object Direction {
    @get:JvmName("get_NEG_X")
    @set:JvmName("set_NEG_X")
    var NEG_X: Any?
        get() = LseKotlinApiRuntime.static("Direction", "NEG_X_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "NEG_X_set", value) }
    @get:JvmName("get_NEG_Y")
    @set:JvmName("set_NEG_Y")
    var NEG_Y: Any?
        get() = LseKotlinApiRuntime.static("Direction", "NEG_Y_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "NEG_Y_set", value) }
    @get:JvmName("get_NEG_Z")
    @set:JvmName("set_NEG_Z")
    var NEG_Z: Any?
        get() = LseKotlinApiRuntime.static("Direction", "NEG_Z_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "NEG_Z_set", value) }
    @get:JvmName("get_POS_X")
    @set:JvmName("set_POS_X")
    var POS_X: Any?
        get() = LseKotlinApiRuntime.static("Direction", "POS_X_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "POS_X_set", value) }
    @get:JvmName("get_POS_Y")
    @set:JvmName("set_POS_Y")
    var POS_Y: Any?
        get() = LseKotlinApiRuntime.static("Direction", "POS_Y_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "POS_Y_set", value) }
    @get:JvmName("get_POS_Z")
    @set:JvmName("set_POS_Z")
    var POS_Z: Any?
        get() = LseKotlinApiRuntime.static("Direction", "POS_Z_get")
        set(value) { LseKotlinApiRuntime.static("Direction", "POS_Z_set", value) }
}

open class KVDatabase internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): KVDatabase =
            LseKotlinApiRuntime.construct("KVDatabase", *args) as KVDatabase
    }
    fun close(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("KVDatabase", "close", this, *args)
    fun delete(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("KVDatabase", "delete", this, *args)
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("KVDatabase", "get", this, *args)
    fun listKey(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("KVDatabase", "listKey", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("KVDatabase", "set", this, *args)
}

open class FloatPos internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): FloatPos =
            LseKotlinApiRuntime.construct("FloatPos", *args) as FloatPos
    }
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("FloatPos", "toString", this, *args)
    @get:JvmName("get_dim")
    @set:JvmName("set_dim")
    var dim: Any?
        get() = LseKotlinApiRuntime.property("FloatPos", "dim", this)
        set(value) { LseKotlinApiRuntime.setProperty("FloatPos", "dim", this, value) }
    @get:JvmName("get_dimid")
    @set:JvmName("set_dimid")
    var dimid: Any?
        get() = LseKotlinApiRuntime.property("FloatPos", "dimid", this)
        set(value) { LseKotlinApiRuntime.setProperty("FloatPos", "dimid", this, value) }
    @get:JvmName("get_x")
    @set:JvmName("set_x")
    var x: Any?
        get() = LseKotlinApiRuntime.property("FloatPos", "x", this)
        set(value) { LseKotlinApiRuntime.setProperty("FloatPos", "x", this, value) }
    @get:JvmName("get_y")
    @set:JvmName("set_y")
    var y: Any?
        get() = LseKotlinApiRuntime.property("FloatPos", "y", this)
        set(value) { LseKotlinApiRuntime.setProperty("FloatPos", "y", this, value) }
    @get:JvmName("get_z")
    @set:JvmName("set_z")
    var z: Any?
        get() = LseKotlinApiRuntime.property("FloatPos", "z", this)
        set(value) { LseKotlinApiRuntime.setProperty("FloatPos", "z", this, value) }
}

open class NbtShort internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtShort =
            LseKotlinApiRuntime.construct("NbtShort", *args) as NbtShort
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtShort", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtShort", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtShort", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtShort", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtShort", "toString", this, *args)
}

open class LLSE_Container internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "addItem", this, *args)
    fun addItemToFirstEmptySlot(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "addItemToFirstEmptySlot", this, *args)
    fun getAllItems(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "getAllItems", this, *args)
    fun getAllSlots(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "getAllSlots", this, *args)
    fun getItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "getItem", this, *args)
    fun getSlot(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "getSlot", this, *args)
    fun hasRoomFor(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "hasRoomFor", this, *args)
    fun isEmpty(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "isEmpty", this, *args)
    fun removeAllItems(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "removeAllItems", this, *args)
    fun removeItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "removeItem", this, *args)
    fun setItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Container", "setItem", this, *args)
    @get:JvmName("get_size")
    @set:JvmName("set_size")
    var size: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Container", "size", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Container", "size", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Container", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Container", "type", this, value) }
}

open class LLSE_Item internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addCount(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "addCount", this, *args)
    fun clone(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "clone", this, *args)
    fun getDisplayName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "getDisplayName", this, *args)
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "getNbt", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "getTag", this, *args)
    fun isNull(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "isNull", this, *args)
    fun match(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "match", this, *args)
    fun removeCount(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "removeCount", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "set", this, *args)
    fun setAux(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setAux", this, *args)
    fun setCount(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setCount", this, *args)
    fun setDamage(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setDamage", this, *args)
    fun setDisplayName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setDisplayName", this, *args)
    fun setLore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setLore", this, *args)
    fun setNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setNbt", this, *args)
    fun setNull(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setNull", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Item", "setTag", this, *args)
    @get:JvmName("get_attackDamage")
    @set:JvmName("set_attackDamage")
    var attackDamage: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "attackDamage", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "attackDamage", this, value) }
    @get:JvmName("get_aux")
    @set:JvmName("set_aux")
    var aux: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "aux", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "aux", this, value) }
    @get:JvmName("get_count")
    @set:JvmName("set_count")
    var count: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "count", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "count", this, value) }
    @get:JvmName("get_damage")
    @set:JvmName("set_damage")
    var damage: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "damage", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "damage", this, value) }
    @get:JvmName("get_id")
    @set:JvmName("set_id")
    var id: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "id", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "id", this, value) }
    @get:JvmName("get_isArmorItem")
    @set:JvmName("set_isArmorItem")
    var isArmorItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isArmorItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isArmorItem", this, value) }
    @get:JvmName("get_isBlock")
    @set:JvmName("set_isBlock")
    var isBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isBlock", this, value) }
    @get:JvmName("get_isDamageableItem")
    @set:JvmName("set_isDamageableItem")
    var isDamageableItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isDamageableItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isDamageableItem", this, value) }
    @get:JvmName("get_isDamaged")
    @set:JvmName("set_isDamaged")
    var isDamaged: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isDamaged", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isDamaged", this, value) }
    @get:JvmName("get_isEnchanted")
    @set:JvmName("set_isEnchanted")
    var isEnchanted: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isEnchanted", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isEnchanted", this, value) }
    @get:JvmName("get_isEnchantingBook")
    @set:JvmName("set_isEnchantingBook")
    var isEnchantingBook: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isEnchantingBook", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isEnchantingBook", this, value) }
    @get:JvmName("get_isFireResistant")
    @set:JvmName("set_isFireResistant")
    var isFireResistant: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isFireResistant", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isFireResistant", this, value) }
    @get:JvmName("get_isFullStack")
    @set:JvmName("set_isFullStack")
    var isFullStack: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isFullStack", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isFullStack", this, value) }
    @get:JvmName("get_isGlint")
    @set:JvmName("set_isGlint")
    var isGlint: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isGlint", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isGlint", this, value) }
    @get:JvmName("get_isHorseArmorItem")
    @set:JvmName("set_isHorseArmorItem")
    var isHorseArmorItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isHorseArmorItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isHorseArmorItem", this, value) }
    @get:JvmName("get_isLiquidClipItem")
    @set:JvmName("set_isLiquidClipItem")
    var isLiquidClipItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isLiquidClipItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isLiquidClipItem", this, value) }
    @get:JvmName("get_isMusicDiscItem")
    @set:JvmName("set_isMusicDiscItem")
    var isMusicDiscItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isMusicDiscItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isMusicDiscItem", this, value) }
    @get:JvmName("get_isOffhandItem")
    @set:JvmName("set_isOffhandItem")
    var isOffhandItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isOffhandItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isOffhandItem", this, value) }
    @get:JvmName("get_isPotionItem")
    @set:JvmName("set_isPotionItem")
    var isPotionItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isPotionItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isPotionItem", this, value) }
    @get:JvmName("get_isStackable")
    @set:JvmName("set_isStackable")
    var isStackable: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isStackable", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isStackable", this, value) }
    @get:JvmName("get_isWearableItem")
    @set:JvmName("set_isWearableItem")
    var isWearableItem: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "isWearableItem", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "isWearableItem", this, value) }
    @get:JvmName("get_lore")
    @set:JvmName("set_lore")
    var lore: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "lore", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "lore", this, value) }
    @get:JvmName("get_maxCount")
    @set:JvmName("set_maxCount")
    var maxCount: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "maxCount", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "maxCount", this, value) }
    @get:JvmName("get_maxDamage")
    @set:JvmName("set_maxDamage")
    var maxDamage: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "maxDamage", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "maxDamage", this, value) }
    @get:JvmName("get_maxStackSize")
    @set:JvmName("set_maxStackSize")
    var maxStackSize: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "maxStackSize", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "maxStackSize", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "name", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Item", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Item", "type", this, value) }
}

open class NbtLong internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtLong =
            LseKotlinApiRuntime.construct("NbtLong", *args) as NbtLong
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtLong", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtLong", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtLong", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtLong", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtLong", "toString", this, *args)
}

open class NbtString internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtString =
            LseKotlinApiRuntime.construct("NbtString", *args) as NbtString
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtString", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtString", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtString", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtString", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtString", "toString", this, *args)
}

object logger {
    fun debug(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "debug", *args)
    fun error(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "error", *args)
    fun fatal(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "fatal", *args)
    fun info(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "info", *args)
    fun log(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "log", *args)
    fun setConsole(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "setConsole", *args)
    fun setFile(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "setFile", *args)
    fun setLogLevel(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "setLogLevel", *args)
    fun setPlayer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "setPlayer", *args)
    fun setTitle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "setTitle", *args)
    fun warn(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "warn", *args)
    fun warning(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("logger", "warning", *args)
}

open class NbtDouble internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtDouble =
            LseKotlinApiRuntime.construct("NbtDouble", *args) as NbtDouble
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtDouble", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtDouble", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtDouble", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtDouble", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtDouble", "toString", this, *args)
}

open class HttpRequest internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun getHeader(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpRequest", "getHeader", this, *args)
    @get:JvmName("get_body")
    @set:JvmName("set_body")
    var body: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "body", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "body", this, value) }
    @get:JvmName("get_headers")
    @set:JvmName("set_headers")
    var headers: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "headers", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "headers", this, value) }
    @get:JvmName("get_matches")
    @set:JvmName("set_matches")
    var matches: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "matches", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "matches", this, value) }
    @get:JvmName("get_method")
    @set:JvmName("set_method")
    var method: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "method", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "method", this, value) }
    @get:JvmName("get_params")
    @set:JvmName("set_params")
    var params: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "params", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "params", this, value) }
    @get:JvmName("get_path")
    @set:JvmName("set_path")
    var path: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "path", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "path", this, value) }
    @get:JvmName("get_query")
    @set:JvmName("set_query")
    var query: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "query", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "query", this, value) }
    @get:JvmName("get_remoteAddr")
    @set:JvmName("set_remoteAddr")
    var remoteAddr: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "remoteAddr", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "remoteAddr", this, value) }
    @get:JvmName("get_remotePort")
    @set:JvmName("set_remotePort")
    var remotePort: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "remotePort", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "remotePort", this, value) }
    @get:JvmName("get_version")
    @set:JvmName("set_version")
    var version: Any?
        get() = LseKotlinApiRuntime.property("HttpRequest", "version", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpRequest", "version", this, value) }
}

open class DBSession internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): DBSession =
            LseKotlinApiRuntime.construct("DBSession", *args) as DBSession
    }
    fun backup(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "backup", this, *args)
    fun close(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "close", this, *args)
    fun exec(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "exec", this, *args)
    fun execute(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "execute", this, *args)
    fun isOpen(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "isOpen", this, *args)
    fun prepare(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "prepare", this, *args)
    fun query(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBSession", "query", this, *args)
}

object money {
    fun add(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "add", *args)
    fun clearHistory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "clearHistory", *args)
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "get", *args)
    fun getHistory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "getHistory", *args)
    fun reduce(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "reduce", *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "set", *args)
    fun trans(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("money", "trans", *args)
}

open class BinaryStream internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): BinaryStream =
            LseKotlinApiRuntime.construct("BinaryStream", *args) as BinaryStream
    }
    fun createPacket(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "createPacket", this, *args)
    fun getData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "getData", this, *args)
    fun getReadPointer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "getReadPointer", this, *args)
    fun readBool(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readBool", this, *args)
    fun readByte(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readByte", this, *args)
    fun readBytes(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readBytes", this, *args)
    fun readDouble(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readDouble", this, *args)
    fun readFloat(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readFloat", this, *args)
    fun readSignedBigEndianInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readSignedBigEndianInt", this, *args)
    fun readSignedInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readSignedInt", this, *args)
    fun readSignedInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readSignedInt64", this, *args)
    fun readSignedShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readSignedShort", this, *args)
    fun readString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readString", this, *args)
    fun readUnsignedChar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedChar", this, *args)
    fun readUnsignedInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedInt", this, *args)
    fun readUnsignedInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedInt64", this, *args)
    fun readUnsignedShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedShort", this, *args)
    fun readUnsignedVarInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedVarInt", this, *args)
    fun readUnsignedVarInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readUnsignedVarInt64", this, *args)
    fun readVarInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readVarInt", this, *args)
    fun readVarInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "readVarInt64", this, *args)
    fun reserve(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "reserve", this, *args)
    fun reset(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "reset", this, *args)
    fun setData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "setData", this, *args)
    fun setReadPointer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "setReadPointer", this, *args)
    fun writeBlockPos(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeBlockPos", this, *args)
    fun writeBool(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeBool", this, *args)
    fun writeByte(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeByte", this, *args)
    fun writeBytes(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeBytes", this, *args)
    fun writeCompoundTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeCompoundTag", this, *args)
    fun writeDouble(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeDouble", this, *args)
    fun writeFloat(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeFloat", this, *args)
    fun writeItem(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeItem", this, *args)
    fun writeSignedBigEndianInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeSignedBigEndianInt", this, *args)
    fun writeSignedInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeSignedInt", this, *args)
    fun writeSignedInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeSignedInt64", this, *args)
    fun writeSignedShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeSignedShort", this, *args)
    fun writeString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeString", this, *args)
    fun writeUnsignedChar(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedChar", this, *args)
    fun writeUnsignedInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedInt", this, *args)
    fun writeUnsignedInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedInt64", this, *args)
    fun writeUnsignedShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedShort", this, *args)
    fun writeUnsignedVarInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedVarInt", this, *args)
    fun writeUnsignedVarInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUnsignedVarInt64", this, *args)
    fun writeUuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeUuid", this, *args)
    fun writeVarInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeVarInt", this, *args)
    fun writeVarInt64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeVarInt64", this, *args)
    fun writeVec3(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("BinaryStream", "writeVec3", this, *args)
}

open class NbtList internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtList =
            LseKotlinApiRuntime.construct("NbtList", *args) as NbtList
    }
    fun addTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "addTag", this, *args)
    fun getData(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "getData", this, *args)
    fun getSize(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "getSize", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "getTag", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "getType", this, *args)
    fun getTypeOf(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "getTypeOf", this, *args)
    fun removeTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "removeTag", this, *args)
    fun setByte(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setByte", this, *args)
    fun setByteArray(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setByteArray", this, *args)
    fun setDouble(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setDouble", this, *args)
    fun setEnd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setEnd", this, *args)
    fun setFloat(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setFloat", this, *args)
    fun setInt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setInt", this, *args)
    fun setLong(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setLong", this, *args)
    fun setShort(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setShort", this, *args)
    fun setString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setString", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "setTag", this, *args)
    fun toArray(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "toArray", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtList", "toString", this, *args)
}

open class NbtFloat internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtFloat =
            LseKotlinApiRuntime.construct("NbtFloat", *args) as NbtFloat
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtFloat", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtFloat", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtFloat", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtFloat", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtFloat", "toString", this, *args)
}

object SnbtFormat {
    @get:JvmName("get_AlwaysLineFeed")
    @set:JvmName("set_AlwaysLineFeed")
    var AlwaysLineFeed: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "AlwaysLineFeed_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "AlwaysLineFeed_set", value) }
    @get:JvmName("get_ArrayLineFeed")
    @set:JvmName("set_ArrayLineFeed")
    var ArrayLineFeed: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "ArrayLineFeed_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "ArrayLineFeed_set", value) }
    @get:JvmName("get_Colored")
    @set:JvmName("set_Colored")
    var Colored: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "Colored_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "Colored_set", value) }
    @get:JvmName("get_CommentMarks")
    @set:JvmName("set_CommentMarks")
    var CommentMarks: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "CommentMarks_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "CommentMarks_set", value) }
    @get:JvmName("get_CompoundLineFeed")
    @set:JvmName("set_CompoundLineFeed")
    var CompoundLineFeed: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "CompoundLineFeed_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "CompoundLineFeed_set", value) }
    @get:JvmName("get_Console")
    @set:JvmName("set_Console")
    var Console: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "Console_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "Console_set", value) }
    @get:JvmName("get_ForceAscii")
    @set:JvmName("set_ForceAscii")
    var ForceAscii: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "ForceAscii_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "ForceAscii_set", value) }
    @get:JvmName("get_ForceQuote")
    @set:JvmName("set_ForceQuote")
    var ForceQuote: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "ForceQuote_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "ForceQuote_set", value) }
    @get:JvmName("get_Jsonify")
    @set:JvmName("set_Jsonify")
    var Jsonify: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "Jsonify_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "Jsonify_set", value) }
    @get:JvmName("get_Minimize")
    @set:JvmName("set_Minimize")
    var Minimize: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "Minimize_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "Minimize_set", value) }
    @get:JvmName("get_PartialLineFeed")
    @set:JvmName("set_PartialLineFeed")
    var PartialLineFeed: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "PartialLineFeed_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "PartialLineFeed_set", value) }
    @get:JvmName("get_PrettyChatPrint")
    @set:JvmName("set_PrettyChatPrint")
    var PrettyChatPrint: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "PrettyChatPrint_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "PrettyChatPrint_set", value) }
    @get:JvmName("get_PrettyConsolePrint")
    @set:JvmName("set_PrettyConsolePrint")
    var PrettyConsolePrint: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "PrettyConsolePrint_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "PrettyConsolePrint_set", value) }
    @get:JvmName("get_PrettyFilePrint")
    @set:JvmName("set_PrettyFilePrint")
    var PrettyFilePrint: Any?
        get() = LseKotlinApiRuntime.static("SnbtFormat", "PrettyFilePrint_get")
        set(value) { LseKotlinApiRuntime.static("SnbtFormat", "PrettyFilePrint_set", value) }
}

object i18n {
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("i18n", "get", *args)
    fun load(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("i18n", "load", *args)
    fun tr(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("i18n", "tr", *args)
    fun trl(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("i18n", "trl", *args)
}

object NBT {
    fun createTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("NBT", "createTag", *args)
    fun newTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("NBT", "newTag", *args)
    fun parseBinaryNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("NBT", "parseBinaryNBT", *args)
    fun parseSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("NBT", "parseSNBT", *args)
    @get:JvmName("get_Byte")
    @set:JvmName("set_Byte")
    var Byte: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Byte_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Byte_set", value) }
    @get:JvmName("get_ByteArray")
    @set:JvmName("set_ByteArray")
    var ByteArray: Any?
        get() = LseKotlinApiRuntime.static("NBT", "ByteArray_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "ByteArray_set", value) }
    @get:JvmName("get_Compound")
    @set:JvmName("set_Compound")
    var Compound: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Compound_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Compound_set", value) }
    @get:JvmName("get_Double")
    @set:JvmName("set_Double")
    var Double: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Double_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Double_set", value) }
    @get:JvmName("get_End")
    @set:JvmName("set_End")
    var End: Any?
        get() = LseKotlinApiRuntime.static("NBT", "End_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "End_set", value) }
    @get:JvmName("get_Float")
    @set:JvmName("set_Float")
    var Float: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Float_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Float_set", value) }
    @get:JvmName("get_Int")
    @set:JvmName("set_Int")
    var Int: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Int_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Int_set", value) }
    @get:JvmName("get_List")
    @set:JvmName("set_List")
    var List: Any?
        get() = LseKotlinApiRuntime.static("NBT", "List_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "List_set", value) }
    @get:JvmName("get_Long")
    @set:JvmName("set_Long")
    var Long: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Long_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Long_set", value) }
    @get:JvmName("get_Short")
    @set:JvmName("set_Short")
    var Short: Any?
        get() = LseKotlinApiRuntime.static("NBT", "Short_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "Short_set", value) }
    @get:JvmName("get_String")
    @set:JvmName("set_String")
    var String: Any?
        get() = LseKotlinApiRuntime.static("NBT", "String_get")
        set(value) { LseKotlinApiRuntime.static("NBT", "String_set", value) }
}

open class NbtByte internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): NbtByte =
            LseKotlinApiRuntime.construct("NbtByte", *args) as NbtByte
    }
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByte", "get", this, *args)
    fun getType(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByte", "getType", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByte", "set", this, *args)
    fun toSNBT(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByte", "toSNBT", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("NbtByte", "toString", this, *args)
}

open class JsonConfigFile internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): JsonConfigFile =
            LseKotlinApiRuntime.construct("JsonConfigFile", *args) as JsonConfigFile
    }
    fun delete(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "delete", this, *args)
    fun get(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "get", this, *args)
    fun getPath(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "getPath", this, *args)
    fun init(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "init", this, *args)
    fun read(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "read", this, *args)
    fun set(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "set", this, *args)
    fun write(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("JsonConfigFile", "write", this, *args)
}

open class ParticleSpawner internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): ParticleSpawner =
            LseKotlinApiRuntime.construct("ParticleSpawner", *args) as ParticleSpawner
    }
    fun drawAxialLine(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawAxialLine", this, *args)
    fun drawCircle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawCircle", this, *args)
    fun drawCuboid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawCuboid", this, *args)
    fun drawNumber(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawNumber", this, *args)
    fun drawOrientedLine(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawOrientedLine", this, *args)
    fun drawPoint(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "drawPoint", this, *args)
    fun spawnParticle(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("ParticleSpawner", "spawnParticle", this, *args)
    @get:JvmName("get_displayRadius")
    @set:JvmName("set_displayRadius")
    var displayRadius: Any?
        get() = LseKotlinApiRuntime.property("ParticleSpawner", "displayRadius", this)
        set(value) { LseKotlinApiRuntime.setProperty("ParticleSpawner", "displayRadius", this, value) }
    @get:JvmName("get_doubleSide")
    @set:JvmName("set_doubleSide")
    var doubleSide: Any?
        get() = LseKotlinApiRuntime.property("ParticleSpawner", "doubleSide", this)
        set(value) { LseKotlinApiRuntime.setProperty("ParticleSpawner", "doubleSide", this, value) }
    @get:JvmName("get_highDetial")
    @set:JvmName("set_highDetial")
    var highDetial: Any?
        get() = LseKotlinApiRuntime.property("ParticleSpawner", "highDetial", this)
        set(value) { LseKotlinApiRuntime.setProperty("ParticleSpawner", "highDetial", this, value) }
}

open class LLSE_Command internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addSoftEnumValues(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "addSoftEnumValues", this, *args)
    fun getSoftEnumNames(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "getSoftEnumNames", this, *args)
    fun getSoftEnumValues(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "getSoftEnumValues", this, *args)
    fun mandatory(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "mandatory", this, *args)
    fun optional(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "optional", this, *args)
    fun overload(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "overload", this, *args)
    fun removeSoftEnumValues(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "removeSoftEnumValues", this, *args)
    fun setAlias(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "setAlias", this, *args)
    fun setCallback(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "setCallback", this, *args)
    fun setEnum(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "setEnum", this, *args)
    fun setSoftEnum(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "setSoftEnum", this, *args)
    fun setup(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Command", "setup", this, *args)
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Command", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Command", "name", this, value) }
    @get:JvmName("get_registered")
    @set:JvmName("set_registered")
    var registered: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Command", "registered", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Command", "registered", this, value) }
}

open class WSClient internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): WSClient =
            LseKotlinApiRuntime.construct("WSClient", *args) as WSClient
    }
    fun close(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "close", this, *args)
    fun connect(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "connect", this, *args)
    fun connectAsync(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "connectAsync", this, *args)
    fun errorCode(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "errorCode", this, *args)
    fun listen(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "listen", this, *args)
    fun send(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "send", this, *args)
    fun shutdown(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("WSClient", "shutdown", this, *args)
    @get:JvmName("get_status")
    @set:JvmName("set_status")
    var status: Any?
        get() = LseKotlinApiRuntime.property("WSClient", "status", this)
        set(value) { LseKotlinApiRuntime.setProperty("WSClient", "status", this, value) }
}

open class IntPos internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): IntPos =
            LseKotlinApiRuntime.construct("IntPos", *args) as IntPos
    }
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("IntPos", "toString", this, *args)
    @get:JvmName("get_dim")
    @set:JvmName("set_dim")
    var dim: Any?
        get() = LseKotlinApiRuntime.property("IntPos", "dim", this)
        set(value) { LseKotlinApiRuntime.setProperty("IntPos", "dim", this, value) }
    @get:JvmName("get_dimid")
    @set:JvmName("set_dimid")
    var dimid: Any?
        get() = LseKotlinApiRuntime.property("IntPos", "dimid", this)
        set(value) { LseKotlinApiRuntime.setProperty("IntPos", "dimid", this, value) }
    @get:JvmName("get_x")
    @set:JvmName("set_x")
    var x: Any?
        get() = LseKotlinApiRuntime.property("IntPos", "x", this)
        set(value) { LseKotlinApiRuntime.setProperty("IntPos", "x", this, value) }
    @get:JvmName("get_y")
    @set:JvmName("set_y")
    var y: Any?
        get() = LseKotlinApiRuntime.property("IntPos", "y", this)
        set(value) { LseKotlinApiRuntime.setProperty("IntPos", "y", this, value) }
    @get:JvmName("get_z")
    @set:JvmName("set_z")
    var z: Any?
        get() = LseKotlinApiRuntime.property("IntPos", "z", this)
        set(value) { LseKotlinApiRuntime.setProperty("IntPos", "z", this, value) }
}

object system {
    fun cmd(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "cmd", *args)
    fun getTimeObj(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "getTimeObj", *args)
    fun getTimeStr(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "getTimeStr", *args)
    fun newProcess(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "newProcess", *args)
    fun randomGuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "randomGuid", *args)
    fun randomUuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("system", "randomUuid", *args)
}

object data {
    fun fromBase64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "fromBase64", *args)
    fun fromName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "fromName", *args)
    fun fromUuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "fromUuid", *args)
    fun fromXuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "fromXuid", *args)
    fun getAllPlayerInfo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "getAllPlayerInfo", *args)
    fun name2uuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "name2uuid", *args)
    fun name2xuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "name2xuid", *args)
    fun openConfig(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "openConfig", *args)
    fun openDB(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "openDB", *args)
    fun parseJson(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "parseJson", *args)
    fun toBase64(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "toBase64", *args)
    fun toJson(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "toJson", *args)
    fun toMD5(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "toMD5", *args)
    fun toSHA1(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "toSHA1", *args)
    fun xuid2name(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "xuid2name", *args)
    fun xuid2uuid(vararg args: Any?): Any? =
        LseKotlinApiRuntime.static("data", "xuid2uuid", *args)
}

open class LLSE_CommandOrigin internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOrigin", "getNbt", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_CommandOrigin", "toString", this, *args)
    @get:JvmName("get_blockPos")
    @set:JvmName("set_blockPos")
    var blockPos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "blockPos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "blockPos", this, value) }
    @get:JvmName("get_entity")
    @set:JvmName("set_entity")
    var entity: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "entity", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "entity", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "name", this, value) }
    @get:JvmName("get_player")
    @set:JvmName("set_player")
    var player: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "player", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "player", this, value) }
    @get:JvmName("get_pos")
    @set:JvmName("set_pos")
    var pos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "pos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "pos", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "type", this, value) }
    @get:JvmName("get_typeName")
    @set:JvmName("set_typeName")
    var typeName: Any?
        get() = LseKotlinApiRuntime.property("LLSE_CommandOrigin", "typeName", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_CommandOrigin", "typeName", this, value) }
}

object Format {
    @get:JvmName("get_Aqua")
    @set:JvmName("set_Aqua")
    var Aqua: Any?
        get() = LseKotlinApiRuntime.static("Format", "Aqua_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Aqua_set", value) }
    @get:JvmName("get_Black")
    @set:JvmName("set_Black")
    var Black: Any?
        get() = LseKotlinApiRuntime.static("Format", "Black_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Black_set", value) }
    @get:JvmName("get_Blue")
    @set:JvmName("set_Blue")
    var Blue: Any?
        get() = LseKotlinApiRuntime.static("Format", "Blue_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Blue_set", value) }
    @get:JvmName("get_Bold")
    @set:JvmName("set_Bold")
    var Bold: Any?
        get() = LseKotlinApiRuntime.static("Format", "Bold_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Bold_set", value) }
    @get:JvmName("get_Clear")
    @set:JvmName("set_Clear")
    var Clear: Any?
        get() = LseKotlinApiRuntime.static("Format", "Clear_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Clear_set", value) }
    @get:JvmName("get_DarkAqua")
    @set:JvmName("set_DarkAqua")
    var DarkAqua: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkAqua_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkAqua_set", value) }
    @get:JvmName("get_DarkBlue")
    @set:JvmName("set_DarkBlue")
    var DarkBlue: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkBlue_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkBlue_set", value) }
    @get:JvmName("get_DarkGray")
    @set:JvmName("set_DarkGray")
    var DarkGray: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkGray_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkGray_set", value) }
    @get:JvmName("get_DarkGreen")
    @set:JvmName("set_DarkGreen")
    var DarkGreen: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkGreen_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkGreen_set", value) }
    @get:JvmName("get_DarkPurple")
    @set:JvmName("set_DarkPurple")
    var DarkPurple: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkPurple_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkPurple_set", value) }
    @get:JvmName("get_DarkRed")
    @set:JvmName("set_DarkRed")
    var DarkRed: Any?
        get() = LseKotlinApiRuntime.static("Format", "DarkRed_get")
        set(value) { LseKotlinApiRuntime.static("Format", "DarkRed_set", value) }
    @get:JvmName("get_Gold")
    @set:JvmName("set_Gold")
    var Gold: Any?
        get() = LseKotlinApiRuntime.static("Format", "Gold_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Gold_set", value) }
    @get:JvmName("get_Gray")
    @set:JvmName("set_Gray")
    var Gray: Any?
        get() = LseKotlinApiRuntime.static("Format", "Gray_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Gray_set", value) }
    @get:JvmName("get_Green")
    @set:JvmName("set_Green")
    var Green: Any?
        get() = LseKotlinApiRuntime.static("Format", "Green_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Green_set", value) }
    @get:JvmName("get_Italics")
    @set:JvmName("set_Italics")
    var Italics: Any?
        get() = LseKotlinApiRuntime.static("Format", "Italics_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Italics_set", value) }
    @get:JvmName("get_LightPurple")
    @set:JvmName("set_LightPurple")
    var LightPurple: Any?
        get() = LseKotlinApiRuntime.static("Format", "LightPurple_get")
        set(value) { LseKotlinApiRuntime.static("Format", "LightPurple_set", value) }
    @get:JvmName("get_MinecoinGold")
    @set:JvmName("set_MinecoinGold")
    var MinecoinGold: Any?
        get() = LseKotlinApiRuntime.static("Format", "MinecoinGold_get")
        set(value) { LseKotlinApiRuntime.static("Format", "MinecoinGold_set", value) }
    @get:JvmName("get_Random")
    @set:JvmName("set_Random")
    var Random: Any?
        get() = LseKotlinApiRuntime.static("Format", "Random_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Random_set", value) }
    @get:JvmName("get_Red")
    @set:JvmName("set_Red")
    var Red: Any?
        get() = LseKotlinApiRuntime.static("Format", "Red_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Red_set", value) }
    @get:JvmName("get_StrikeThrough")
    @set:JvmName("set_StrikeThrough")
    var StrikeThrough: Any?
        get() = LseKotlinApiRuntime.static("Format", "StrikeThrough_get")
        set(value) { LseKotlinApiRuntime.static("Format", "StrikeThrough_set", value) }
    @get:JvmName("get_Underline")
    @set:JvmName("set_Underline")
    var Underline: Any?
        get() = LseKotlinApiRuntime.static("Format", "Underline_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Underline_set", value) }
    @get:JvmName("get_White")
    @set:JvmName("set_White")
    var White: Any?
        get() = LseKotlinApiRuntime.static("Format", "White_get")
        set(value) { LseKotlinApiRuntime.static("Format", "White_set", value) }
    @get:JvmName("get_Yellow")
    @set:JvmName("set_Yellow")
    var Yellow: Any?
        get() = LseKotlinApiRuntime.static("Format", "Yellow_get")
        set(value) { LseKotlinApiRuntime.static("Format", "Yellow_set", value) }
}

open class LLSE_Packet internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun getId(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "getId", this, *args)
    fun getName(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "getName", this, *args)
    fun read(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "read", this, *args)
    fun sendTo(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "sendTo", this, *args)
    fun sendToClients(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "sendToClients", this, *args)
    fun sendToServer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "sendToServer", this, *args)
    fun write(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Packet", "write", this, *args)
}

open class HttpResponse internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun getHeader(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpResponse", "getHeader", this, *args)
    fun setHeader(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpResponse", "setHeader", this, *args)
    fun write(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpResponse", "write", this, *args)
    @get:JvmName("get_body")
    @set:JvmName("set_body")
    var body: Any?
        get() = LseKotlinApiRuntime.property("HttpResponse", "body", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpResponse", "body", this, value) }
    @get:JvmName("get_headers")
    @set:JvmName("set_headers")
    var headers: Any?
        get() = LseKotlinApiRuntime.property("HttpResponse", "headers", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpResponse", "headers", this, value) }
    @get:JvmName("get_reason")
    @set:JvmName("set_reason")
    var reason: Any?
        get() = LseKotlinApiRuntime.property("HttpResponse", "reason", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpResponse", "reason", this, value) }
    @get:JvmName("get_status")
    @set:JvmName("set_status")
    var status: Any?
        get() = LseKotlinApiRuntime.property("HttpResponse", "status", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpResponse", "status", this, value) }
    @get:JvmName("get_version")
    @set:JvmName("set_version")
    var version: Any?
        get() = LseKotlinApiRuntime.property("HttpResponse", "version", this)
        set(value) { LseKotlinApiRuntime.setProperty("HttpResponse", "version", this, value) }
}

open class HttpServer internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): HttpServer =
            LseKotlinApiRuntime.construct("HttpServer", *args) as HttpServer
    }
    fun close(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "close", this, *args)
    fun isRunning(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "isRunning", this, *args)
    fun listen(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "listen", this, *args)
    fun onDelete(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onDelete", this, *args)
    fun onError(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onError", this, *args)
    fun onException(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onException", this, *args)
    fun onGet(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onGet", this, *args)
    fun onOptions(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onOptions", this, *args)
    fun onPatch(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onPatch", this, *args)
    fun onPost(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onPost", this, *args)
    fun onPostRouting(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onPostRouting", this, *args)
    fun onPreRouting(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onPreRouting", this, *args)
    fun onPut(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "onPut", this, *args)
    fun startAt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "startAt", this, *args)
    fun stop(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("HttpServer", "stop", this, *args)
}

object Version {
    @get:JvmName("get_Beta")
    @set:JvmName("set_Beta")
    var Beta: Any?
        get() = LseKotlinApiRuntime.static("Version", "Beta_get")
        set(value) { LseKotlinApiRuntime.static("Version", "Beta_set", value) }
    @get:JvmName("get_Dev")
    @set:JvmName("set_Dev")
    var Dev: Any?
        get() = LseKotlinApiRuntime.static("Version", "Dev_get")
        set(value) { LseKotlinApiRuntime.static("Version", "Dev_set", value) }
    @get:JvmName("get_Release")
    @set:JvmName("set_Release")
    var Release: Any?
        get() = LseKotlinApiRuntime.static("Version", "Release_get")
        set(value) { LseKotlinApiRuntime.static("Version", "Release_set", value) }
}

open class DirectionAngle internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    companion object {
        operator fun invoke(vararg args: Any?): DirectionAngle =
            LseKotlinApiRuntime.construct("DirectionAngle", *args) as DirectionAngle
    }
    fun toFacing(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DirectionAngle", "toFacing", this, *args)
    fun toString(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DirectionAngle", "toString", this, *args)
    @get:JvmName("get_pitch")
    @set:JvmName("set_pitch")
    var pitch: Any?
        get() = LseKotlinApiRuntime.property("DirectionAngle", "pitch", this)
        set(value) { LseKotlinApiRuntime.setProperty("DirectionAngle", "pitch", this, value) }
    @get:JvmName("get_yaw")
    @set:JvmName("set_yaw")
    var yaw: Any?
        get() = LseKotlinApiRuntime.property("DirectionAngle", "yaw", this)
        set(value) { LseKotlinApiRuntime.setProperty("DirectionAngle", "yaw", this, value) }
}

open class LLSE_Block internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun destroy(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "destroy", this, *args)
    fun getBlockEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "getBlockEntity", this, *args)
    fun getBlockState(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "getBlockState", this, *args)
    fun getContainer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "getContainer", this, *args)
    fun getNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "getNbt", this, *args)
    fun getTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "getTag", this, *args)
    fun hasBlockEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "hasBlockEntity", this, *args)
    fun hasContainer(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "hasContainer", this, *args)
    fun removeBlockEntity(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "removeBlockEntity", this, *args)
    fun setNbt(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "setNbt", this, *args)
    fun setTag(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Block", "setTag", this, *args)
    @get:JvmName("get_id")
    @set:JvmName("set_id")
    var id: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "id", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "id", this, value) }
    @get:JvmName("get_isAir")
    @set:JvmName("set_isAir")
    var isAir: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isAir", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isAir", this, value) }
    @get:JvmName("get_isBounceBlock")
    @set:JvmName("set_isBounceBlock")
    var isBounceBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isBounceBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isBounceBlock", this, value) }
    @get:JvmName("get_isButtonBlock")
    @set:JvmName("set_isButtonBlock")
    var isButtonBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isButtonBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isButtonBlock", this, value) }
    @get:JvmName("get_isCropBlock")
    @set:JvmName("set_isCropBlock")
    var isCropBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isCropBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isCropBlock", this, value) }
    @get:JvmName("get_isDoorBlock")
    @set:JvmName("set_isDoorBlock")
    var isDoorBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isDoorBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isDoorBlock", this, value) }
    @get:JvmName("get_isFenceBlock")
    @set:JvmName("set_isFenceBlock")
    var isFenceBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isFenceBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isFenceBlock", this, value) }
    @get:JvmName("get_isFenceGateBlock")
    @set:JvmName("set_isFenceGateBlock")
    var isFenceGateBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isFenceGateBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isFenceGateBlock", this, value) }
    @get:JvmName("get_isHeavyBlock")
    @set:JvmName("set_isHeavyBlock")
    var isHeavyBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isHeavyBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isHeavyBlock", this, value) }
    @get:JvmName("get_isSlabBlock")
    @set:JvmName("set_isSlabBlock")
    var isSlabBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isSlabBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isSlabBlock", this, value) }
    @get:JvmName("get_isStemBlock")
    @set:JvmName("set_isStemBlock")
    var isStemBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isStemBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isStemBlock", this, value) }
    @get:JvmName("get_isThinFenceBlock")
    @set:JvmName("set_isThinFenceBlock")
    var isThinFenceBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isThinFenceBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isThinFenceBlock", this, value) }
    @get:JvmName("get_isUnbreakable")
    @set:JvmName("set_isUnbreakable")
    var isUnbreakable: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isUnbreakable", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isUnbreakable", this, value) }
    @get:JvmName("get_isWaterBlockingBlock")
    @set:JvmName("set_isWaterBlockingBlock")
    var isWaterBlockingBlock: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "isWaterBlockingBlock", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "isWaterBlockingBlock", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "name", this, value) }
    @get:JvmName("get_pos")
    @set:JvmName("set_pos")
    var pos: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "pos", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "pos", this, value) }
    @get:JvmName("get_thickness")
    @set:JvmName("set_thickness")
    var thickness: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "thickness", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "thickness", this, value) }
    @get:JvmName("get_tileData")
    @set:JvmName("set_tileData")
    var tileData: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "tileData", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "tileData", this, value) }
    @get:JvmName("get_translucency")
    @set:JvmName("set_translucency")
    var translucency: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "translucency", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "translucency", this, value) }
    @get:JvmName("get_type")
    @set:JvmName("set_type")
    var type: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "type", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "type", this, value) }
    @get:JvmName("get_variant")
    @set:JvmName("set_variant")
    var variant: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Block", "variant", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Block", "variant", this, value) }
}

open class DBStmt internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun bind(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "bind", this, *args)
    fun clear(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "clear", this, *args)
    fun execute(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "execute", this, *args)
    fun fetch(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "fetch", this, *args)
    fun fetchAll(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "fetchAll", this, *args)
    fun reexec(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "reexec", this, *args)
    fun reset(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "reset", this, *args)
    fun step(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("DBStmt", "step", this, *args)
    @get:JvmName("get_affectedRows")
    @set:JvmName("set_affectedRows")
    var affectedRows: Any?
        get() = LseKotlinApiRuntime.property("DBStmt", "affectedRows", this)
        set(value) { LseKotlinApiRuntime.setProperty("DBStmt", "affectedRows", this, value) }
    @get:JvmName("get_insertId")
    @set:JvmName("set_insertId")
    var insertId: Any?
        get() = LseKotlinApiRuntime.property("DBStmt", "insertId", this)
        set(value) { LseKotlinApiRuntime.setProperty("DBStmt", "insertId", this, value) }
}

open class LLSE_Objective internal constructor(override val rawNativeValue: Any?) : LseNativeBacked {
    fun addScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "addScore", this, *args)
    fun deleteScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "deleteScore", this, *args)
    fun getScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "getScore", this, *args)
    fun reduceScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "reduceScore", this, *args)
    fun setDisplay(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "setDisplay", this, *args)
    fun setScore(vararg args: Any?): Any? =
        LseKotlinApiRuntime.instance("LLSE_Objective", "setScore", this, *args)
    @get:JvmName("get_displayName")
    @set:JvmName("set_displayName")
    var displayName: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Objective", "displayName", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Objective", "displayName", this, value) }
    @get:JvmName("get_name")
    @set:JvmName("set_name")
    var name: Any?
        get() = LseKotlinApiRuntime.property("LLSE_Objective", "name", this)
        set(value) { LseKotlinApiRuntime.setProperty("LLSE_Objective", "name", this, value) }
}

