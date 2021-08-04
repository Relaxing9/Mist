package com.illuzionzstudios.mist.ui.type;

import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.Mist;
import com.illuzionzstudios.mist.exception.PluginException;
import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.ui.button.Button;
import com.illuzionzstudios.mist.ui.render.InterfaceDrawer;
import com.illuzionzstudios.mist.ui.render.ItemCreator;
import com.illuzionzstudios.mist.util.MathUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.Getter;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An interface that displays elements and can switch between pages
 *
 * @param <T> The type of {@link Object} displayed in the interface
 */
public abstract class PagedInterface<T> extends UserInterface {

    /**
     * The pages by the page number, containing a list of items
     */
    @Getter
    private final Map<Integer, List<T>> pages;

    /**
     * The current page
     */
    @Getter
    private int currentPage = 1;

    /**
     * The next button automatically generated
     */
    private Button nextButton;

    /**
     * The "go to previous page" button automatically generated
     */
    private Button prevButton;

    /**
     * Create a new paged menu where each page has 3 rows + 1 bottom bar
     *
     * @param pages
     *            the pages
     */
    protected PagedInterface(final Iterable<T> pages) {
        this(null, pages);
    }

    /**
     * Create a new paged menu
     *
     * @param parent
     *            the parent menu
     * @param pages
     *            the pages the pages
     */
    protected PagedInterface(final UserInterface parent, final Iterable<T> pages) {
        this(null, parent, pages, false);
    }

    /**
     * Create a new paged menu
     *
     * @param parent
     * @param pages
     * @param returnMakesNewInstance
     */
    protected PagedInterface(final UserInterface parent, final Iterable<T> pages,
                         final boolean returnMakesNewInstance) {
        this(null, parent, pages, returnMakesNewInstance);
    }

    /**
     * Create a new paged menu
     *
     * @param pageSize
     *            size of the menu, a multiple of 9 (keep in mind we already add
     *            1 row there)
     * @param pages
     *            the pages
     *
     * @deprecated we recommend you don't set the page size for the menu to
     *             autocalculate
     */
    @Deprecated
    protected PagedInterface(final int pageSize, final Iterable<T> pages) {
        this(pageSize, null, pages);
    }

    /**
     * Create a new paged menu
     *
     * @param pageSize
     *            size of the menu, a multiple of 9 (keep in mind we already add
     *            1 row there)
     * @param parent
     *            the parent menu
     * @param pages
     *            the pages the pages
     * @deprecated we recommend you don't set the page size for the menu to
     *             autocalculate
     */
    @Deprecated
    protected PagedInterface(final int pageSize, final UserInterface parent,
                         final Iterable<T> pages) {
        this(pageSize, parent, pages, false);
    }

    /**
     * Create a new paged menu
     *
     * @param pageSize
     * @param parent
     * @param pages
     * @param returnMakesNewInstance *
     * @deprecated we recommend you don't set the page size for the menu to
     *             autocalculate
     */
    @Deprecated
    protected PagedInterface(final int pageSize, final UserInterface parent,
                         final Iterable<T> pages, final boolean returnMakesNewInstance) {
        this((Integer) pageSize, parent, pages, returnMakesNewInstance);
    }

    /**
     * Create a new paged menu
     *
     * @param pageSize
     *            size of the menu, a multiple of 9 (keep in mind we already add
     *            1 row there)
     * @param parent
     *            the parent menu
     * @param pages
     *            the pages the pages
     * @param returnMakesNewInstance
     *            should we re-instatiate the parent menu when returning to it?
     */
    private PagedInterface(final Integer pageSize, final UserInterface parent, final Iterable<T> pages, final boolean returnMakesNewInstance) {
        super(parent, returnMakesNewInstance);

        final int items = getItemAmount(pages);
        final int autoPageSize = pageSize != null ? pageSize : items <= 9 ? 9 * 1 : items <= 9 * 2 ? 9 * 2 : items <= 9 * 3 ? 9 * 3 : items <= 9 * 4 ? 9 * 4 : 9 * 5;

        this.currentPage = 1;
        this.pages = fillPages(autoPageSize, pages);

        setSize(9 + autoPageSize);
        setButtons();
    }

    /**
     * Dynamically populates the pages
     *
     * @param items all items that will be split
     * @return the map containing pages and their items
     */
    private Map<Integer, List<T>> fillPages(int cellSize, Iterable<T> items) {
        final List<T> allItems = Mist.toList(items);

        final Map<Integer, List<T>> pages = new HashMap<>();
        final int pageCount = allItems.size() == cellSize ? 0 : allItems.size() / cellSize;

        for (int i = 0; i <= pageCount; i++) {
            final List<T> pageItems = new ArrayList<>();

            final int down = cellSize * i;
            final int up = down + cellSize;

            for (int valueIndex = down; valueIndex < up; valueIndex++)
                if (valueIndex < allItems.size()) {
                    final T page = allItems.get(valueIndex);

                    pageItems.add(page);
                }

                else
                    break;

            pages.put(i, pageItems);
        }

        return pages;
    }

    private int getItemAmount(final Iterable<T> pages) {
        int amount = 0;

        for (final T t : pages)
            amount++;

        return amount;
    }

