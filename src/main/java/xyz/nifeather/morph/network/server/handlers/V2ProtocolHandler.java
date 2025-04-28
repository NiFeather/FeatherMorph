package xyz.nifeather.morph.network.server.handlers;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.server.MessageChannel;
import xyz.nifeather.morph.network.server.handlers.results.CommandHandleResult;
import xyz.nifeather.morph.network.server.handlers.results.VersionHandleResult;
import xyz.nifeather.morph.network.server.respond.ClientInitializeRecord;
import xyz.nifeather.morph.network.server.respond.InitializeRespond;

import java.util.Arrays;
import java.util.List;

/**
 * 应当用在 "commands_v2", "version_v2" 频道，应当在 "init" 频道上优先使用
 */
@SuppressWarnings("deprecation")
public class V2ProtocolHandler extends AbstractCommandPacketHandler
{
    public static final V2ProtocolHandler V2_INSTANCE = new V2ProtocolHandler();

    @Override
    public @NotNull ClientInitializeRecord handleInitializeData(Player player, byte @NotNull [] rawData)
    {
        List<String> content;

        try
        {
            var stringContent = readStringFromByteInput(rawData);
            if (stringContent.isBlank())
            {
                if (FeatherMorphMain.getInstance().debugOutputEnabled())
                    logger.info("Input string is blank, assuming the player is using V1...");

                return ClientInitializeRecord.fail();
            }

            var contentList = Arrays.stream(stringContent.split(" ")).toList();
            content = new ObjectArrayList<>(contentList);
        }
        catch (Throwable t)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.info("'%s' is possibly using a legacy client.".formatted(player.getName()));

            if (FeatherMorphMain.getInstance().debugOutputEnabled())
            {
                logger.info("Unable to decode packet. Is '%s' using a legacy client? %s".formatted(player.getName(), t.getMessage()));
                t.printStackTrace();
            }

            return ClientInitializeRecord.fail();
        }

        return new ClientInitializeRecord(content, 0, true);
    }

    public void sendV2InitalizeRespond(Player player, List<String> featureFlags)
    {
        StringBuilder featureFlagsMessageBuilder = new StringBuilder();

        var it = featureFlags.iterator();

        while (it.hasNext())
        {
            var next = it.next();
            featureFlagsMessageBuilder.append(next);

            if (it.hasNext())
                featureFlagsMessageBuilder.append(" ");
        }

        sendString(player, MessageChannel.initializeChannelV1, featureFlagsMessageBuilder.toString());
    }

    @Override
    public void sendInitializeRespond(Player player, InitializeRespond respond)
    {
        sendInt(player, MessageChannel.versionChannelV2, respond.apiVersion());
    }

    @Override
    @NotNull
    public VersionHandleResult handleVersionData(@NotNull Player player, byte @NotNull [] data)
    {
        try
        {
            var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
            var clientVersion = buf.readInt();

            return VersionHandleResult.from(clientVersion);
        }
        catch (Throwable t)
        {
            logger.warn("Failed to handle version input from '%s', they might using legacy client implementation: %s".formatted(player.getName(), t.getMessage()));
            return VersionHandleResult.fail();
        }
    }

    @Override
    @NotNull
    public CommandHandleResult handleCommandData(@NotNull Player player, byte @NotNull [] data)
    {
        try
        {
            return CommandHandleResult.from(this.readStringFromByteInput(data));
        }
        catch (Throwable t)
        {
            logger.error("Failed to handle command from player '%s': %s".formatted(player.getName(), t.getMessage()));
            return CommandHandleResult.fail();
        }
    }

    @Override
    public void sendCommand(Player player, String data)
    {
        sendString(player, MessageChannel.commandChannelV2, data);
    }

    public void sendString(Player player, String channel, String message)
    {
        var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeUtf(message);

        sendPacketRaw(channel, player, buffer);
    }

    protected String readStringFromByteInput(byte[] rawData) throws Throwable
    {
        String input = null;

        try
        {
            var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(rawData));

            input = buf.readUtf();

            if (buf.readableBytes() > 0)
                throw new RuntimeException("Malformed buffer: still has %s readable bytes!".formatted(buf.readableBytes()));
        }
        catch (Throwable t)
        {
            logger.info("Failed to decode byte: %s".formatted(t.getMessage()));
            throw t;
        }

        return input;
    }
}
