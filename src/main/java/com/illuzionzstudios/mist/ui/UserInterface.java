package com.illuzionzstudios.mist.ui;

import com.illuzionzstudios.mist.Logger;
import com.illuzionzstudios.mist.exception.PluginException;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import com.illuzionzstudios.mist.scheduler.MinecraftScheduler;
import com.illuzionzstudios.mist.scheduler.Tickable;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.button.type.ReturnBackButton;
import com.illuzionzstudios.mist.ui.render.InterfaceDrawer;
import com.illuzionzstudios.mist.util.ReflectionUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.List;

/**
 * Our main menu (or interface) for all inventory interaction. We provide
 * a lot of functionality for adding buttons, storing items, and being
 * able to navigate through the menu easily. We can also have a parent menu
 * that can be returned to.
 */
public abstract class UserInterface implements Tickable {

    //  -------------------------------------------------------------------------
    //  Static variables
    //  -------------------------------------------------------------------------

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the current menu in order to keep
     * track of what menu is currently open
     */
    public static final String TAG_CURRENT = "UI_" + SpigotPlugin.getPluginName();

    /**
     * This is an internal metadata tag that the player has.
     *
     * This will set the name of the previous menu in order to
     * backtrack for returning menus
     */
    public static final String TAG_PREVIOUS = "UI_PREVIOUS_" + SpigotPlugin.getPluginName();

    /**
     * Registered buttons to this menu (via reflection) to add in rendering
     */
    private final List<Button> registeredButtons = new ArrayList<>();

    //  -------------------------------------------------------------------------
    //  Properties of an interface
    //  -------------------------------------------------------------------------

    /**
     * The parent interface we entered from. This allows us to return to a previous menu
     * Can be {@code null} if no parent
     */
    private final UserInterface parent;

    /**
     * The return button to display if applicable
     */
    private final Button returnButton;

    /**
     * Dictates if we called the method to register buttons
     * See {@link #registerButtonViaReflection(Field)}
     */
    private boolean buttonsRegisteredViaReflection = false;

    /**
     * Amount of slots in the inventory
     */
    private int size = 9 * 3;

    /**
     * This is the title to display at the top of the interface
     */
    private String title = "&8Menu";

    /**
     * This is the description of the menu that can be displayed
     * at a certain slot
     */
    @Getter(value = AccessLevel.PROTECTED)
    private String[] info = null;

    /**
     * This is the player currently viewing the menu.
     * Isn't set till displayed to a player
     */
    private Player viewer;

    /**
     * See {@link #UserInterface(UserInterface, boolean)}
     *
     * Creates a {@link UserInterface} with no parent
     */
    protected UserInterface() {
        this(null, false);
    }

    /**
     * Base constructor to create a {@link UserInterface} with the size 9 * 3
     * with a parent menu.
     *
     * You should set the size and title of the {@link UserInterface} in
     * the constructor.
     *
     * Note: The viewer is still null here
     *
     * @param parent The parent {@link UserInterface}
     * @param makeNewInstance If the {@link ReturnBackButton} makes a new instance
     *                        of the parent menu
     */
    protected UserInterface(final UserInterface parent, final boolean makeNewInstance) {
        this.parent = parent;

        returnButton = parent != null ? new ReturnBackButton(parent, makeNewInstance) : Button.makeEmpty();
    }

    //  -------------------------------------------------------------------------
    //  Getting menus
    //  -------------------------------------------------------------------------

    /**
     * Get the currently active menu for the player
     *
     * @param player The player to get menu for
     * @return Found interface or {@code null} See {@link #getInterfaceViaTag(Player, String)}
     */
    public static UserInterface getInterface(final Player player) {
        return getInterfaceViaTag(player, TAG_CURRENT);
    }

    /**
     * Get the previous active menu for the player
     *
     * @param player The player to get menu for
     * @return Found interface or {@code null} See {@link #getInterfaceViaTag(Player, String)}
     */
    public static UserInterface getPrevious(final Player player) {
        return getInterfaceViaTag(player, TAG_PREVIOUS);
    }

    /**
     * Get a {@link UserInterface} from the metadata on a player
     *
     * @param player The player to check metadata
     * @param tag The name of the tag storing the interface
     * @return Found {@link UserInterface} otherwise {@code null}
     */
    public static UserInterface getInterfaceViaTag(final Player player, final String tag) {
        if (player.hasMetadata(tag)) {
            // Cast from tag
            final UserInterface userInterface = (UserInterface) player.getMetadata(tag).get(0).value();
            Valid.checkNotNull(userInterface, "Interface was missing from " + player.getName() + "'s metadata " + tag + "tag!");

            return userInterface;
        }

        return null;
    }

    //  -------------------------------------------------------------------------
    //  Button utils
    //  -------------------------------------------------------------------------

