package com.illuzionzstudios.mist.item;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.compatibility.XItemFlag;
import com.illuzionzstudios.mist.config.locale.MistString;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A custom item is a custom item that is loaded from the config. This
 * allows us to customise things like display name and lore from a {@link com.illuzionzstudios.mist.config.ConfigSection}
 * and turn it into an item. Can be implemented to have custom items that are configured
 * by the config extending on the base functionality.
 *
 * This differs from {@link ItemCreator} as that is strictly for creating an item
 * stack where this is to create an item stack with a bit more functionality and
 * have our own sort of data attached to it.
 *
 * Contains methods for manipulating item
 */
@Getter
@Builder
@ToString
public class CustomItem {

    /**
     * Actual item stack constructed to perform more operations on
     */
    @NotNull
    private ItemStack item;

    /**
     * The identifier of this custom item. Usually so we can
     * include this in a map if need be
     */
    @NotNull
    private String identifier;

    /**
     * The material of this item
     */
    @NotNull
    private XMaterial material;

    /**
     * Custom name of the item
     */
    @Nullable
    private MistString customName;

    /**
     * Custom lore of the item
     */
    @Nullable
    private List<MistString> lore;

    /**
     * Amount of the item
     */
    @Builder.Default
    private int amount = 1;

    /**
     * Damage to the item for setting custom metadata
     */
    @Builder.Default
    private final int damage = -1;

    /**
     * Item custom model data
     */
    @Builder.Default
    private int customModelData = 0;

    /**
     * Map of all enchantments
     */
    @Nullable
    private Map<XEnchantment, Integer> enchants;

    /**
     * If the item is glowing
     */
    private boolean glowing;

    /**
     * If the item is unbreakable
     */
    private boolean unbreakable;

    /**
     * If to hide all flags
     */
    private boolean hideFlags;

    /**
     * Actually build the item. Must be called before any kind of
     * registering or giving of the item is done. If is overriden must
     * call this super method
     */
    public void buildItem() {
        ItemCreator.ItemCreatorBuilder creator = ItemCreator.of(material);

        if (customName != null)
            creator.name(customName.toString());

        if (lore != null)
            creator.lores(MistString.fromMistList(lore));

        creator.amount(amount);
        creator.customModelData(customModelData);
        creator.enchants(enchants);
        creator.glow(glowing);
        creator.unbreakable(unbreakable);
        creator.hideTags(hideFlags);

        this.item = creator.build().make();
        customiseItem();
    }

    /**
     * Do custom things to the item with extra loaded stuff right after {@link #item} instance
     * is set.
     */
    protected void customiseItem() {}

    /**
     * Give this item to a player
     *
     * @param player Player receiving item
     */
    public void givePlayer(final Player player) {
        player.getInventory().addItem(this.item);
    }

}
