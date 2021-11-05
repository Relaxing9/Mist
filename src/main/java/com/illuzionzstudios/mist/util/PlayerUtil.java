package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permissible;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility method for dealing with players
 */
@UtilityClass
public class PlayerUtil {

    /**
     * Stores a list of currently pending title animation tasks to restore the tile to its original one
     */
    private final Map<UUID, BukkitTask> titleRestoreTasks = new ConcurrentHashMap<>();

    /**
     * Return if the given sender has a certain permission
     */
    public boolean hasPerm(final Permissible sender, String permission) {
        Valid.checkNotNull(sender, "cannot call hasPerm for null sender!");

        if (permission == null) {
            new Throwable().printStackTrace();
            return true;
        }

        Valid.checkBoolean(!permission.contains("{plugin_name}") && !permission.contains("{plugin_name_lower}"),
                "Found {plugin_name} variable calling hasPerm(" + sender + ", " + permission + ")." + "This is now disallowed, contact plugin authors to put " + SpigotPlugin.getPluginName().toLowerCase() + " in their permission.");

        return sender.hasPermission(permission);
    }

    // ------------------------------------------------------------------------------------------------------------
    // Inventory
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Sets pretty much every flag the player can have such as
     * flying etc, back to normal
     * <p>
     * Also sets gamemode to survival
     * <p>
     * Typical usage: Minigame plugins - call this before joining the player to an arena
     * <p>
     * Even disables Essentials god mode and removes vanish (most vanish plugins are supported).
     */
    public void normalize(final Player player, final boolean cleanInventory) {
        normalize(player, cleanInventory, true);
    }

    /**
     * Sets pretty much every flag the player can have such as
     * flying etc, back to normal
     * <p>
     * Also sets gamemode to survival
     * <p>
     * Typical usage: Minigame plugins - call this before joining the player to an arena
     * <p>
     * Even disables Essentials god mode.
     *
     * @param removeVanish should we remove vanish from players? most vanish plugins are supported
     */
    public void normalize(final Player player, final boolean cleanInventory, final boolean removeVanish) {
        synchronized (titleRestoreTasks) {

            player.setGameMode(GameMode.SURVIVAL);

            if (cleanInventory) {
                cleanInventoryAndFood(player);

                player.resetMaxHealth();

                try {
                    player.setHealth(20);
                } catch (final Throwable ignored) {
                }

                player.setHealthScaled(false);

                for (final PotionEffect potion : player.getActivePotionEffects())
                    player.removePotionEffect(potion.getType());
            }

            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0F);

            player.resetPlayerTime();
            player.resetPlayerWeather();

            player.setFallDistance(0);

            try {
                player.setGlowing(false);
                player.setSilent(false);
            } catch (final NoSuchMethodError ignored) {
            }

            player.setAllowFlight(false);
            player.setFlying(false);

            player.setFlySpeed(0.2F);
            player.setWalkSpeed(0.2F);

            player.setCanPickupItems(true);

            player.setVelocity(new Vector(0, 0, 0));
            player.eject();

            if (player.isInsideVehicle())
                player.getVehicle().remove();

            try {
                for (final Entity passenger : player.getPassengers())
                    player.removePassenger(passenger);
            } catch (final NoSuchMethodError err) {
                /* old MC */
            }

            if (removeVanish)
                try {
                    if (player.hasMetadata("vanished")) {
                        final Plugin plugin = player.getMetadata("vanished").get(0).getOwningPlugin();

                        player.removeMetadata("vanished", plugin);
                    }

                    for (final Player other : Bukkit.getOnlinePlayers())
                        if (!other.getName().equals(player.getName()) && !other.canSee(player))
                            other.showPlayer(player);

                } catch (final NoSuchMethodError err) {
                    /* old MC */

                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
        }
    }

    /*
     * Cleans players inventory and restores food levels
     */
    private void cleanInventoryAndFood(final Player player) {
        player.getInventory().setArmorContents(null);
        player.getInventory().setContents(new ItemStack[player.getInventory().getContents().length]);
        try {
            player.getInventory().setExtraContents(new ItemStack[player.getInventory().getExtraContents().length]);
        } catch (final NoSuchMethodError err) {
            /* old MC */
        }

        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setExhaustion(0);
        player.setSaturation(10);

        player.setVelocity(new Vector(0, 0, 0));
    }

    /**
     * Returns true if the player has empty both normal and armor inventory
     */
    public boolean hasEmptyInventory(final Player player) {
        final ItemStack[] inv = player.getInventory().getContents();
        final ItemStack[] armor = player.getInventory().getArmorContents();

        final ItemStack[] everything = ArrayUtils.addAll(inv, armor);

        for (final ItemStack i : everything)
            if (i != null && i.getType() != Material.AIR)
                return false;

        return true;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Vanish
    // ------------------------------------------------------------------------------------------------------------

    /**
     * Return if the player is vanished, see {@link #isVanished(Player)} or if the other player can see him
     */
    public boolean isVanished(final Player player, final Player otherPlayer) {
        if (otherPlayer != null && !otherPlayer.canSee(player))
            return true;

        return isVanished(player);
    }

    /**
     * Return true if the player is vanished. We check for Essentials and CMI vanish and also "vanished"
     * metadata value which is supported by most plugins
     */
    public boolean isVanished(final Player player) {
        final List<MetadataValue> list = player.getMetadata("vanished");

        for (final MetadataValue meta : list)
            if (meta.asBoolean())
                return true;

        return false;
    }

}
