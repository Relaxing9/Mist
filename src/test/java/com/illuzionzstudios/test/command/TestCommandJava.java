package com.illuzionzstudios.test.command;

import com.illuzionzstudios.mist.command.SpigotCommand;
import com.illuzionzstudios.mist.command.response.ReturnType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestCommandJava extends SpigotCommand {

    protected TestCommandJava(@NotNull String commandLabel, @Nullable String... aliases) {
        super(commandLabel, aliases);
    }

    @NotNull
    @Override
    protected ReturnType onCommand() {
        return ReturnType.SUCCESS;
    }
}
