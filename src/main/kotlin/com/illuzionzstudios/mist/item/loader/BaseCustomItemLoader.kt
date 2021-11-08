package com.illuzionzstudios.mist.item.loader

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import com.illuzionzstudios.mist.config.ConfigSection
import com.illuzionzstudios.mist.config.locale.MistString
import com.illuzionzstudios.mist.config.serialization.loader.type.YamlSectionLoader
import com.illuzionzstudios.mist.exception.PluginException
import com.illuzionzstudios.mist.item.CustomItem

/**
 * A loader for a [CustomItem] from a YAML config section. Can be implemented
 * with another custom item by overriding [.returnImplementedObject] and creating
 * new instance of custom item with loading already done for that item.
 */
abstract class BaseCustomItemLoader<T : CustomItem?>(section: ConfigSection) : YamlSectionLoader<T>(section) {
    override fun save(): Boolean {
        // TODO: Implement saving
        return true
    }

    override fun loadObject(configSection: ConfigSection): T {
        // Lets try build the item
        val item = returnImplementedObject(configSection)

        // Set base options
        item.setIdentifier(getLoader().nodeKey)
        val customName = getLoader().getString("item-name")
        if (customName != null) item.setCustomName(MistString(customName))
        val lore = getLoader().getStringList("lore")
        if (!lore.isEmpty()) item.setLore(MistString.Companion.fromStringList(lore))
        item.setAmount(configSection.getInt("amount", 1))
        item.setMaterial(XMaterial.matchXMaterial(configSection.getString("material", "AIR")!!).orElse(XMaterial.AIR))
        item.setCustomModelData(configSection.getInt("model-data"))
        val enchants: MutableMap<XEnchantment, Int> = HashMap()
        for (toParse in configSection.getStringList("enchants")) {
            val tokens = toParse.split(":".toRegex()).toTypedArray()
            enchants[XEnchantment.matchXEnchantment(tokens[0])
                .orElseThrow { PluginException("Enchant " + tokens[0] + " does not exist") }] = tokens[1].toInt()
        }
        item.setEnchants(enchants)
        item.setGlowing(configSection.getBoolean("glowing"))
        item.setUnbreakable(configSection.getBoolean("unbreakable"))
        item.setHideFlags(configSection.getBoolean("hide-flags"))
        return item
    }

    /**
     * Can be overridden to return the new custom item with the loading done
     * for that item.
     */
    protected abstract fun returnImplementedObject(configSection: ConfigSection?): T
}