package xiamomc.morph.misc;

import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

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
