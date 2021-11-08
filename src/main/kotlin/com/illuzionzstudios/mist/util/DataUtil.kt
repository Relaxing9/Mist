package com.illuzionzstudios.mist.util

import lombok.experimental.UtilityClass
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockState
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * A utility class for playing around with NBT data on objects
 */
@UtilityClass
class DataUtil {
    val DOUBLE_ARRAY: PersistentDataType<ByteArray, DoubleArray> = DoubleArray()
    val STRING_ARRAY: PersistentDataType<ByteArray, Array<String>> = StringArray(StandardCharsets.UTF_8)
    val UUID: PersistentDataType<ByteArray, UUID> = UUIDDataType()
    fun <Z> getData(
        holder: PersistentDataHolder, type: PersistentDataType<*, Z>,
        key: NamespacedKey
    ): Z? {
        val container = holder.persistentDataContainer
        return if (container.has(key, type)) {
            container[key, type]
        } else null
    }

    fun setData(holder: PersistentDataHolder, key: NamespacedKey, value: Any) {
        val container = holder.persistentDataContainer
        if (value is Boolean) {
            setData(holder, key, if (value.toBoolean()) 1 else 0)
            return
        }
        if (value is Double) {
            container.set(key, PersistentDataType.DOUBLE, value)
        } else if (value is Int) {
            container.set(key, PersistentDataType.INTEGER, value)
        } else if (value is Array<String>) {
            container.set(key, STRING_ARRAY, value)
        } else if (value is DoubleArray) {
            container.set(key, DOUBLE_ARRAY, value)
        } else {
            val i = value.toString()
            container.set(key, PersistentDataType.STRING, i)
        }
        if (holder is BlockState) {
            val state = holder as BlockState
            state.update()
        }
    }

    fun removeData(holder: PersistentDataHolder, key: NamespacedKey) {
        val container = holder.persistentDataContainer
        container.remove(key)
        if (holder is BlockState) {
            val state = holder as BlockState
            state.update()
        }
    }

    fun getStringData(holder: PersistentDataHolder, key: NamespacedKey): String? {
        return getData(holder, PersistentDataType.STRING, key)
    }

    fun getStringArrayData(holder: PersistentDataHolder, key: NamespacedKey): Array<String>? {
        return getData(holder, STRING_ARRAY, key)
    }

    fun getDoubleArrayData(holder: PersistentDataHolder, key: NamespacedKey): DoubleArray {
        return getData(holder, DOUBLE_ARRAY, key)!!
    }

    fun getIntData(holder: PersistentDataHolder, key: NamespacedKey): Int {
        return getData(holder, PersistentDataType.INTEGER, key)!!
    }

    fun getDoubleData(holder: PersistentDataHolder, key: NamespacedKey): Double {
        return getData(holder, PersistentDataType.DOUBLE, key)!!
    }

    fun getBooleanData(holder: PersistentDataHolder, key: NamespacedKey): Boolean {
        val i = getIntData(holder, key)
        return i != 0
    }

    fun <Z> getData(item: ItemStack, type: PersistentDataType<*, Z>, key: NamespacedKey): Z? {
        val meta = item.itemMeta ?: return null
        return getData(meta, type, key)
    }

    fun setData(item: ItemStack, key: NamespacedKey, value: Any) {
        val meta = item.itemMeta ?: return
        setData(meta, key, value)
        item.itemMeta = meta
    }

    fun removeData(item: ItemStack, key: NamespacedKey) {
        val meta = item.itemMeta ?: return
        removeData(meta, key)
        item.itemMeta = meta
    }

    fun getStringData(item: ItemStack, key: NamespacedKey): String? {
        val meta = item.itemMeta
        return meta?.let { getStringData(it, key) }
    }

    fun getIntData(item: ItemStack, key: NamespacedKey): Int {
        val meta = item.itemMeta
        return meta?.let { getIntData(it, key) } ?: 0
    }

