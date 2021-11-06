package com.illuzionzstudios.mist.command.type;

import com.illuzionzstudios.mist.command.SpigotSubCommand;
import com.illuzionzstudios.mist.command.response.ReturnType;
import com.illuzionzstudios.mist.config.locale.PluginLocale;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * Skeleton command that you can add to your {@link com.illuzionzstudios.mist.command.SpigotCommandGroup}
 * Simply reloads the plugin with all files etc, implemented in {@link SpigotPlugin#onPluginReload()}.
 * Invokes {@link SpigotPlugin#reload()}. Should only be implemented in whole plugin main command not
 * per main command group
 * <p>
 * {@permission {plugin.name}.command.reload}
 */
public class ReloadCommand extends SpigotSubCommand {

    public ReloadCommand() {
        super("reload", "rl");

        setDescription("Reload the plugin configurations");
    }

    @Override
    protected ReturnType onCommand() {
        // Just call this method to reload
        SpigotPlugin.getInstance().reload();

        // Inform
        PluginLocale.GENERAL_PLUGIN_PREFIX.concat(" " + PluginLocale.GENERAL_PLUGIN_RELOAD).sendMessage(getSender());
        return ReturnType.SUCCESS;
    }
}
