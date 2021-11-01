package com.illuzionzstudios.mist.data.player;

import com.illuzionzstudios.mist.scheduler.Tickable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

/**
 * Player that contains Bukkit API methods
 */
@Getter
public class BukkitPlayer extends AbstractPlayer implements Tickable {

    public BukkitPlayer(UUID uuid, String name) {
        super(uuid, name);
    }

    /**
     * To be overridden
     */
    @Override
    public void tick() {
    }

    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(getUUID());
    }

    /**
     * Send a message to the player translating color code.
     *
     * @param message The target message.
     */
    public void sendRawMessage(String message) {
        this.getBukkitPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Kick the player from the server with target message.
     *
     * @param message Kick Message.
     */
    public void kick(String message) {
        this.getBukkitPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Teleport The the player to a location.
     *
     * @param location {@link Location}
     */
    public void teleport(Location location) {
        this.getBukkitPlayer().teleport(location);
    }

    /**
     * If another entity is in range
     *
     * @param entity       Entity
     * @param rangeSquared The blocks to check
     */
    public boolean isInRange(Entity entity, double rangeSquared) {
        return isInRange(entity.getLocation(), rangeSquared);
    }

    /**
     * If another location is in range
     *
     * @param location     location
     * @param rangeSquared The blocks to check
     */
    public boolean isInRange(Location location, double rangeSquared) {
        if (this.getBukkitPlayer() == null) {
            return false;
        }

        return location.getWorld().equals(this.getBukkitPlayer().getWorld()) && location.distanceSquared(this.getBukkitPlayer().getLocation()) < rangeSquared;
    }

    public Location getLocation() {
        return this.getBukkitPlayer().getLocation();
    }

    public PlayerInventory getInventory() {
        return this.getBukkitPlayer().getInventory();
    }
}
