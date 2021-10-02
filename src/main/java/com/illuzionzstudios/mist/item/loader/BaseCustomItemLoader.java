package com.illuzionzstudios.mist.item.loader;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.illuzionzstudios.mist.config.ConfigSection;
import com.illuzionzstudios.mist.config.locale.MistString;
import com.illuzionzstudios.mist.config.serialization.loader.SectionLoader;
import com.illuzionzstudios.mist.exception.PluginException;
import com.illuzionzstudios.mist.item.CustomItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A loader for a {@link CustomItem} from a YAML config section. Can be implemented
 * with another custom item by overriding {@link #returnImplementedObject(ConfigSection)} and creating
 * new instance of custom item with loading already done for that item.
 */
public abstract class BaseCustomItemLoader<T extends CustomItem> extends SectionLoader<T> {

    public BaseCustomItemLoader(ConfigSection section) {
        super(section);
    }

    @Override
    public boolean save() {
        // TODO: Implement saving
        return true;
    }

    @Override
    public T loadObject(ConfigSection configSection) {
        // Lets try build the item
        T item = returnImplementedObject(configSection);

        // Set base options

        item.setIdentifier(getSectionName());

        String customName = section.getString("item-name");
        if (customName != null)
            item.setCustomName(new MistString(customName));

        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty())
            item.setLore(MistString.fromStringList(lore));

        item.setAmount(configSection.getInt("amount", 1));
        item.setMaterial(XMaterial.matchXMaterial(configSection.getString("material", "AIR")).orElse(XMaterial.AIR));
        item.setCustomModelData(configSection.getInt("model-data"));

        Map<XEnchantment, Integer> enchants = new HashMap<>();
        for (String toParse : configSection.getStringList("enchants")) {
            String[] tokens = toParse.split(":");
            enchants.put(XEnchantment.matchXEnchantment(tokens[0]).orElseThrow(() -> new PluginException("Enchant " + tokens[0] + " does not exist")), Integer.parseInt(tokens[1]));
        }
        item.setEnchants(enchants);
        item.setGlowing(configSection.getBoolean("glowing"));
        item.setUnbreakable(configSection.getBoolean("unbreakable"));
        item.setHideFlags(configSection.getBoolean("hide-flags"));

        return item;
    }

    /**
     * Can be overridden to return the new custom item with the loading done
     * for that item.
     */
    protected abstract T returnImplementedObject(final ConfigSection configSection);

}
