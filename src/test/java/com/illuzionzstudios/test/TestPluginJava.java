package com.illuzionzstudios.test;

import com.illuzionzstudios.mist.config.PluginSettings;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;
import org.jetbrains.annotations.NotNull;

public class TestPluginJava extends SpigotPlugin {
    @Override
    public void onPluginLoad() {
        getMainCommand();
    }

    @Override
    public void onPluginPreEnable() {
    }

    @Override
    public void onPluginEnable() {

    }

    @Override
    public void onPluginDisable() {

    }

    @Override
    public void onPluginPreReload() {
    }

    @Override
    public void onPluginReload() {

    }

    @Override
    public void onRegisterReloadables() {

    }

    @NotNull
    @Override
    public PluginSettings getPluginSettings() {
        return null;
    }

    @NotNull
    @Override
    public PluginLocale getPluginLocale() {
        return null;
    }

    @Override
    public int getPluginId() {
        return 0;
    }
}