    fun getStringArrayData(item: ItemStack, key: NamespacedKey): Array<String>? {
        val meta = item.itemMeta
        return meta?.let { getStringArrayData(it, key) }
    }

    fun getDoubleArrayData(item: ItemStack, key: NamespacedKey): DoubleArray? {
        val meta = item.itemMeta
        return meta?.let { getDoubleArrayData(it, key) }
    }

    fun getDoubleData(item: ItemStack, key: NamespacedKey): Double {
        val meta = item.itemMeta
        return meta?.let { getDoubleData(it, key) } ?: 0
    }

    fun getBooleanData(item: ItemStack, key: NamespacedKey): Boolean {
        val meta = item.itemMeta
        return meta != null && getBooleanData(meta, key)
    }

    class DoubleArray : PersistentDataType<ByteArray, DoubleArray> {
        override fun getPrimitiveType(): Class<ByteArray> {
            return ByteArray::class.java
        }

        override fun getComplexType(): Class<DoubleArray> {
            return DoubleArray::class.java
        }

        override fun toPrimitive(complex: DoubleArray, context: PersistentDataAdapterContext): ByteArray {
            val bb = ByteBuffer.allocate(complex.size * 8)
            for (d in complex) {
                bb.putDouble(d)
            }
            return bb.array()
        }

        override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): DoubleArray {
            val bb = ByteBuffer.wrap(primitive)
            val dbuf = bb.asDoubleBuffer() // Make DoubleBuffer
            val a = DoubleArray(dbuf.remaining()) // Make an array of the correct size
            dbuf[a]
            return a
        }
    }

    class StringArray(private val charset: Charset) : PersistentDataType<ByteArray, Array<String>> {
        override fun getPrimitiveType(): Class<ByteArray> {
            return ByteArray::class.java
        }

        override fun getComplexType(): Class<Array<String>> {
            return Array<String>::class.java
        }

        override fun toPrimitive(
            strings: Array<String>,
            itemTagAdapterContext: PersistentDataAdapterContext
        ): ByteArray {
            val allStringBytes = arrayOfNulls<ByteArray>(strings.size)
            var total = 0
            for (i in allStringBytes.indices) {
                val bytes = strings[i].toByteArray(charset)
                allStringBytes[i] = bytes
                total += bytes.size
            }
            val buffer = ByteBuffer.allocate(total + allStringBytes.size * 4) // stores integers
            for (bytes in allStringBytes) {
                buffer.putInt(bytes!!.size)
                buffer.put(bytes)
            }
            return buffer.array()
        }

        override fun fromPrimitive(
            bytes: ByteArray,
            itemTagAdapterContext: PersistentDataAdapterContext
        ): Array<String> {
            val buffer = ByteBuffer.wrap(bytes)
            val list = ArrayList<String>()
            while (buffer.remaining() > 0) {
                if (buffer.remaining() < 4) break
                val stringLength = buffer.int
                if (buffer.remaining() < stringLength) break
                val stringBytes = ByteArray(stringLength)
                buffer[stringBytes]
                list.add(String(stringBytes, charset))
            }
            return list.toTypedArray()
        }
    }

    class UUIDDataType : PersistentDataType<ByteArray, UUID> {
        override fun getPrimitiveType(): Class<ByteArray> {
            return ByteArray::class.java
        }

        override fun getComplexType(): Class<UUID> {
            return UUID::class.java
        }

        override fun toPrimitive(complex: UUID, context: PersistentDataAdapterContext): ByteArray {
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(complex.mostSignificantBits)
            bb.putLong(complex.leastSignificantBits)
            return bb.array()
        }

        override fun fromPrimitive(primitive: ByteArray, context: PersistentDataAdapterContext): UUID {
            val bb = ByteBuffer.wrap(primitive)
            val firstLong = bb.long
            val secondLong = bb.long
            return UUID(firstLong, secondLong)
        }
    }
}