package xiamomc.morph.misc;

import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.command.ServerCommandSender;
import org.jetbrains.annotations.NotNull;

public final class NilCommandSource extends ServerCommandSender
{
    /**
     * Sends this sender a message
     *
     * @param message Message to be displayed
     * @see #sendMessage(Component)
     * @see #sendPlainMessage(String)
     * @see #sendRichMessage(String)
     */
    @Override
    public void sendMessage(@NotNull String message)
    {

    }

    /**
     * Sends this sender multiple messages
     *
     * @param messages An array of messages to be displayed
     * @see #sendMessage(Component)
     * @see #sendPlainMessage(String)
     * @see #sendRichMessage(String)
     */
    @Override
    public void sendMessage(@NotNull String... messages)
    {

    }

    /**
     * Gets the name of this command sender
     *
     * @return Name of the sender
     */
    @Override
    public @NotNull String getName()
    {
        return "NilCommandSource";
    }

    /**
     * Gets the name of this command sender
     *
     * @return Name of the sender
     */
    @Override
    public @NotNull Component name()
    {
        return Component.text("NilCommandSource");
    }

    /**
     * Checks if this object is a server operator
     *
     * @return true if this is an operator, otherwise false
     */
    @Override
    public boolean isOp()
    {
        return true;
    }

    /**
     * Sets the operator status of this object
     *
     * @param value New operator value
     */
    @Override
    public void setOp(boolean value)
    {

    }
}