    // Render the next/prev buttons
    private void setButtons() {
        final boolean hasPages = pages.size() > 1;

        // Set previous button
        prevButton = hasPages ? new Button() {
            final boolean canGo = currentPage > 1;

            @Override
            public ButtonListener getListener() {
                return (player, ui, type, event) -> {
                    if (canGo) {
                        currentPage = MathUtil.range(currentPage - 1, 1,
                                pages.size());

                        updatePage();
                    }
                };
            }

            @Override
            public ItemStack getItem() {
                final int str = currentPage - 1;

                return ItemCreator.of(
                        canGo ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE)
                        .name(str == 0 ? "&7First Page" : "&8<< &fPage " + str)
                        .build().make();
            }
        } : Button.makeEmpty();

        // Set next page button
        nextButton = hasPages ? new Button() {
            final boolean canGo = currentPage < pages.size();

            @Override
            public ButtonListener getListener() {
                return (player, ui, type, event) -> {
                    if (canGo) {
                        currentPage = MathUtil.range(currentPage + 1, 1,
                                pages.size());

                        updatePage();
                    }
                };
            }

            @Override
            public ItemStack getItem() {
                final boolean last = currentPage == pages.size();

                return ItemCreator.of(
                        canGo ? XMaterial.LIME_DYE : XMaterial.GRAY_DYE)
                        .name(last
                                ? "&7Last Page"
                                : "Page " + (currentPage + 1) + " &8>>")
                        .build().make();
            }
        } : Button.makeEmpty();
    }

    // Reinits the menu and plays the anvil sound
    private void updatePage() {
        setButtons();
        redraw();
        registerButtons();
    }

    // Compile title and page numbers
    private String compileTitle() {
        final boolean canAddNumbers = addPageNumbers() && pages.size() > 1;

        return getTitle() + (canAddNumbers
                ? " &8" + currentPage + "/" + pages.size()
                : "");
    }

    /**
     * Automatically prepend the title with page numbers
     *
     * Override for a custom last-minute implementation, but
     * ensure to call the super method otherwise no title will
     * be set in {@link InterfaceDrawer}
     */
    @Override
    protected void onDisplay(final InterfaceDrawer drawer) {
        drawer.setTitle(compileTitle());
    }

    /**
     * Return the {@link ItemStack} representation of an item on a certain page
     *
     * Use {@link ItemCreator} for easy creation.
     *
     * @param item
     *            the given object, for example Arena
     * @return the itemstack, for example diamond sword having arena name
     */
    protected abstract ItemStack convertToItemStack(T item);

    /**
     * Called automatically when an item is clicked
     *
     * @param player
     *            the player who clicked
     * @param item
     *            the clicked item
     * @param click
     *            the click type
     * @param event
     *            the click event
     */
    protected abstract void onPageClick(Player player, T item, ClickType click, InventoryClickEvent event);

    /**
     * Utility: Shall we send update packet when the menu is clicked?
     *
     * @return true by default
     */
    protected boolean updateButtonOnClick() {
        return true;
    }

    /**
     * Return true if you want our system to add page/totalPages suffix after
     * your title, true by default
     *
     * @return
     */
    protected boolean addPageNumbers() {
        return true;
    }

    /**
     * Return if there are no items at all
     *
     * @return
     */
    protected boolean isEmpty() {
        return pages.isEmpty() || pages.get(0).isEmpty();
    }

    /**
     * Automatically get the correct item from the actual page, including
     * prev/next buttons
     *
     * @param slot
     *            the slot
     * @return the item, or null
     */
    @Override
    public ItemStack getItemAt(final int slot) {
        if (slot < getCurrentPageItems().size()) {
            final T object = getCurrentPageItems().get(slot);

            if (object != null)
                return convertToItemStack(object);
        }

        if (slot == getSize() - 6)
            return prevButton.getItem();

        if (slot == getSize() - 4)
            return nextButton.getItem();

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onInterfaceClick(final Player player, final int slot,
                                  final InventoryAction action, final ClickType click,
                                  final ItemStack cursor, final ItemStack clicked,
                                  final boolean cancelled, final InventoryClickEvent event) {
        if (slot < getCurrentPageItems().size()) {
            final T obj = getCurrentPageItems().get(slot);

            if (obj != null) {
                final val prevType = player.getOpenInventory().getType();
                onPageClick(player, obj, click, event);

                if (updateButtonOnClick()
                        && prevType == player.getOpenInventory().getType())
                    player.getOpenInventory().getTopInventory().setItem(slot,
                            getItemAt(slot));
            }
        }
    }

    // Do not allow override
    @Override
    public final void onButtonClick(final Player player, final int slot,
                                    final InventoryAction action, final ClickType click,
                                    final Button button, final InventoryClickEvent event) {
        super.onButtonClick(player, slot, action, click, button, event);
    }

    // Do not allow override
    @Override
    public final void onInterfaceClick(final Player player, final int slot,
                                  final ItemStack clicked, final InventoryClickEvent event) {
        throw new PluginException("Simplest click unsupported");
    }

    // Get all items in a page
    private List<T> getCurrentPageItems() {
        Valid.checkBoolean(pages.containsKey(currentPage - 1),
                "The menu has only " + pages.size() + " pages, not "
                        + currentPage + "!");

        return pages.get(currentPage - 1);
    }

}
