package xyz.nifeather.morph.network.server;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.network.server.handlers.V1ProtocolHandler;
import xyz.nifeather.morph.network.server.handlers.V2ProtocolHandler;
import xyz.nifeather.morph.network.server.respond.ClientInitializeRecord;

import java.util.List;

@SuppressWarnings("deprecation")
public class LegacyClientHandler extends MorphPluginObject
{
    private final MorphClientHandler clientHandler;

    public LegacyClientHandler(MorphClientHandler clientHandler)
    {
        this.clientHandler = clientHandler;

        var messenger = Bukkit.getMessenger();

        messenger.registerIncomingPluginChannel(plugin, MessageChannel.initializeChannelV1, this::handleInitializeV1V2);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.initializeChannelV1);

        messenger.registerIncomingPluginChannel(plugin, MessageChannel.versionChannelV2, this::handleVersionV2);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.versionChannelV2);

        messenger.registerIncomingPluginChannel(plugin, MessageChannel.versionChannelV1, this::handleVersionV1);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.versionChannelV1);

        messenger.registerIncomingPluginChannel(plugin, MessageChannel.commandChannelV1, this::handleCommandV1);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.commandChannelV1);
    }

    /**
     * V1 和 V2 共用一个 init 频道 :>
     */
    private void handleInitializeV1V2(@NotNull String cN, @NotNull Player player, byte @NotNull [] bytes)
    {
        MorphClientHandler.logPacket(false, player, cN, bytes);

        ((CraftPlayer) player).addChannel(MessageChannel.initializeChannelV1);
        ((CraftPlayer) player).addChannel(MessageChannel.commandChannelV1);
        ((CraftPlayer) player).addChannel(MessageChannel.versionChannelV1);
        ((CraftPlayer) player).addChannel(MessageChannel.versionChannelV2);

        var v2Handle = V2ProtocolHandler.V2_INSTANCE.handleInitializeData(player, bytes);
        if (v2Handle.handleSuccess()) // success!
        {
            clientHandler.setProtocolHandlerFor(player, V2ProtocolHandler.V2_INSTANCE);
            logger.info("%s is using V2 packets".formatted(player.getName()));

            V2ProtocolHandler.V2_INSTANCE.sendV2InitalizeRespond(player, clientHandler.getInitializeRespond().serverFeatures());
            return;
        }

        // Possible V1, just send respond
        clientHandler.setProtocolHandlerFor(player, V1ProtocolHandler.V1_INSTANCE);
        logger.info("%s is using V1 packets".formatted(player.getName()));

        V1ProtocolHandler.V1_INSTANCE.sendV1InitializeRespond(player);
    }

    private void handleCommandV1(@NotNull String cN, @NotNull Player player, byte @NotNull [] data)
    {
        MorphClientHandler.logPacket(false, player, cN, data, true);
        clientHandler.setProtocolHandlerFor(player, V1ProtocolHandler.V1_INSTANCE);
        clientHandler.handleCommandInput(V1ProtocolHandler.V1_INSTANCE, cN, player, data);
    }

    private void handleVersionV1(@NotNull String cN, @NotNull Player player, byte @NotNull [] data)
    {
        MorphClientHandler.logPacket(false, player, cN, data);
        var protocolHandler = V1ProtocolHandler.V1_INSTANCE;
        var handleResult = protocolHandler.handleVersionData(player, data);
        if (!handleResult.success())
        {
            clientHandler.rejectPlayer(player);
            return;
        }

        var client = new ClientInitializeRecord(List.of(), handleResult.result(), true);
        clientHandler.handleHandshakeMessage(protocolHandler, player, client);
    }

    private void handleVersionV2(@NotNull String cN, @NotNull Player player, byte @NotNull [] data)
    {
        MorphClientHandler.logPacket(false, player, cN, data);
        var protocolHandler = V2ProtocolHandler.V2_INSTANCE;
        var handleResult = protocolHandler.handleVersionData(player, data);
        if (!handleResult.success())
        {
            clientHandler.rejectPlayer(player);
            return;
        }

        var client = new ClientInitializeRecord(List.of(), handleResult.result(), true);
        clientHandler.handleHandshakeMessage(protocolHandler, player, client);
    }
}
