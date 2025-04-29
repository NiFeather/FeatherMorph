package xyz.nifeather.morph.network.server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.commands.C2S.C2SCommandRecord;
import xyz.nifeather.morph.network.commands.C2S.ClientInitializeRecordV3;
import xyz.nifeather.morph.network.commands.S2C.InitializeRespondV3;
import xyz.nifeather.morph.network.commands.S2C.S2CCommandRecord;
import xyz.nifeather.morph.network.server.MessageChannel;
import xyz.nifeather.morph.network.server.handlers.results.CommandHandleResult;
import xyz.nifeather.morph.network.server.handlers.results.VersionHandleResult;

import java.util.Arrays;
import java.util.List;

public class V3ProtocolHandler extends AbstractCommandPacketHandler
{
    public static final V3ProtocolHandler V3_INSTANCE = new V3ProtocolHandler();

    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    @Override
    public @NotNull ClientInitializeRecordV3 handleInitializeData(Player player, byte @NotNull [] rawData)
    {
        try
        {
            var deSerialized = gson.fromJson(this.readStringFromByteInput(rawData), ClientInitializeRecordV3.class);

            // Returned a new copy.... Is this good?
            return new ClientInitializeRecordV3(deSerialized.clientFeatures(), deSerialized.apiVersion(), true);
        }
        catch (Throwable t)
        {
            logger.error("Failed to decode initialize data from '%s': %s".formatted(player.getName(), t.getMessage()));
        }

        return ClientInitializeRecordV3.fail();
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
            var str = this.readStringFromByteInput(data);
            var rec = gson.fromJson(str, C2SCommandRecord.class);

            return CommandHandleResult.from(new C2SCommandRecord(rec.commandName(), rec.arguments()));
        }
        catch (Throwable t)
        {
            logger.error("Failed to handle command from player '%s': %s".formatted(player.getName(), t.getMessage()));
            return CommandHandleResult.fail();
        }
    }

    @Override
    public void sendInitializeRespond(Player player, InitializeRespondV3 respond)
    {
        sendString(player, MessageChannel.initializeChannelV3, gson.toJson(respond));
    }

    @Override
    public void sendCommand(Player player, S2CCommandRecord commandRecord)
    {
        sendString(player, MessageChannel.commandChannelV3, gson.toJson(commandRecord));
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

    public void sendString(Player player, String channel, String message)
    {
        var buffer = new FriendlyByteBuf(Unpooled.buffer()).writeUtf(message);

        sendPacketRaw(channel, player, buffer);
    }
}
