package xyz.nifeather.morph.network.server.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.network.server.MorphClientHandler;

public abstract class AbstractCommandPacketHandler extends MorphPluginObject implements ICommandPacketHandler
{
    public AbstractCommandPacketHandler()
    {
    }

    public void sendInt(Player player, String channel, int value)
    {
        var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeInt(value);
        sendPacketRaw(channel, player, buffer);
    }

    protected void sendPacketRaw(String channel, Player player, ByteBuf buffer)
    {
        if (channel == null || player == null || buffer == null)
            throw new IllegalArgumentException("Null channel/player/message");

        try
        {
            byte[] bufferBytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bufferBytes);

            MorphClientHandler.logPacket(true, player, channel, bufferBytes, false);

            if (!player.getListeningPluginChannels().contains(channel))
                throw new NullDependencyException("Channel %s is INVALID for player %s!".formatted(channel, player.getName()));

            player.sendPluginMessage(plugin, channel, bufferBytes);
        }
        catch (Throwable t)
        {
            logger.error("Can't send packet to player", t);
        }
    }
}
