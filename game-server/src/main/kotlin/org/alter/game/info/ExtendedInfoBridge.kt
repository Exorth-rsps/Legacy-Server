package org.alter.game.info

import io.github.oshai.kotlinlogging.KotlinLogging
import org.alter.game.model.Tile
import org.alter.game.model.entity.Npc
import org.alter.game.model.entity.Player
import kotlin.math.atan2
import kotlin.math.floor
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}

internal abstract class ExtendedInfoBridge(private val extendedInfo: Any) {
    private val methodMap: Map<String, List<java.lang.reflect.Method>> =
        extendedInfo.javaClass.methods.groupBy { it.name }

    protected fun methods(name: String): List<java.lang.reflect.Method> = methodMap[name].orEmpty()

    protected fun invokeWithSubset(
        name: String,
        values: Array<Any?>,
        logFailure: Boolean = true,
    ): Boolean =
        invokeMatching(name, logFailure = logFailure) { method ->
            val subset = values.copyOf(method.parameterCount)
            convertArgs(method.parameterTypes, subset)
        }

    protected fun invokeAny(
        names: Array<String>,
        vararg values: Any?,
        logFailure: Boolean = true,
    ): Boolean {
        val base = values.copyOf(values.size)
        for ((index, candidate) in names.withIndex()) {
            val shouldLog = logFailure && index == names.lastIndex
            if (invokeWithSubset(candidate, base.copyOf(base.size), logFailure = shouldLog)) {
                return true
            }
        }
        return false
    }

    protected fun invokeMatching(
        name: String,
        valueProvider: (java.lang.reflect.Method) -> Array<Any?>?,
        logFailure: Boolean = true,
    ): Boolean {
        val candidates = methods(name)
        if (candidates.isEmpty()) {
            if (logFailure) {
                logger.warn { "Extended info ${extendedInfo.javaClass.name} does not expose method $name" }
            }
            return false
        }
        for (method in candidates) {
            try {
                val args = valueProvider(method) ?: continue
                method.isAccessible = true
                method.invoke(extendedInfo, *args)
                return true
            } catch (ex: ReflectiveOperationException) {
                logger.debug(ex) { "Failed invoking ${method.name} on ${extendedInfo.javaClass.name}" }
            }
        }
        if (logFailure) {
            logger.warn { "Unable to invoke $name on ${extendedInfo.javaClass.name}" }
        }
        return false
    }

    protected fun convertArgs(
        parameterTypes: Array<Class<*>>,
        values: Array<Any?>,
    ): Array<Any?>? {
        if (parameterTypes.size != values.size) {
            return null
        }
        val converted = arrayOfNulls<Any?>(values.size)
        for (index in parameterTypes.indices) {
            val convertedValue = convertSingle(parameterTypes[index], values[index]) ?: return null
            converted[index] = convertedValue
        }
        @Suppress("UNCHECKED_CAST")
        return converted as Array<Any?>
    }

    protected fun convertSingle(
        parameterType: Class<*>,
        value: Any?,
    ): Any? {
        if (parameterType.isPrimitive) {
            return convertSingle(primitiveToWrapper(parameterType), value)
        }
        if (value == null) {
            return if (parameterType.isPrimitive) null else null
        }
        if (parameterType.isInstance(value)) {
            return value
        }
        return when {
            Number::class.java.isAssignableFrom(parameterType) && value is Number -> when (parameterType) {
                java.lang.Integer::class.java -> value.toInt()
                java.lang.Long::class.java -> value.toLong()
                java.lang.Short::class.java -> value.toShort()
                java.lang.Byte::class.java -> value.toByte()
                java.lang.Double::class.java -> value.toDouble()
                java.lang.Float::class.java -> value.toFloat()
                else -> null
            }

            parameterType == java.lang.Boolean::class.java && value is Boolean -> value

            parameterType == java.lang.Boolean::class.java && value is Number -> value.toInt() != 0

            parameterType == java.lang.String::class.java -> value.toString()

            CharSequence::class.java.isAssignableFrom(parameterType) -> value.toString()

            parameterType.isArray && parameterType.componentType == java.lang.Byte.TYPE && value is ByteArray -> value

            parameterType.name == "java.nio.ByteBuffer" && value is ByteArray -> java.nio.ByteBuffer.wrap(value)

            parameterType.isEnum -> {
                val stringValue = when (value) {
                    is Enum<*> -> value.name
                    else -> value.toString()
                }
                @Suppress("UNCHECKED_CAST")
                parameterType.enumConstants?.firstOrNull { (it as Enum<*>).name == stringValue }
            }

            else -> null
        }
    }

