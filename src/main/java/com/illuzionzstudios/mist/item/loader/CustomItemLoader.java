package com.illuzionzstudios.mist.item.loader;

import com.illuzionzstudios.mist.config.ConfigSection;
import com.illuzionzstudios.mist.item.CustomItem;

/**
 * Custom item loader for normal implementation
 */
public class CustomItemLoader extends BaseCustomItemLoader<CustomItem> {

    public CustomItemLoader(ConfigSection section) {
        super(section);
    }

    @Override
    protected CustomItem returnImplementedObject(final ConfigSection configSection) {
        return CustomItem.builder().build();
    }
}
