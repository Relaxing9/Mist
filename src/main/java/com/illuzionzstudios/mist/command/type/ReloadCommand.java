package com.illuzionzstudios.mist.command.type;

import com.illuzionzstudios.mist.command.SpigotSubCommand;
import com.illuzionzstudios.mist.command.response.ReturnType;
import com.illuzionzstudios.mist.config.locale.Locale;
import com.illuzionzstudios.mist.config.locale.Message;
import com.illuzionzstudios.mist.plugin.SpigotPlugin;

/**
 * Skeleton command that you can add to your {@link com.illuzionzstudios.mist.command.SpigotCommandGroup}
 * Simply reloads the plugin with all files etc, implemented in {@link SpigotPlugin#onPluginReload()}.
 * Invokes {@link SpigotPlugin#reload()}
 *
 * {@permission {plugin.name}.command.reload}
 */
public class ReloadCommand extends SpigotSubCommand {

    public ReloadCommand() {
        super("reload", "rl");

        setDescription("Reload the plugin configurations");
    }

    @Override
    protected ReturnType onCommand() {
        SpigotPlugin plugin = SpigotPlugin.getInstance();

        // Just call this method to reload
        plugin.reload();

        // Inform
        new Message(Locale.General.PLUGIN_RELOAD).sendPrefixedMessage(getSender());
        return ReturnType.SUCCESS;
    }
}
