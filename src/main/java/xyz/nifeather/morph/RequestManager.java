package xyz.nifeather.morph;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.storage.IPlayerDataBackend;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.RequestStrings;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.RequestInfo;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.commands.S2C.S2CUpdateRequestStatusCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;

import java.util.Collections;
import java.util.List;

public class RequestManager extends MorphPluginObject implements IManageRequests
{
    //region Implementation of IManageRequests

    @Resolved
    private MorphManager morphManager;

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        if (this.requests.isEmpty())
            return;

        //更新请求
        var requests = new ObjectArrayList<>(this.requests);
        for (var r : requests)
        {
            r.ticksRemain -= 1;
            if (r.ticksRemain <= 0)
            {
                var owner = r.sourcePlayer;

                if (r.ticksRemain > -255)
                {
                    clientHandler.sendCommand(owner, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.RequestExpiredOwner, r.targetPlayer.getName()));
                    clientHandler.sendCommand(r.targetPlayer, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.RequestExpired, owner.getName()));
                }

                this.requests.remove(r);
            }
        }
    }

    private final List<RequestInfo> requests = Collections.synchronizedList(new ObjectArrayList<>());

    @Override
    public void createRequest(Player source, Player target)
    {
        if (!source.hasPermission(CommonPermissions.SEND_REQUEST))
        {
            MessageUtils.send(source, CommandStrings.noPermissionMessage());
            return;
        }

        if (requests.stream()
                .anyMatch(i -> i.sourcePlayer.getUniqueId().equals(source.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(target.getUniqueId())))
        {
            MessageUtils.send(source, RequestStrings.requestAlreadySentString()
                    .resolve("who", target.getName()));
            return;
        }

        var req = new RequestInfo();
        req.sourcePlayer = source;
        req.targetPlayer = target;
        req.ticksRemain = 1200;

        requests.add(req);

        MessageUtils.send(target, RequestStrings.requestReceivedString()
                .resolve("who", source.getName()));

        MessageUtils.send(target, RequestStrings.requestReceivedAcceptString()
                .resolve("who", source.getName()));

        MessageUtils.send(target, RequestStrings.requestReceivedDenyString()
                .resolve("who", source.getName()));

        MessageUtils.send(source, RequestStrings.requestSendString()
                .resolve("who", target.getName()));

        clientHandler.sendCommand(target, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.NewRequest, source.getName()));
        clientHandler.sendCommand(source, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.RequestSend, target.getName()));
    }

    @Resolved
    private MorphClientHandler clientHandler;

    /**
     * 接受请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
    @Override
    public void acceptRequest(Player source, Player target)
    {
        if (!source.hasPermission(CommonPermissions.ACCEPT_REQUEST))
        {
            MessageUtils.send(source, CommandStrings.noPermissionMessage());
            return;
        }

        var req = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst().orElse(null);

        if (req == null)
        {
            MessageUtils.send(source, RequestStrings.requestNotFound());
            return;
        }

        req.ticksRemain = -256;

        morphManager.grantMorphToPlayer(target, DisguiseTypes.PLAYER.toId(source.getName()));
        morphManager.grantMorphToPlayer(source, DisguiseTypes.PLAYER.toId(target.getName()));

        MessageUtils.send(target, RequestStrings.targetAcceptedString().resolve("who", source.getName()));
        MessageUtils.send(source, RequestStrings.sourceAcceptedString().resolve("who", target.getName()));

        clientHandler.sendCommand(target, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.RequestAccepted, source.getName()));
        //clientHandler.sendCommand(source, new S2CRequestCommand(S2CRequestCommand.Type.RequestAccepted, target.getName()));
    }

    /**
     * 拒绝请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
    @Override
    public void denyRequest(Player source, Player target)
    {
        if (!source.hasPermission(CommonPermissions.DENY_REQUEST))
        {
            MessageUtils.send(source, CommandStrings.noPermissionMessage());
            return;
        }

        var req = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst().orElse(null);

        if (req == null)
        {
            MessageUtils.send(source, RequestStrings.requestNotFound());

            //"未找到目标请求，可能已经过期？"
            return;
        }

        req.ticksRemain = -256;

        MessageUtils.send(target, RequestStrings.targetDeniedString().resolve("who", source.getName()));
        MessageUtils.send(source, RequestStrings.sourceDeniedString().resolve("who", target.getName()));

        clientHandler.sendCommand(target, new S2CUpdateRequestStatusCommand(S2CUpdateRequestStatusCommand.Type.RequestDenied, source.getName()));
        //clientHandler.sendCommand(source, new S2CRequestCommand(S2CRequestCommand.Type.RequestDenied, target.getName()));
    }

    @Override
    public List<RequestInfo> getAvailableRequestsFor(Player player)
    {
        return requests.stream()
                .filter(t -> t.targetPlayer.getUniqueId().equals(player.getUniqueId()))
                .toList();
    }

    //endregion Implementation of IManageRequests
}
