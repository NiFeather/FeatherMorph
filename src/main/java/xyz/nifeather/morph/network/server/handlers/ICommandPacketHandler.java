package xyz.nifeather.morph.network.server.handlers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.network.commands.C2S.ClientInitializeRecordV3;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.commands.S2C.InitializeRespondV3;
import xyz.nifeather.morph.network.server.handlers.results.CommandHandleResult;
import xyz.nifeather.morph.network.server.handlers.results.VersionHandleResult;

public interface ICommandPacketHandler
{
    @NotNull
    ClientInitializeRecordV3 handleInitializeData(Player player, byte @NotNull [] rawData);

    @NotNull
    VersionHandleResult handleVersionData(Player player, byte @NotNull [] rawData);

    @NotNull
    CommandHandleResult handleCommandData(Player player, byte @NotNull [] rawData);

    /**
     * @apiNote 只有 V3 支持在 init_v3 频道上一起输出 API版本 和 服务器特性，<b>V2 和 V1 在调用时只会在 version/version_v2 频道上发送API版本</b>
     *          有关 V2, V1 协议的处理，参见 {@link xyz.nifeather.morph.network.server.LegacyClientHandler}
     */
    void sendInitializeRespond(Player player, InitializeRespondV3 respond);
    void sendCommand(Player player, AbstractS2CCommand<?> command);
}