    /**
     * Scans the class for every {@link Button} instance and registers it
     */
    protected final void registerButtons() {
        // Don't double register stuff
        registeredButtons.clear();

        // Register buttons explicitly given
        {
            final List<Button> buttons = getButtonsToRegister();

            if (buttons != null)
                registeredButtons.addAll(buttons);
        }

        // Register buttons declared as fields
        {
            Class<?> lookup = getClass();

            // Scan every class and super until interface class
            do
                for (final Field f : lookup.getDeclaredFields())
                    registerButtonViaReflection(f);
            while (UserInterface.class.isAssignableFrom(lookup = lookup.getSuperclass()));
        }
    }

    /**
     * Registers a {@link Button} into this {@link UserInterface} if the
     * field is a {@link Button}
     *
     * @param field The field to register
     */
    private void registerButtonViaReflection(final Field field) {
        field.setAccessible(true);

        final Class<?> clazz = field.getType();

        // Is just a button instance
        if (Button.class.isAssignableFrom(clazz)) {
            // Get button
            final Button button = (Button) ReflectionUtil.getFieldContent(field, this);

            Valid.checkNotNull(button, "Invalid button for names field " + field.getName());
            registeredButtons.add(button);
        } else if (Button[].class.isAssignableFrom(clazz)) {
            // Array of buttons
            Valid.checkBoolean(Modifier.isFinal(field.getModifiers()),
                    "Button[] field must be final: " + field);
            final Button[] buttons = (Button[]) ReflectionUtil.getFieldContent(field, this);

            Valid.checkBoolean(buttons != null && buttons.length > 0, "Null " + field.getName() + "[] in " + this);
            registeredButtons.addAll(Arrays.asList(buttons));
        }

        buttonsRegisteredViaReflection = true;
    }

    /**
     * @return A list of buttons to manually add instead of scanning
     */
    protected List<Button> getButtonsToRegister() {
        return null;
    }

    /**
     * Try to get a button with a specific icon from {@link ItemStack}
     *
     * @param icon {@link ItemStack} to find by
     * @return Found button otherwise null
     */
    public final Button getButton(final ItemStack icon) {
        if (!buttonsRegisteredViaReflection) registerButtons();

        if (icon != null) {
            for (final Button button : registeredButtons) {
                // Make sure valid button
                Valid.checkNotNull(button, "Menu button is null at " + getClass().getSimpleName());
                if (button.getItem() == null)
                    return null;

                if (equals(icon, button.getItem())) {
                    return button;
                }
            }
        }

        return null;
    }

    private boolean equals(final ItemStack stack1, final ItemStack stack2) {
        final ItemMeta meta1 = stack1.getItemMeta();
        final ItemMeta meta2 = stack2.getItemMeta();

        if (stack1.getType() != stack2.getType())
            return false;

        if (meta1 != null)
            return meta1.equals(meta2);

        return stack1.getAmount() == stack2.getAmount();
    }

    /**
     * Return a new instance of this interface
     *
     * You must override this in certain cases
     *
     * @return the new instance, of null
     * @throws PluginException if new instance could not be made, for example when the menu is
     *            taking constructor params
     */
    public UserInterface newInstance() {
        try {
            return ReflectionUtil.instantiate(getClass());
        } catch (final Throwable t) {
            try {
                final Object parent = getClass().getMethod("getParent").invoke(getClass());

                if (parent != null)
                    return ReflectionUtil.instantiate(getClass(), parent);
            } catch (final Throwable ignored) {
            }

            t.printStackTrace();
        }

        throw new PluginException("Could not instatiate menu of " + getClass()
                + ", override 'newInstance' and ensure constructor is public!");
    }

    //  -------------------------------------------------------------------------
    //  Rendering
    //  -------------------------------------------------------------------------

    /**
     * Build, render, and show our {@link UserInterface} to a player
     * Only used to firstly show it to a player, shouldn't be used to re-render
     *
     * @param player The player to show the menu to
     */
    public final void show(Player player) {
        Valid.checkNotNull(size, "Size not set in " + this + " (call setSize in your constructor)");
        Valid.checkNotNull(title, "Title not set in " + this + " (call setTitle in your constructor)");

        // Set our viewer
        viewer = player;

        preDisplay();

        // If buttons didn't get registered, do it ourselves
        if (!buttonsRegisteredViaReflection)
            registerButtons();

        // Render the menu
        final InterfaceDrawer drawer = InterfaceDrawer.of(size, title);

        // Compile bottom bar
        compileBottomBar().forEach(drawer::setItem);

        // Set items defined by classes upstream
        // Doesn't replace set items
        for (int i = 0; i < drawer.getSize(); i++) {
            final ItemStack item = getItemAt(i);

            if (item != null && !drawer.isSet(i))
                drawer.setItem(i, item);
        }

        // Call event
        onDisplay(drawer);

        // Set our previous menu if applicable
        final UserInterface previous = getInterface(player);
        if (previous != null)
            player.setMetadata(TAG_PREVIOUS, new FixedMetadataValue(SpigotPlugin.getInstance(), previous));

        // Register current menu
        MinecraftScheduler.get().synchronize(() -> {
            drawer.display(player);

            player.setMetadata(TAG_CURRENT, new FixedMetadataValue(SpigotPlugin.getInstance(), UserInterface.this));
        }, 1);
    }

