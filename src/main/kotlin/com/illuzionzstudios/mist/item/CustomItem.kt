package com.illuzionzstudios.mist.item

import com.cryptomorin.xseries.XEnchantment
import com.cryptomorin.xseries.XMaterial
import com.illuzionzstudios.mist.config.locale.MistString
import lombok.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * A custom item is a custom item that is loaded from the config. This
 * allows us to customise things like display name and lore from a [com.illuzionzstudios.mist.config.ConfigSection]
 * and turn it into an item. Can be implemented to have custom items that are configured
 * by the config extending on the base functionality.
 *
 *
 * This differs from [ItemCreator] as that is strictly for creating an item
 * stack where this is to create an item stack with a bit more functionality and
 * have our own sort of data attached to it.
 *
 *
 * Is not a builder as we should set everything manually.
 *
 *
 * Contains methods for manipulating item
 */
@Getter
@Setter
@ToString
class CustomItem {
    /**
     * Damage to the item for setting custom metadata
     */
    @Builder.Default
    private val damage = -1

    /**
     * Actual item stack constructed to perform more operations on
     */
    private var item: ItemStack? = null

    /**
     * The identifier of this custom item. Usually so we can
     * include this in a map if need be
     */
    @Builder.Default
    private val identifier = "null"

    /**
     * The material of this item
     */
    @Builder.Default
    private val material = XMaterial.AIR

    /**
     * Custom name of the item
     */
    private val customName: MistString? = null

    /**
     * Custom lore of the item
     */
    private val lore: List<MistString>? = null

    /**
     * Amount of the item
     */
    @Builder.Default
    private val amount = 1

    /**
     * Item custom model data
     */
    @Builder.Default
    private val customModelData = 0

    /**
     * Map of all enchantments
     */
    private val enchants: Map<XEnchantment, Int>? = null

    /**
     * If the item is glowing
     */
    private val glowing = false

    /**
     * If the item is unbreakable
     */
    private val unbreakable = false

    /**
     * If to hide all flags
     */
    private val hideFlags = false

    /**
     * Actually build the item. Must be called before any kind of
     * registering or giving of the item is done. If is overriden must
     * call this super method
     */
    fun buildItem(): ItemStack {
        val creator: ItemCreator.ItemCreatorBuilder = ItemCreator.Companion.of(material)
        if (customName != null && !customName.toString().trim { it <= ' ' }
                .isEmpty()) creator.name(customName.toString())
        if (lore != null) creator.lores(MistString.Companion.fromList(lore))
        creator.amount(amount)
        creator.customModelData(customModelData)
        creator.enchants(enchants)
        creator.glow(glowing)
        creator.unbreakable(unbreakable)
        creator.hideTags(hideFlags)
        item = creator.build().make()
        customiseItem()
        return item!!.clone()
    }

    /**
     * Do custom things to the item with extra loaded stuff right after [.item] instance
     * is set.
     */
    protected fun customiseItem() {}

    /**
     * Makes sure item is set. Run where we need to make sure it isn't null
     */
    private fun checkBuilt() {
        if (item == null) buildItem()
    }

    /**
     * Give this item to a player
     *
     * @param player Player receiving item
     */
    fun givePlayer(player: Player) {
        checkBuilt()
        player.inventory.addItem(item)
    }
}