package com.illuzionzstudios.mist.ui.button.type;

import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.render.ItemCreator;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.Callable;

/**
 * A simple button to open another {@link com.illuzionzstudios.mist.ui.UserInterface}
 */
public final class InterfaceButton extends Button {

    /**
     * Sometimes you need to allocate data when you create the button,
     * but these data are not yet available when you make new instance of this button
     *
     * Use this helper to set them right before showing the button
     */
    private final Callable<UserInterface> interfaceLateBind;

    /**
     * Instance of {@link UserInterface} to open
     */
    private final UserInterface toOpen;

    /**
     * The icon to display
     */
    @Getter
    private final ItemStack item;

    /**
     * Create a new instanceof using {@link UserInterface#newInstance()} when showing the interface?
     */
    private final boolean newInstance;

    /**
     * Create a new button that triggers another menu
     *
     * @param menuClass Class of the menu to create
     * @param material Material for the icon
     * @param name Name for the icon
     * @param lore Lore for the icon
     */
    public InterfaceButton(final Class<? extends UserInterface> menuClass, final XMaterial material, final String name, final String... lore) {
        this(null, () -> ReflectionUtil.instantiate(menuClass), ItemCreator.of(material, name, lore).hideTags(true).build().make(), false);
    }

    /**
     * Create a new button that triggers another menu
     *
     * @param menuLateBind The callable to create the menu
     * @param item The item creator for the icon
     */
    public InterfaceButton(final Callable<UserInterface> menuLateBind, final ItemCreator.ItemCreatorBuilder item) {
        this(null, menuLateBind, item.hideTags(true).build().make(), false);
    }

    /**
     * Create a new button that triggers another menu
     *
     * @param menuLateBind The callable to create the menu
     * @param item The icon for the button
     */
    public InterfaceButton(final Callable<UserInterface> menuLateBind, final ItemStack item) {
        this(null, menuLateBind, item, false);
    }

    /**
     * Create a new button that triggers another menu
     *
     * @param menu Instance of menu to create
     * @param material Material for the icon
     * @param name Name for the icon
     * @param lore Lore for the icon
     */
    public InterfaceButton(final UserInterface menu, final XMaterial material, final String name, final String... lore) {
        this(menu, ItemCreator.of(material, name, lore));
    }

    /**
     * Create a new button that triggers another menu
     *
     * @param menu Instance of menu to create
     * @param item The item creator for the icon
     */
    public InterfaceButton(final UserInterface menu, final ItemCreator.ItemCreatorBuilder item) {
        this(menu, null, item.hideTags(true).build().make(), false);
    }

    /**
     * Create a new button that triggers another menu
     *
     * @param menu Instance of menu to create
     * @param item The icon for the button
     */
    public InterfaceButton(final UserInterface menu, final ItemStack item) {
        this(menu, null, item, false);
    }

    public InterfaceButton(final UserInterface menu, final ItemStack item, final boolean newInstance) {
        this(menu, null, item, newInstance);
    }

    // Private constructor
    private InterfaceButton(final UserInterface menuToOpen, final Callable<UserInterface> menuLateBind, final ItemStack item, final boolean newInstance) {
        this.toOpen = menuToOpen;
        this.interfaceLateBind = menuLateBind;
        this.item = item;
        this.newInstance = newInstance;
    }

    @Override
    public ButtonListener getListener() {
        return ((player, ui, type, event) -> {
            if (interfaceLateBind != null) {
                UserInterface menuToOpen;

                // Try set the menu afterwards
                try {
                    menuToOpen = interfaceLateBind.call();
                } catch (final Exception ex) {
                    Logger.displayError(ex, "Could not open interface via button in " + this.toOpen.getTitle());
                    return;
                }

                if (newInstance)
                    menuToOpen = menuToOpen.newInstance();

                menuToOpen.show(player);

            } else {
                Valid.checkNotNull(toOpen, "Report / ButtonTrigger requires either 'late bind menu' or normal menu to be set!");

                if (newInstance)
                    toOpen.newInstance().show(player);
                else
                    toOpen.show(player);
            }
        });
    }
}
