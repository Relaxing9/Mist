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
 * A loader for a {@link CustomItem} from a YAML config section. Can be overriden
 * if loading a custom implementation however must make call to super loadObject method
 * and make sure is making extra alterations to the already altered object to avoid
 * overriding.
 */
public class CustomItemLoader extends SectionLoader<CustomItem> {

    public CustomItemLoader(ConfigSection section) {
        super(section);
    }

    @Override
    public boolean save() {
        // TOOD: Implement saving
        return true;
    }

    @Override
    public CustomItem loadObject(ConfigSection configSection) {
        // Lets try build the item
        CustomItem.CustomItemBuilder builder = CustomItem.builder();

        builder.identifier(getSectionName());

        String customName = section.getString("item-name");
        if (customName != null)
            builder.customName(new MistString(customName));

        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty())
            builder.lore(MistString.fromStringList(lore));

        builder.amount(configSection.getInt("amount", 1));
        builder.material(XMaterial.matchXMaterial(configSection.getString("material", "AIR")).orElse(XMaterial.AIR));
        builder.customModelData(configSection.getInt("model-data"));

        Map<XEnchantment, Integer> enchants = new HashMap<>();
        for (String toParse : configSection.getStringList("enchants")) {
            String[] tokens = toParse.split(":");
            enchants.put(XEnchantment.matchXEnchantment(tokens[0]).orElseThrow(() -> new PluginException("Enchant " + tokens[0] + " does not exist")), Integer.parseInt(tokens[1]));
        }
        builder.enchants(enchants);
        builder.glowing(configSection.getBoolean("glowing"));
        builder.unbreakable(configSection.getBoolean("unbreakable"));
        builder.hideFlags(configSection.getBoolean("hide-flags"));

        return builder.build();
    }
}
