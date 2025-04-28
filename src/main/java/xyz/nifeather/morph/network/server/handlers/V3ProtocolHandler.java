package xyz.nifeather.morph.network.server.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.server.respond.ClientInitializeRecord;

public class V3ProtocolHandler extends V2ProtocolHandler
{
    public static final V3ProtocolHandler V3_INSTANCE = new V3ProtocolHandler();

    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    @Override
    public @NotNull ClientInitializeRecord handleInitializeData(Player player, byte @NotNull [] rawData)
    {
        try
        {
            var deSerialized = gson.fromJson(this.readStringFromByteInput(rawData), ClientInitializeRecord.class);

            // Returned a new copy.... Is this good?
            return new ClientInitializeRecord(deSerialized.clientFeatures(), deSerialized.apiVersion(), true);
        }
        catch (Throwable t)
        {
            logger.error("Failed to decode initialize data from '%s': %s".formatted(player.getName(), t.getMessage()));
        }

        return ClientInitializeRecord.fail();
    }
}
