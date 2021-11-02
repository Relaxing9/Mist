package com.illuzionzstudios.mist.config.serialization.loader.type;

import com.illuzionzstudios.mist.config.ConfigSection;
import com.illuzionzstudios.mist.config.serialization.loader.ObjectLoader;

public abstract class YamlSectionLoader<T> extends ObjectLoader<T, ConfigSection> {

    public YamlSectionLoader(final ConfigSection load) {
        super(load);
    }
}
