package xyz.nifeather.morph.network.server.handlers;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.fmccl.converter.C2SCommandConverter;
import xyz.nifeather.fmccl.converter.S2CCommandConverter;
import xyz.nifeather.fmccl.processor.C2SCommandProcessor;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.network.commands.C2S.C2SCommandRecord;
import xyz.nifeather.morph.network.commands.C2S.ClientInitializeRecordV3;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.commands.S2C.InitializeRespondV3;
import xyz.nifeather.morph.network.server.MessageChannel;
import xyz.nifeather.morph.network.server.handlers.results.CommandHandleResult;
import xyz.nifeather.morph.network.server.handlers.results.VersionHandleResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 应当用在 "command", "version" 频道，应当在 {@link V2ProtocolHandler} 之后被用在 "init" 频道
 */
@SuppressWarnings("deprecation")
public class V1ProtocolHandler extends AbstractCommandPacketHandler
{
    public static final V1ProtocolHandler V1_INSTANCE = new V1ProtocolHandler();

    private final C2SCommandProcessor commandProcessor = new C2SCommandProcessor();
    private final S2CCommandConverter s2cConverter = new MorphLegacyCommandConverter();
    private final C2SCommandConverter c2sConverter = new C2SCommandConverter();

    @Override
    public @NotNull ClientInitializeRecordV3 handleInitializeData(Player player, byte @NotNull [] rawData)
    {
        return new ClientInitializeRecordV3(List.of(), 0, true);
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
        try
        {
            var str = new String(rawData, StandardCharsets.UTF_8);
            var command = commandProcessor.processLegacyCommandLine(str);
            var convert = c2sConverter.fromNetheriteCommand(command);

            return CommandHandleResult.from(C2SCommandRecord.fromC2SCommand(convert));
        }
        catch (Throwable t)
        {
            logger.error("Failed to handle command from player '%s': %s".formatted(player.getName(), t.getMessage()));
            return CommandHandleResult.fail();
        }
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
    public void sendInitializeRespond(Player player, InitializeRespondV3 respond)
    {
        sendInt(player, MessageChannel.versionChannelV1, respond.apiVersion());
    }

    public void sendString(Player player, String channel, String message)
    {
        var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeBytes(message.getBytes(StandardCharsets.UTF_8));
        sendPacketRaw(channel, player, buffer);
    }

    @Override
    public void sendCommand(Player player, AbstractS2CCommand<?> command)
    {
        String commandString;

        try
        {
            commandString = s2cConverter.toNetheriteCommand(command).buildCommand();
        }
        catch (Throwable t)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("Can't convert from modern to legacy: " + t.getMessage());

            return;
        }

        sendString(player, MessageChannel.commandChannelV1, commandString);
    }
}
