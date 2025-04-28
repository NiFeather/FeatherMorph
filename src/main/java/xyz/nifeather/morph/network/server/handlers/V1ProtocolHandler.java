package xyz.nifeather.morph.network.server.handlers;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.server.MessageChannel;
import xyz.nifeather.morph.network.server.handlers.results.CommandHandleResult;
import xyz.nifeather.morph.network.server.handlers.results.VersionHandleResult;
import xyz.nifeather.morph.network.server.respond.ClientInitializeRecord;
import xyz.nifeather.morph.network.server.respond.InitializeRespond;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 应当用在 "command", "version" 频道，应当在 {@link V2ProtocolHandler} 之后被用在 "init" 频道
 */
@SuppressWarnings("deprecation")
public class V1ProtocolHandler extends AbstractCommandPacketHandler
{
    public static final V1ProtocolHandler V1_INSTANCE = new V1ProtocolHandler();

    @Override
    public @NotNull ClientInitializeRecord handleInitializeData(Player player, byte @NotNull [] rawData)
    {
        return new ClientInitializeRecord(List.of(), 0, true);
    }

    @Override
    @NotNull
    public VersionHandleResult handleVersionData(Player player, byte @NotNull [] rawData)
    {
        try
        {
            var clientVersionStr = new String(rawData, StandardCharsets.UTF_8);

            return VersionHandleResult.from(Integer.parseInt(clientVersionStr));
        }
        catch (Throwable t)
        {
            logger.error("Failed to decode client version from legacy buffer: " + t.getMessage());
            return VersionHandleResult.fail();
        }
    }

    @Override
    @NotNull
    public CommandHandleResult handleCommandData(Player player, byte @NotNull [] rawData)
    {
        return CommandHandleResult.from(new String(rawData, StandardCharsets.UTF_8));
    }

    public void sendVersionRespond(Player player, int implementingApi)
    {
        sendInt(player,  MessageChannel.versionChannelV1, implementingApi);
    }

    public void sendV1InitializeRespond(Player player)
    {
        sendString(player, MessageChannel.initializeChannelV1, "");
    }

    @Override
    @Deprecated
    public void sendInitializeRespond(Player player, InitializeRespond respond)
    {
        sendInt(player, MessageChannel.versionChannelV1, respond.apiVersion());
    }

    public void sendString(Player player, String channel, String message)
    {
        var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(message.getBytes(StandardCharsets.UTF_8));
        sendPacketRaw(channel, player, buffer);
    }

    @Override
    public void sendCommand(Player player, String data)
    {
        sendString(player, MessageChannel.commandChannelV1, data);
    }
}
