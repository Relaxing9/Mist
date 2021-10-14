package com.illuzionzstudios.mist.util;

import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class to deal with {@link org.bukkit.inventory.ItemStack}.
 * This could be parsing the data or dealing with NBT data on the item
 */
public final class ItemStackUtil {

    public static final String LORE_FIX_PREFIX = "fogus_loren-";
    public static final String NAME_FIX_PREFIX = "fogus_namel-";
    public static final String TAG_SPLITTER = "__x__";
    private static final Map<String, NamespacedKey> LORE_KEYS_CACHE;
    private static final Map<String, NamespacedKey> NAME_KEYS_CACHE;

    static {
        LORE_KEYS_CACHE = new HashMap<>();
        NAME_KEYS_CACHE = new HashMap<>();
    }

    /**
     * Serialize a {@link ItemStack} into a string blob
     *
     * @param itemStack ItemStack to serialize
     * @return ItemStack as a string blob
     */
    public static String serialize(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return config.saveToString();
    }

    /**
     * Deserialize a string blob into a {@link ItemStack}
     * See {@link #serialize(ItemStack)}
     *
     * @param stringBlob String blob to convert to {@link ItemStack}
     * @return Formed {@link ItemStack}. {@code null} if didn't deserialize properly
     */
    public static ItemStack deserialize(String stringBlob) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(stringBlob);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }

    public static void clear() {
        LORE_KEYS_CACHE.clear();
        NAME_KEYS_CACHE.clear();
    }

    public static int addToLore(@NotNull List<String> lore, int pos, @NotNull String value) {
        if (pos >= lore.size() || pos < 0) {
            lore.add(value);
        } else {
            lore.add(pos, value);
        }
        return pos + 1;
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull String text, int pos) {
        String[] lines = text.split(TAG_SPLITTER);
        addLore(item, id, Arrays.asList(lines), pos);
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull List<String> text, int pos) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        text = TextUtil.formatText(text);
        StringBuilder loreTag = new StringBuilder();

        delLore(item, id);
        for (String line : text) {
            pos = addToLore(lore, pos, line);

            if (loreTag.length() > 0) loreTag.append(TAG_SPLITTER);
            loreTag.append(line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        addLoreTag(item, id, loreTag.toString());
    }

    public static void delLore(@NotNull ItemStack item, @NotNull String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        int index = getLoreIndex(item, id, 0);
        if (index < 0) return;

        int lastIndex = getLoreIndex(item, id, 1);
        int diff = lastIndex - index;

        for (int i = 0; i < (diff + 1); i++) {
            lore.remove(index);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        delLoreTag(item, id);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id) {
        return getLoreIndex(item, id, 0);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id, int type) {
        String storedText = DataUtil.getStringData(item, getLoreKey(id));
        if (storedText == null) return -1;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;

        List<String> lore = meta.getLore();
        if (lore == null) return -1;

        String[] lines = storedText.split(TAG_SPLITTER);
        String lastText = null;
        int count = 0;

        if (type == 0) {
            for (int i = 0; i < lines.length; i++) {
                lastText = lines[i];
                if (!ChatColor.stripColor(lastText).isEmpty()) {
                    break;
                }
                count--;
            }
        } else {
            for (int i = lines.length; i > 0; i--) {
                lastText = lines[i - 1];
                if (!ChatColor.stripColor(lastText).isEmpty()) {
                    break;
                }
                count++;
            }
        }

        if (lastText == null) return -1;

        int index = lore.indexOf(lastText) + count;

        // Clean up invalid lore tags.
        if (index < 0) {
            delLoreTag(item, id);
        }
        return index;
    }

    @NotNull
    private static NamespacedKey getLoreKey(@NotNull String id2) {
        String id = id2.toLowerCase();
        return LORE_KEYS_CACHE.computeIfAbsent(id, key -> new NamespacedKey(SpigotPlugin.getInstance(), LORE_FIX_PREFIX + id));
    }

    @NotNull
    private static NamespacedKey getNameKey(@NotNull String id2) {
        String id = id2.toLowerCase();
        return NAME_KEYS_CACHE.computeIfAbsent(id, key -> new NamespacedKey(SpigotPlugin.getInstance(), NAME_FIX_PREFIX + id));
    }

    public static void addLoreTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        DataUtil.setData(item, getLoreKey(id), text);
    }

    public static void delLoreTag(@NotNull ItemStack item, @NotNull String id) {
        DataUtil.removeData(item, getLoreKey(id));
    }

    @Nullable
    public static String getLoreTag(@NotNull ItemStack item, @NotNull String id) {
        return DataUtil.getStringData(item, getLoreKey(id));
    }

    public static void addNameTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        DataUtil.setData(item, getNameKey(id), text);
    }

    public static void delNameTag(@NotNull ItemStack item, @NotNull String id) {
        DataUtil.removeData(item, getNameKey(id));
    }

    @Nullable
    public static String getNameTag(@NotNull ItemStack item, @NotNull String id) {
        return DataUtil.getStringData(item, getNameKey(id));
    }


}