    protected fun primitiveToWrapper(type: Class<*>): Class<*> = when (type) {
        java.lang.Integer.TYPE -> java.lang.Integer::class.java
        java.lang.Long.TYPE -> java.lang.Long::class.java
        java.lang.Short.TYPE -> java.lang.Short::class.java
        java.lang.Byte.TYPE -> java.lang.Byte::class.java
        java.lang.Double.TYPE -> java.lang.Double::class.java
        java.lang.Float.TYPE -> java.lang.Float::class.java
        java.lang.Boolean.TYPE -> java.lang.Boolean::class.java
        java.lang.Character.TYPE -> java.lang.Character::class.java
        else -> type
    }
}

internal class NpcExtendedInfoBridge(private val npc: Npc, extendedInfo: Any) : ExtendedInfoBridge(extendedInfo) {
    fun setSpotAnim(slot: Int, id: Int, delay: Int, height: Int) {
        invokeAny(arrayOf("setSpotAnim", "setSpotAnimation"), slot, id, delay, height)
    }

    fun setInaccessible(state: Boolean) {
        invokeAny(arrayOf("setInaccessible", "setBlocking", "setInteractable"), state)
    }

    fun addHeadBar(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int,
        startFill: Int,
        endFill: Int,
        startTime: Int,
        endTime: Int,
    ) {
        val values = arrayOf<Any?>(sourceIndex, selfType, otherType, startFill, endFill, startTime, endTime)
        invokeMatching("addHeadBar") { method ->
            val params = method.parameterTypes
            val subset = values.copyOf(params.size)
            convertArgs(params, subset)
        }
    }

    fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int,
        value: Int,
        delay: Int,
    ) {
        val values = arrayOf<Any?>(sourceIndex, selfType, otherType, value, delay)
        invokeMatching("addHitMark") { method ->
            val params = method.parameterTypes
            val subset = values.copyOf(params.size)
            convertArgs(params, subset)
        }
    }

    fun setSequence(id: Int, delay: Int) {
        invokeAny(arrayOf("setSequence", "setAnimation", "playSequence"), id, delay)
    }

    fun setTinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
    ) {
        invokeAny(
            arrayOf("setTinting", "tinting", "setColourOverlay"),
            startTime,
            endTime,
            hue,
            saturation,
            lightness,
            weight,
        )
    }

    fun setCombatLevelChange(level: Int) {
        invokeAny(arrayOf("setCombatLevelChange", "setCombatLevel"), level)
    }

    fun setNameChange(name: String) {
        invokeAny(arrayOf("setNameChange", "setName", "setDisplayName"), name)
    }

    fun setSay(message: String) {
        invokeAny(arrayOf("setSay", "setForceChat", "setChatMessage"), message)
    }

    fun setFaceCoord(
        x: Int,
        z: Int,
        width: Int,
        length: Int,
        instant: Boolean,
    ) {
        if (invokeMatching("setFaceCoord", logFailure = false) { method ->
                buildFaceCoordArguments(method, x, z, width, length, instant)
            }
        ) {
            return
        }
        if (invokeMatching("setFaceAngle") { method ->
                buildFaceAngleArguments(method, x, z, width, length, instant)
            }
        ) {
            return
        }
        logger.warn {
            "Falling back to face pathing entity reset for npc ${npc.id} due to missing face methods"
        }
        setFacePathingEntity(-1)
    }

    fun setFacePathingEntity(index: Int) {
        invokeAny(arrayOf("setFacePathingEntity", "setFaceEntity", "setFaceIndex"), index)
    }

    fun setAllOpsInvisible() {
        invokeAny(arrayOf("setAllOpsInvisible", "hideAllOps"))
    }

    fun setAllOpsVisible() {
        invokeAny(arrayOf("setAllOpsVisible", "showAllOps"))
    }

    private fun buildFaceCoordArguments(
        method: java.lang.reflect.Method,
        x: Int,
        z: Int,
        width: Int,
        length: Int,
        instant: Boolean,
    ): Array<Any?>? {
        val params = method.parameterTypes
        return when (params.size) {
            0 -> emptyArray<Any?>()
            1 -> createFaceCoordinateObject(params[0], x, z, width, length, instant)?.let { arrayOf(it) }
            else -> {
                val values = mutableListOf<Any?>()
                var numericIndex = 0
                for (type in params) {
                    when {
                        isBoolean(type) -> values += instant
                        Number::class.java.isAssignableFrom(toWrapper(type)) -> {
                            val value = when (numericIndex++) {
                                0 -> x
                                1 -> z
                                2 -> width
                                3 -> length
                                else -> return null
                            }
                            values += value
                        }
                        else -> return null
                    }
                }
                convertArgs(params, values.toTypedArray())
            }
        }
    }

    private fun buildFaceAngleArguments(
        method: java.lang.reflect.Method,
        x: Int,
        z: Int,
        width: Int,
        length: Int,
        instant: Boolean,
    ): Array<Any?>? {
        val params = method.parameterTypes
        val angle = computeAngle(npc.tile, npc.getSize(), x, z, width, length)
        val values = mutableListOf<Any?>()
        var numericIndex = 0
        for (type in params) {
            when {
                isBoolean(type) -> values += instant
                Number::class.java.isAssignableFrom(toWrapper(type)) -> {
                    val value = when (numericIndex++) {
                        0 -> angle
                        1 -> width
                        2 -> length
                        else -> return null
                    }
                    values += value
                }
                else -> return null
            }
        }
        return convertArgs(params, values.toTypedArray())
    }

    private fun createFaceCoordinateObject(
        type: Class<*>,
        x: Int,
        z: Int,
        width: Int,
        length: Int,
        instant: Boolean,
    ): Any? {
        val ctor = type.declaredConstructors.firstOrNull { it.parameterCount == 0 } ?: return null
        return try {
            ctor.isAccessible = true
            val instance = ctor.newInstance()
            assignProperty(instance, type, arrayOf("setX", "setTargetX", "setCoordX", "setTileX"), x)
            assignProperty(instance, type, arrayOf("setZ", "setTargetZ", "setCoordZ", "setTileZ"), z)
            assignProperty(instance, type, arrayOf("setWidth", "setTargetWidth", "setSizeX"), width)
            assignProperty(instance, type, arrayOf("setLength", "setTargetLength", "setSizeZ"), length)
            assignProperty(instance, type, arrayOf("setInstant", "setImmediate"), instant)
            instance
        } catch (ex: ReflectiveOperationException) {
            logger.debug(ex) { "Failed creating face coordinate object of type ${type.name}" }
            null
        }
    }

    private fun assignProperty(
        instance: Any,
        type: Class<*>,
        prefixes: Array<String>,
        value: Any,
    ) {
        val method = type.methods.firstOrNull { method ->
            method.parameterCount == 1 && prefixes.any { prefix -> method.name.startsWith(prefix) }
        } ?: return
        val converted = convertSingle(method.parameterTypes[0], value) ?: return
        try {
            method.isAccessible = true
            method.invoke(instance, converted)
        } catch (ex: ReflectiveOperationException) {
            logger.debug(ex) { "Failed assigning property ${method.name} on ${type.name}" }
        }
    }

    private fun computeAngle(
        source: Tile,
        sourceSize: Int,
        targetX: Int,
        targetZ: Int,
        targetWidth: Int,
        targetLength: Int,
    ): Int {
        val srcX = source.x shl 6
        val srcZ = source.z shl 6
        val dstX = targetX shl 6
        val dstZ = targetZ shl 6
        var deltaX = (srcX - dstX).toDouble()
        var deltaZ = (srcZ - dstZ).toDouble()
        deltaX += floor(targetWidth / 2.0) * 32
        deltaZ += floor(targetLength / 2.0) * 32
        deltaX -= floor(sourceSize / 2.0) * 32
        deltaZ -= floor(sourceSize / 2.0) * 32
        val angle = atan2(deltaX, deltaZ) * 325.949
        return angle.roundToInt() and 0x7ff
    }

    private fun toWrapper(type: Class<*>): Class<*> = if (type.isPrimitive) primitiveToWrapper(type) else type

    private fun isBoolean(type: Class<*>): Boolean {
        val wrapper = toWrapper(type)
        return wrapper == java.lang.Boolean::class.java
    }
}

internal class PlayerExtendedInfoBridge(private val player: Player, extendedInfo: Any) : ExtendedInfoBridge(extendedInfo) {
    fun setIdentKit(slot: Int, look: Int) {
        invokeAny(arrayOf("setIdentKit", "setKit"), slot, look)
    }

    fun setWornObj(slot: Int, id: Int, override1: Int, override2: Int) {
        invokeAny(
            arrayOf("setWornObj", "setWornItem", "setEquipment"),
            slot,
            id,
            override1,
            override2,
        )
    }

    fun setBaseAnimationSet(
        readyAnim: Int,
        turnAnim: Int,
        walkAnim: Int,
        walkAnimBack: Int,
        walkAnimLeft: Int,
        walkAnimRight: Int,
        runAnim: Int,
    ) {
        invokeAny(
            arrayOf("setBaseAnimationSet", "setAnimationSet", "setBaseAnimations"),
            readyAnim,
            turnAnim,
            walkAnim,
            walkAnimBack,
            walkAnimLeft,
            walkAnimRight,
            runAnim,
        )
    }

    fun setColour(slot: Int, colour: Int) {
        invokeAny(arrayOf("setColour", "setColor"), slot, colour)
    }

