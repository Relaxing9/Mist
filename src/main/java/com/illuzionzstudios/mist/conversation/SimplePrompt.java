package com.illuzionzstudios.mist.conversation;

import com.illuzionzstudios.mist.ui.UserInterface;
import com.illuzionzstudios.mist.util.TextUtil;
import com.illuzionzstudios.mist.util.Valid;
import lombok.SneakyThrows;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents one question for the player during a server conversation
 */
public abstract class SimplePrompt extends ValidatingPrompt implements Cloneable {

    /**
     * Open the players menu back if any?
     */
    private boolean openMenu = true;

    /**
     * The player who sees the input
     */
    private Player player = null;

    protected SimplePrompt() {
    }

    /**
     * Create a new prompt, show we open players menu back if he has any?
     */
    protected SimplePrompt(final boolean openMenu) {
        this.openMenu = openMenu;
    }

    /**
     * Show the given prompt to the player
     */
    public static void show(final Player player, final SimplePrompt prompt) {
        prompt.show(player);
    }

    /**
     * Return the prefix before tell messages
     */
    protected String getCustomPrefix() {
        return null;
    }

    /**
     * Return the question, implemented in own way using colors
     */
    @NotNull
    @Override
    public final String getPromptText(@NotNull final ConversationContext ctx) {
        return TextUtil.formatText(getPrompt(ctx));
    }

    /**
     * Return the question to the user in this prompt
     */
    protected abstract String getPrompt(ConversationContext ctx);

    /**
     * Checks if the input from the user was valid, if it was, we can continue to the next prompt
     */
    @Override
    protected boolean isInputValid(@NotNull final ConversationContext context, @NotNull final String input) {
        return true;
    }

    /**
     * Return the failed error message when {@link #isInputValid(ConversationContext, String)} returns false
     */
    @Override
    protected String getFailedValidationText(@NotNull final ConversationContext context, @NotNull final String invalidInput) {
        return null;
    }

    /**
     * Converts the {@link ConversationContext} into a {@link Player}
     * or throws an error if it is not a player
     */
    protected Player getPlayer(final ConversationContext ctx) {
        Valid.checkBoolean(ctx.getForWhom() instanceof Player, "Conversable is not a player but: " + ctx.getForWhom());

        return (Player) ctx.getForWhom();
    }

    /**
     * Send the player (in case any) the given message
     */
    protected void tell(final String message) {
        Valid.checkNotNull(player, "Cannot use tell() when player not yet set!");

        tell(player, message);
    }

    /**
     * Send the player (in case any) the given message
     */
    protected void tell(final ConversationContext ctx, final String message) {
        tell(getPlayer(ctx), message);
    }

    /**
     * Sends the message to the player
     */
    protected void tell(final Conversable conversable, final String message) {
        Mist.tellConversing(conversable, (getCustomPrefix() != null ? getCustomPrefix() : "") + message);
    }

    /**
     * Sends the message to the player later
     */
    protected void tellLater(final Conversable conversable, final String message, int delayTicks) {
        Mist.tellLaterConversing(conversable, (getCustomPrefix() != null ? getCustomPrefix() : "") + message, delayTicks);
    }

    /**
     * Called when the whole conversation is over. This is called before {@link SimpleConversation#onConversationEnd(ConversationAbandonedEvent)}
     */
    public void onConversationEnd(final SimpleConversation conversation, final ConversationAbandonedEvent event) {
    }

    // Do not allow superclasses to modify this since we have isInputValid here
    @Override
    public final Prompt acceptInput(@NotNull final ConversationContext context, final String input) {
        if (isInputValid(context, input))
            return acceptValidatedInput(context, input);

        else {
            final String failPrompt = getFailedValidationText(context, input);

            if (failPrompt != null)
                tellLater(context.getForWhom(), "&c" + failPrompt, 1);

            // Redisplay this prompt to the user to re-collect input
            return this;
        }
    }

    /**
     * Shows this prompt as a conversation to the player
     * <p>
     * NB: Do not call this as a means to showing this prompt DURING AN EXISTING
     * conversation as it will fail! Use {@link #acceptValidatedInput(ConversationContext, String)} instead
     * to show the next prompt
     */
    public final SimpleConversation show(final Player player) {
        Valid.checkBoolean(!player.isConversing(), "Player " + player.getName() + " is already conversing! Show them their next prompt in acceptValidatedInput() in " + getClass().getSimpleName() + " instead!");

        this.player = player;

        final SimpleConversation conversation = new SimpleConversation() {

            @Override
            protected Prompt getFirstPrompt() {
                return SimplePrompt.this;
            }

            @Override
            protected ConversationPrefix getPrefix() {
                final String prefix = SimplePrompt.this.getCustomPrefix();

                return prefix != null ? new SimplePrefix(prefix) : super.getPrefix();
            }
        };

        if (openMenu) {
            final UserInterface menu = UserInterface.getInterface(player);

            if (menu != null)
                conversation.setMenuToReturnTo(menu);
        }

        conversation.start(player);

        return conversation;
    }

    @SneakyThrows
    @Override
    public SimplePrompt clone() {
        return (SimplePrompt) super.clone();
    }
}

