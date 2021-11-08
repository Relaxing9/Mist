package com.illuzionzstudios.mist.item

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import com.illuzionzstudios.mist.compatibility.ServerVersion
import com.illuzionzstudios.mist.compatibility.ServerVersion.V
import com.illuzionzstudios.mist.compatibility.XItemFlag
import com.illuzionzstudios.mist.compatibility.XProperty
import com.illuzionzstudios.mist.util.TextUtil
import com.illuzionzstudios.mist.util.TextUtil.formatText
import com.illuzionzstudios.mist.util.Valid
import com.illuzionzstudios.mist.util.Valid.checkBoolean
import com.illuzionzstudios.mist.util.Valid.checkNotNull
import lombok.*
import org.bukkit.ChatColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.util.*
import java.util.function.Consumer

/**
 * Utility class to easily build custom items. We can set flags,
 * names and lore, and enchantments. Provides a way to
 * just easily construct an item
 */
@Builder
class ItemCreator {
    /**
     * The actual item stack this represents
     */
    private val item: ItemStack? = null

    /**
     * The [XMaterial] of the item
     */
    private val material: XMaterial? = null

    /**
     * The amount of items in the stack
     */
    @Builder.Default
    private val amount = 1

    /**
     * Damage to the item for setting custom metadata
     */
    @Builder.Default
    private val damage = -1

    /**
     * Custom model data
     */
    @Builder.Default
    private val customModelData = 0

    /**
     * The display name of the item
     */
    private val name: String? = null

    /**
     * The lore strings to display
     */
    @Singular
    private val lores: List<String>? = null

    /**
     * The enchants applied for the item mapped by level
     */
    @Singular
    private val enchants: Map<XEnchantment, Int>? = null

    /**
     * The item flags
     */
    @Singular
    private val flags: MutableList<XItemFlag> = ArrayList()

    /**
     * The actual metadata of the item stack
     */
    private val meta: ItemMeta? = null

    /**
     * If the [ItemStack] has the unbreakable flag
     */
    private var unbreakable = false

    /**
     * Should we hide all tags from the item (enchants, etc.)?
     */
    @Builder.Default
    private var hideTags = false

    /**
     * Should we add glow to the item? (adds a fake enchant and uses
     * item flags to hide it)
     *
     *
     * The enchant is visible on older MC versions.
     */
    @Builder.Default
    private val glow = false

    /**
     * @return This item suitable for a [com.illuzionzstudios.mist.ui.UserInterface]
     */
    fun makeUIItem(): ItemStack {
        unbreakable = true
        hideTags = true
        return make()
    }

    /**
     * Finally construct the [ItemStack] from all parameters
     *
     * @return The built item
     */
    fun make(): ItemStack {
        // Make sure base item and material are set
        Valid.checkBoolean(
            material != null && material.parseMaterial() != null || item != null,
            "Material or item must be set!"
        )

        // Actual item we're building on
        val stack = item?.clone() ?: ItemStack(
            material!!.parseMaterial()!!, amount
        )
        val stackMeta = meta?.clone() ?: stack.itemMeta!!
        Valid.checkNotNull(stackMeta, "Item metadata was somehow null")

        // Skip if trying to build on air
        if (material == XMaterial.AIR) return stack

        // Set damage
        if (damage != -1) {
            try {
                stack.durability = damage.toShort()
            } catch (ignored: Throwable) {
            }
            try {
                if (stackMeta is Damageable) {
                    stackMeta.damage = damage
                }
            } catch (ignored: Throwable) {
            }
        }

        // Custom model data only in 1.14+
        if (ServerVersion.atLeast(V.v1_14) && customModelData != 0) stackMeta.setCustomModelData(customModelData)

        // Glow
        if (glow) {
            stackMeta.addEnchant(Enchantment.DURABILITY, 1, true)
            flags.add(XItemFlag.HIDE_ENCHANTS)
        }

        // Enchantments
        if (enchants != null) {
            for (ench in enchants.keys) {
                stackMeta.addEnchant(ench.parseEnchantment()!!, enchants[ench]!!, true)
            }
        }

        // Name and lore
        if (name != null) {
            stackMeta.setDisplayName(TextUtil.formatText("&r$name"))
        }
        if (lores != null && !lores.isEmpty()) {
            val coloredLores: MutableList<String> = ArrayList()
            lores.forEach(Consumer { line: String ->
                // Colour and split by \n
                val lines = Arrays.asList(*line.split("\\r?\\n".toRegex()).toTypedArray())
                // Append '&7' before every line instead of ugly purple italics
                lines.forEach(Consumer { line2: String -> coloredLores.add(TextUtil.formatText(ChatColor.GRAY.toString() + line2)) })
            })
            stackMeta.lore = coloredLores
        }

        // Unbreakable
        if (unbreakable) {
            if (ServerVersion.olderThan(V.v1_12)) {
                try {
                    val spigot = stackMeta.javaClass.getMethod("spigot").invoke(stackMeta)
                    spigot.javaClass.getMethod("setUnbreakable", Boolean::class.javaPrimitiveType).invoke(spigot, true)
                } catch (ignored: Throwable) {
                    // Probably 1.7.10, tough luck
                }
            } else {
                XProperty.UNBREAKABLE.apply(stackMeta, true)
            }
        }

        // Hide flags
        if (hideTags) {
            for (f in XItemFlag.values()) {
                if (!flags.contains(f)) {
                    flags.add(f)
                }
            }
        }

        // Apply flags
        for (flag in flags) {
            try {
                stackMeta.addItemFlags(ItemFlag.valueOf(flag.toString()))
            } catch (ignored: Throwable) {
            }
        }

        // Finally apply metadata
        stack.itemMeta = stackMeta
        return stack
    }

    companion object {
        //  -------------------------------------------------------------------------
        //  Constructing
        //  -------------------------------------------------------------------------
        /**
         * Convenience method to get a new item creator with material, name and lore set
         *
         * @param material The [XMaterial] to set
         * @param name     The name of the item
         * @param lore     Collection of lore strings
         * @return THe builder with these properties
         */
        fun of(material: XMaterial?, name: String?, lore: Collection<String>): ItemCreator.ItemCreatorBuilder {
            return of(material, name, *lore.toTypedArray())
        }

        /**
         * See [.of]
         */
        fun of(material: XMaterial?, name: String?, vararg lore: String?): ItemCreator.ItemCreatorBuilder {
            return ItemCreator.builder().material(material).name(name).lores(Arrays.asList(*lore)).hideTags(true)
        }

        /**
         * Get a new item creator from material
         *
         * @param mat existing material
         * @return the new item creator
         */
        fun of(mat: XMaterial?): ItemCreator.ItemCreatorBuilder {
            Valid.checkNotNull(mat, "Material cannot be null!")
            return ItemCreator.builder().material(mat)
        }
    }
}