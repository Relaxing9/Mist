package com.illuzionzstudios.mist.util

import com.illuzionzstudios.mist.plugin.SpigotPlugin
import lombok.experimental.UtilityClass
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Utility class to deal with [org.bukkit.inventory.ItemStack].
 * This could be parsing the data or dealing with NBT data on the item
 */
@UtilityClass
class ItemUtil {
    /**
     * Identify key from our plugin
     */
    val LORE_FIX_PREFIX = SpigotPlugin.pluginName + "-"
    val NAME_FIX_PREFIX = SpigotPlugin.pluginName + "-"
    val TAG_SPLITTER = "\n"

    /**
     * A cache of name and lore keys
     */
    private val NAME_KEYS_CACHE: MutableMap<String, NamespacedKey> = HashMap()
    private val LORE_KEYS_CACHE: MutableMap<String, NamespacedKey> = HashMap()

    /**
     * Serialize a [ItemStack] into a string blob
     *
     * @param itemStack ItemStack to serialize
     * @return ItemStack as a string blob
     */
    fun serialize(itemStack: ItemStack?): String {
        val config = YamlConfiguration()
        config["i"] = itemStack
        return config.saveToString()
    }

    /**
     * Deserialize a string blob into a [ItemStack]
     * See [.serialize]
     *
     * @param stringBlob String blob to convert to [ItemStack]
     * @return Formed [ItemStack]. `null` if didn't deserialize properly
     */
    fun deserialize(stringBlob: String?): ItemStack? {
        val config = YamlConfiguration()
        try {
            config.loadFromString(stringBlob!!)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return config.getItemStack("i", null)
    }

    fun clear() {
        LORE_KEYS_CACHE.clear()
        NAME_KEYS_CACHE.clear()
    }

    /**
     * Insert a string value into lore at position and return new position of item
     */
    fun addToLore(lore: MutableList<String?>, pos: Int, value: String): Int {
        if (pos >= lore.size || pos < 0) {
            lore.add(value)
        } else {
            lore.add(pos, value)
        }
        return pos + 1
    }

    fun addLore(item: ItemStack, id: String, text: String, pos: Int) {
        val lines = text.split(TAG_SPLITTER.toRegex()).toTypedArray()
        addLore(item, id, Arrays.asList(*lines), pos)
    }

    fun addLore(item: ItemStack, id: String, text: List<String>, pos: Int) {
        var text = text
        var pos = pos
        val meta = item.itemMeta ?: return
        var lore = meta.lore
        if (lore == null) lore = ArrayList()
        text = formatText(text)
        val loreTag = StringBuilder()
        delLore(item, id)
        for (line in text) {
            pos = addToLore(lore, pos, line)
            if (loreTag.length > 0) loreTag.append(TAG_SPLITTER)
            loreTag.append(line)
        }
        meta.lore = lore
        item.itemMeta = meta
        addLoreTag(item, id, loreTag.toString())
    }

    fun delLore(item: ItemStack, id: String) {
        val meta = item.itemMeta ?: return
        val lore = meta.lore ?: return
        val index = getLoreIndex(item, id, 0)
        if (index < 0) return
        val lastIndex = getLoreIndex(item, id, 1)
        val diff = lastIndex - index
        for (i in 0 until diff + 1) {
            lore.removeAt(index)
        }
        meta.lore = lore
        item.itemMeta = meta
        delLoreTag(item, id)
    }

    fun getLoreIndex(item: ItemStack, id: String): Int {
        return getLoreIndex(item, id, 0)
    }

    fun getLoreIndex(item: ItemStack, id: String, type: Int): Int {
        val storedText: String = getStringData(item, getLoreKey(id)) ?: return -1
        val meta = item.itemMeta ?: return -1
        val lore = meta.lore ?: return -1
        val lines = storedText.split(TAG_SPLITTER.toRegex()).toTypedArray()
        var lastText: String? = null
        var count = 0
        if (type == 0) {
            for (i in lines.indices) {
                lastText = lines[i]
                if (!ChatColor.stripColor(lastText)!!.isEmpty()) {
                    break
                }
                count--
            }
        } else {
            for (i in lines.size downTo 1) {
                lastText = lines[i - 1]
                if (!ChatColor.stripColor(lastText)!!.isEmpty()) {
                    break
                }
                count++
            }
        }
        if (lastText == null) return -1
        val index = lore.indexOf(lastText) + count

        // Clean up invalid lore tags.
        if (index < 0) {
            delLoreTag(item, id)
        }
        return index
    }

    private fun getLoreKey(id2: String): NamespacedKey {
        val id = id2.lowercase(Locale.getDefault())
        return LORE_KEYS_CACHE.computeIfAbsent(id) { key: String? ->
            NamespacedKey(
                SpigotPlugin.instance,
                LORE_FIX_PREFIX + id
            )
        }
    }

    private fun getNameKey(id2: String): NamespacedKey {
        val id = id2.lowercase(Locale.getDefault())
        return NAME_KEYS_CACHE.computeIfAbsent(id) { key: String? ->
            NamespacedKey(
                SpigotPlugin.instance,
                NAME_FIX_PREFIX + id
            )
        }
    }

    fun addLoreTag(item: ItemStack, id: String, text: String) {
        setData(item, getLoreKey(id), text)
    }

    fun delLoreTag(item: ItemStack, id: String) {
        removeData(item, getLoreKey(id))
    }

    fun getLoreTag(item: ItemStack, id: String): String? {
        return getStringData(item, getLoreKey(id))
    }

    fun addNameTag(item: ItemStack, id: String, text: String) {
        setData(item, getNameKey(id), text)
    }

    fun delNameTag(item: ItemStack, id: String) {
        removeData(item, getNameKey(id))
    }

    fun getNameTag(item: ItemStack, id: String): String? {
        return getStringData(item, getNameKey(id))
    }
}