    /**
     * Run any last minute registering before the interface is displayed
     */
    protected void preDisplay() {
    }

    /**
     * Called automatically before the menu is displayed but after all items have
     * been drawn
     *
     * Override for custom last-minute modifications
     *
     * @param drawer The drawer for the interface
     */
    protected void onDisplay(final InterfaceDrawer drawer) {
    }

    /**
     * "Restart" this interface. This means re-registering all buttons,
     * and redrawing all items
     */
    protected final void restart() {
        preDisplay();
        registerButtons();
        redraw();
    }

    /**
     * Tick rendering
     * To be overridden
     */
    @Override
    public void tick() {
    }

    /**
     * Simply re-render the inventory and bottom items
     */
    protected final void redraw() {
        final Inventory inv = getViewer().getOpenInventory().getTopInventory();

        // Make sure a chest inventory and not something else
        Valid.checkBoolean(inv.getType() == InventoryType.CHEST,
                getViewer().getName() + "'s inventory closed in the meanwhile (now == " + inv.getType() + ").");

        for (int i = 0; i < size; i++) {
            final ItemStack item = getItemAt(i);

            Valid.checkBoolean(i < inv.getSize(), "Item (" + (item != null ? item.getType() : "null") + ") position ("
                    + i + ") > inv size (" + inv.getSize() + ")");
            inv.setItem(i, item);
        }

        compileBottomBar().forEach(inv::setItem);
        getViewer().updateInventory();
    }

    /**
     * Map the buttons placed for navigation to their slots
     *
     * @return Map of items
     */
    private Map<Integer, ItemStack> compileBottomBar() {
        final Map<Integer, ItemStack> items = new HashMap<>();

        if (addInfoButton() && getInfo() != null)
            items.put(getInfoButtonPosition(), Button.makeInfo(getInfo()).getItem());

        if (addReturnButton() && !(returnButton instanceof Button.IconButton))
            items.put(getReturnButtonPosition(), returnButton.getItem());

        return items;
    }

    //  -------------------------------------------------------------------------
    //  Final getters and setters
    //  -------------------------------------------------------------------------

    /**
     * Returns the item at a certain slot
     *
     * To be overridden by the type of menu to get the item
     *
     * @param slot the slow
     * @return the item, or null if no icon at the given slot (default)
     */
    public ItemStack getItemAt(final int slot) {
        return null;
    }

    /**
     * Get the info button position
     *
     * @return the slot which info buttons is located on
     */
    protected int getInfoButtonPosition() {
        return size - 9;
    }

    /**
     * Should we automatically add the return button to the bottom left corner?
     *
     * @return true if the return button should be added, true by default
     */
    protected boolean addReturnButton() {
        return true;
    }

    /**
     * Should we automatically add an info button {@link #getInfo()} at the
     * {@link #getInfoButtonPosition()} ?
     *
     * @return If to add button
     */
    protected boolean addInfoButton() {
        return true;
    }

    /**
     * Get the return button position
     *
     * @return the slot which return buttons is located on
     */
    protected int getReturnButtonPosition() {
        return size - 1;
    }

    /**
     * Calculates the center slot of this menu
     *
     * <p>
     * Credits to Gober at
     * https://www.spigotmc.org/threads/get-the-center-slot-of-a-menu.379586/
     *
     * @return the estimated center slot
     */
    protected final int getCenterSlot() {
        final int pos = size / 2;

        return size % 2 == 1 ? pos : pos - 5;
    }

    /**
     * The title of this menu
     *
     * @return the menu title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the title of this inventory, this change is not reflected in client, you
     * must call {@link #restart()} ()} to take change
     *
     * @param title the new title
     */
    protected final void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Return the parent menu or null
     *
     * @return
     */
    public final UserInterface getParent() {
        return parent;
    }

    /**
     * Get the size of this menu
     *
     * @return
     */
    public final Integer getSize() {
        return size;
    }

    /**
     * Sets the size of this menu (without updating the player container - if you
     * want to update it call {@link #restart()} ()})
     *
     * @param size
     */
    protected final void setSize(final Integer size) {
        this.size = size;
    }

