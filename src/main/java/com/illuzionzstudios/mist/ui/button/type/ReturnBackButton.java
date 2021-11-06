package com.illuzionzstudios.mist.ui.button.type;

import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.item.ItemCreator;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import lombok.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * A button that returns to a previous/parent {@link UserInterface}
 */
@RequiredArgsConstructor
@AllArgsConstructor
public final class ReturnBackButton extends Button {

    /**
     * Material for this button
     */
    @Getter
    @Setter
    private static XMaterial material = XMaterial.OAK_DOOR;

    /**
     * The title of this button
     */
    @Getter
    @Setter
    private static String title = "&4&lReturn";

    /**
     * The lore of this button
     */
    @Getter
    @Setter
    private static List<String> lore = Arrays.asList("", "Return back.");

    /**
     * The parent {@link UserInterface}
     */
    @NonNull
    private final UserInterface parentInterface;

    /**
     * Make a new instance of the {@link UserInterface} when showing
     */
    private boolean makeNewInstance = false;

    @Override
    public ItemStack getItem() {
        return ItemCreator.of(material).name(title).lores(lore).build().makeUIItem();
    }

    /**
     * Open the parent interface
     */
    @Override
    public ButtonListener getListener() {
        return ((player, ui, type, event) -> {
            // When clicking don't move items
            event.setCancelled(true);
            if (makeNewInstance)
                parentInterface.newInstance().show(player);
            else
                parentInterface.show(player);
        });
    }
}