    fun setName(name: String) {
        invokeAny(arrayOf("setName", "setDisplayName", "setUsername"), name)
    }

    fun setCombatLevel(level: Int) {
        invokeAny(arrayOf("setCombatLevel", "setCombat"), level)
    }

    fun setMale(isMale: Boolean) {
        invokeAny(arrayOf("setMale", "setIsMale"), isMale)
    }

    fun setOverheadIcon(icon: Int) {
        invokeAny(arrayOf("setOverheadIcon", "setPrayerIcon", "setOverheadPrayer"), icon)
    }

    fun setSkullIcon(icon: Int) {
        invokeAny(arrayOf("setSkullIcon", "setHeadIconPk", "setPkIcon"), icon)
    }

    fun transformToNpc(id: Int) {
        invokeAny(arrayOf("transformToNpc", "setNpcTransform", "transformNpc"), id)
    }

    fun setHidden(hidden: Boolean) {
        invokeAny(arrayOf("setHidden", "setInvisible", "setHiddenFlag"), hidden)
    }

    fun setSequence(id: Int, delay: Int) {
        invokeAny(arrayOf("setSequence", "playSequence", "setAnimation"), id, delay)
    }

    fun tinting(
        startTime: Int,
        endTime: Int,
        hue: Int,
        saturation: Int,
        lightness: Int,
        weight: Int,
    ) {
        invokeAny(
            arrayOf("tinting", "setTinting", "setColourOverlay"),
            startTime,
            endTime,
            hue,
            saturation,
            lightness,
            weight,
        )
    }

    fun setSay(message: String) {
        invokeAny(arrayOf("setSay", "setForceChat", "setChatMessage"), message)
    }

    fun setFaceAngle(angle: Int) {
        invokeAny(arrayOf("setFaceAngle", "setFaceYaw", "setOrientation"), angle)
    }

    fun setFacePathingEntity(index: Int) {
        invokeAny(arrayOf("setFacePathingEntity", "setFaceEntity", "setFaceIndex"), index)
    }

    fun setSpotAnim(slot: Int, id: Int, delay: Int, height: Int) {
        invokeAny(arrayOf("setSpotAnim", "setSpotAnimation", "addSpotAnimation"), slot, id, delay, height)
    }

    fun setExactMove(
        deltaX1: Int,
        deltaZ1: Int,
        delay1: Int,
        deltaX2: Int,
        deltaZ2: Int,
        delay2: Int,
        angle: Int,
    ) {
        invokeAny(
            arrayOf("setExactMove", "setForceMove", "setForcedMovement"),
            deltaX1,
            deltaZ1,
            delay1,
            deltaX2,
            deltaZ2,
            delay2,
            angle,
        )
    }

    fun setMoveSpeed(speed: Int) {
        invokeAny(arrayOf("setMoveSpeed", "setMovementSpeed", "setRunSpeed"), speed)
    }

    fun addHitMark(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int,
        value: Int,
        delay: Int,
    ) {
        val values = arrayOf<Any?>(sourceIndex, selfType, otherType, value, delay)
        invokeMatching("addHitMark") { method ->
            val params = method.parameterTypes
            val subset = values.copyOf(params.size)
            convertArgs(params, subset)
        }
    }

    fun addHeadBar(
        sourceIndex: Int,
        selfType: Int,
        otherType: Int,
        startFill: Int,
        endFill: Int,
        startTime: Int,
        endTime: Int,
    ) {
        val values = arrayOf<Any?>(sourceIndex, selfType, otherType, startFill, endFill, startTime, endTime)
        invokeMatching("addHeadBar") { method ->
            val params = method.parameterTypes
            val subset = values.copyOf(params.size)
            convertArgs(params, subset)
        }
    }

    fun setChat(
        colour: Int,
        effect: Int,
        icon: Int,
        auto: Boolean,
        message: String,
        pattern: ByteArray?,
    ) {
        val names = arrayOf("setChat", "setChatMessage", "setPublicChat")
        for ((index, name) in names.withIndex()) {
            val shouldLog = index == names.lastIndex
            if (
                invokeMatching(name, logFailure = shouldLog) { method ->
                    val params = method.parameterTypes
                    val requested = when (params.size) {
                        4 -> arrayOf<Any?>(colour, effect, icon, auto)
                        5 -> arrayOf<Any?>(colour, effect, icon, auto, message)
                        else -> arrayOf<Any?>(colour, effect, icon, auto, message, pattern)
                    }
                    convertArgs(params, requested)
                }
            ) {
                return
            }
        }
    }

    fun setTempMoveSpeed(speed: Int) {
        invokeAny(arrayOf("setTempMoveSpeed", "setTemporaryMoveSpeed", "setTempMovementSpeed"), speed)
    }
}