    /**
     * Set the menu's description
     *
     * <p>
     * Used to create an info bottom in bottom left corner, see
     * {@link Button#makeInfo(String...)}
     *
     * @param info the info to set
     */
    protected final void setInfo(final String... info) {
        this.info = info;
    }

    /**
     * Get the viewer that this instance of this menu is associated with
     *
     * @return the viewer of this instance, or null
     */
    protected final Player getViewer() {
        return viewer;
    }

    /**
     * Sets the viewer for this instance of this menu
     *
     * @param viewer The new viewer of the menu. Only sets the player
     *               doesn't perform any magic
     */
    protected final void setViewer(final Player viewer) {
        this.viewer = viewer;
    }

    /**
     * Return the top opened inventory if viewer exists
     *
     * @return The open inventory instance
     */
    protected final Inventory getInventory() {
        Valid.checkNotNull(viewer, "Cannot get inventory when there is no viewer!");

        final Inventory topInventory = viewer.getOpenInventory().getTopInventory();
        Valid.checkNotNull(topInventory, "Top inventory is null!");

        return topInventory;
    }

    /**
     * Get the open inventory content to match the array length, cloning items
     * preventing ID mismatch in yaml files
     *
     * @param from The slot to start from
     * @param to The slot to end at
     * @return The array of found {@link ItemStack} can contain {@link org.bukkit.Material#AIR}
     */
    protected final ItemStack[] getContent(final int from, final int to) {
        final ItemStack[] content = getInventory().getContents();
        final ItemStack[] copy = new ItemStack[content.length];

        for (int i = from; i < copy.length; i++) {
            final ItemStack item = content[i];

            copy[i] = item != null ? item.clone() : null;
        }

        return Arrays.copyOfRange(copy, from, to);
    }

    //  -------------------------------------------------------------------------
    //  Interface events
    //  -------------------------------------------------------------------------

    /**
     * Master method called when the interface is clicked on. Calls methods to be implemented
     * and handles click logic.
     *
     * It passes down to {@link #onInterfaceClick(Player, int, ItemStack, InventoryClickEvent)}
     *
     * @param player The player clicking the menu
     * @param slot The slot that was clicked
     * @param action The type of action performed
     * @param click How the slot was clicked
     * @param cursor What {@link ItemStack} was on the cursor
     * @param clicked The clicked {@link ItemStack}
     * @param cancelled If the event was cancelled
     * @param event The actual event if needed
     */
    protected void onInterfaceClick(final Player player, final int slot, final InventoryAction action, final ClickType click,
                                    final ItemStack cursor, final ItemStack clicked, final boolean cancelled, final InventoryClickEvent event) {
        final InventoryView openedInventory = player.getOpenInventory();

        onInterfaceClick(player, slot, clicked, event);

        // Delay by 1 tick to get the accurate item in slot
        MinecraftScheduler.get().synchronize(() -> {
            // Make sure inventory is still open 1 tick later
            if (openedInventory.equals(player.getOpenInventory())) {
                final Inventory topInventory = openedInventory.getTopInventory();

                if (action.toString().contains("PLACE") || action.toString().equals("SWAP_WITH_CURSOR"))
                    onItemPlace(player, slot, topInventory.getItem(slot), event);
            }
        }, 1);
    }

    /**
     * Called automatically when the interface is clicked
     *
     * @param player The player clicking the menu
     * @param slot The slot that was clicked
     * @param clicked The clicked {@link ItemStack}
     */
    protected void onInterfaceClick(final Player player, final int slot, final ItemStack clicked, final InventoryClickEvent event) {
        // By default cancel moving items
        event.setCancelled(true);
    }

    /**
     * Called automatically when an item is placed to the menu
     *
     * @param player The player clicking the menu
     * @param slot The slot that was clicked
     * @param placed The {@link ItemStack} that was placed
     */
    protected void onItemPlace(final Player player, final int slot, final ItemStack placed, final InventoryClickEvent event) {
        // By default cancel moving items
        event.setCancelled(true);
    }

    /**
     * Called when a registered button is clicked on
     *
     * @param player The player clicking the menu
     * @param slot The slot that was clicked
     * @param action The type of action performed
     * @param click How the slot was clicked
     * @param button The {@link Button} object clicked
     */
    protected void onButtonClick(final Player player, final int slot, final InventoryAction action,
                                 final ClickType click, final Button button, final InventoryClickEvent event) {
        // By default cancel moving items
        event.setCancelled(true);
        button.getListener().onClickInInterface(player, this, click, event);
    }

    /**
     * Called when the interface is closed
     *
     * @param player The player who closed the interface
     * @param inventory The {@link Inventory} instance ended
     */
    protected void onInterfaceClose(final Player player, final Inventory inventory) {
    }
}
