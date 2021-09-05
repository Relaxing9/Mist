package com.illuzionzstudios.mist.ui.type;

import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.item.ItemCreator;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Simple interface to confirm an action
 */
public class ConfirmUI extends UserInterface {

    /**
     * Action to run when after answered.
     * Returns whether was accepted or not
     */
    public final Consumer<Boolean> confirmAction;

    /**
     * Button to deny
     */
    public final Button denyButton;

    /**
     * Button to confirm
     */
    public final Button confirmButton;

    /**
     * @param confirmAction Action to run after confirmed/answered
     */
    public ConfirmUI(Consumer<Boolean> confirmAction) {
        super(null, false);
        setTitle("&8Are you sure?");
        setSize(27);

        this.confirmAction = confirmAction;

        denyButton = Button.of(ItemCreator.builder()
                .material(XMaterial.RED_DYE)
                .name(PluginLocale.INTERFACE_CONFIRM_CONFIRM_NAME.toString())
                .lore(PluginLocale.INTERFACE_CONFIRM_CONFIRM_LORE.toString())
                .glow(true)
                .build(),
                (player, ui, clickType, event) -> {
                    confirmAction.accept(false);
                });

        confirmButton = Button.of(ItemCreator.builder()
                .material(XMaterial.LIME_DYE)
                .name(PluginLocale.INTERFACE_CONFIRM_DENY_NAME.toString())
                .lore(PluginLocale.INTERFACE_CONFIRM_DENY_LORE.toString())
                .glow(true)
                .build(),
                (player, ui, clickType, event) -> {
                    confirmAction.accept(true);
                });
    }

    @Override
    public ItemStack getItemAt(final int slot) {
        if (slot == 11) {
            return confirmButton.getItem();
        } else if (slot == 15) {
            return denyButton.getItem();
        }

        // Else placeholder item
        return ItemCreator.builder().name(" ").material(XMaterial.BLACK_STAINED_GLASS_PANE).build().makeUIItem();
    }

}
