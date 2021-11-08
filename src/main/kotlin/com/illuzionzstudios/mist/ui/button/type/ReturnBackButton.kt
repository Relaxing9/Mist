package com.illuzionzstudios.mist.ui.button.type

import com.cryptomorin.xseries.XMaterial
import com.illuzionzstudios.mist.item.ItemCreator
import com.illuzionzstudios.mist.ui.UserInterface
import com.illuzionzstudios.mist.ui.button.Button
import lombok.*
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * A button that returns to a previous/parent [UserInterface]
 */
@RequiredArgsConstructor
@AllArgsConstructor
class ReturnBackButton : Button() {
    /**
     * The parent [UserInterface]
     */
    private val parentInterface: UserInterface = null

    /**
     * Make a new instance of the [UserInterface] when showing
     */
    private val makeNewInstance = false
    override val item: ItemStack?
        get() = ItemCreator.Companion.of(material).name(title).lores(lore).build()
            .makeUIItem()// When clicking don't move items

    /**
     * Open the parent interface
     */
    override val listener: ButtonListener
        get() = ButtonListener { player: Player, ui: UserInterface?, type: ClickType?, event: InventoryClickEvent ->
            // When clicking don't move items
            event.isCancelled = true
            if (makeNewInstance) parentInterface.newInstance().show(player) else parentInterface.show(player)
        }

    companion object {
        /**
         * Material for this button
         */
        @Getter
        @Setter
        private val material = XMaterial.OAK_DOOR

        /**
         * The title of this button
         */
        @Getter
        @Setter
        private val title = "&4&lReturn"

        /**
         * The lore of this button
         */
        @Getter
        @Setter
        private val lore = Arrays.asList("", "Return back.")
    }
}