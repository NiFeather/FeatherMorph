package xiamomc.morph;

import org.bukkit.entity.Player;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.RequestStrings;
import xiamomc.morph.misc.RequestInfo;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.List;

public class RequestManager extends MorphPluginObject implements IManageRequests
{
    //region Implementation of IManageRequests

    @Resolved
    private IManagePlayerData data;

    @Initializer
    private void load()
    {
        this.addSchedule(c -> update());
    }

    private void update()
    {
        //更新请求
        var requests = new ArrayList<>(this.requests);
        for (var r : requests)
        {
            r.ticksRemain -= 1;
            if (r.ticksRemain <= 0) this.requests.remove(r);
        }

        this.addSchedule(c -> update());
    }

    private final List<RequestInfo> requests = new ArrayList<>();

    @Override
    public void createRequest(Player source, Player target)
    {
        if (requests.stream()
                .anyMatch(i -> i.sourcePlayer.getUniqueId() == source.getUniqueId()
                        && i.targetPlayer.getUniqueId() == target.getUniqueId()))
        {
            source.sendMessage(MessageUtils.prefixes(source, RequestStrings.requestAlreadySentString()));
            return;
        }

        var req = new RequestInfo();
        req.sourcePlayer = source;
        req.targetPlayer = target;
        req.ticksRemain = 1200;

        requests.add(req);

        target.sendMessage(MessageUtils.prefixes(target, RequestStrings.requestReceivedString()
                .resolve("who", source.getName())));

        source.sendMessage(MessageUtils.prefixes(source, RequestStrings.requestSendString()));
    }

    /**
     * 接受请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
    @Override
    public void acceptRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, RequestStrings.requestNotFound()));
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        data.grantPlayerMorphToPlayer(target, source.getName());
        data.grantPlayerMorphToPlayer(source, target.getName());

        target.sendMessage(MessageUtils.prefixes(target, RequestStrings.targetAcceptedString().resolve("who", source.getName())));
        source.sendMessage(MessageUtils.prefixes(source, RequestStrings.sourceAcceptedString().resolve("who", target.getName())));
    }

    /**
     * 拒绝请求
     * @param source 请求接受方
     * @param target 请求发起方
     */
    @Override
    public void denyRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, RequestStrings.requestNotFound()));

            //"未找到目标请求，可能已经过期？"
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        target.sendMessage(MessageUtils.prefixes(target, RequestStrings.targetDeniedString().resolve("who", source.getName())));
        source.sendMessage(MessageUtils.prefixes(source, RequestStrings.sourceAcceptedString().resolve("who", target.getName())));
    }

    @Override
    public List<RequestInfo> getAvaliableRequestFor(Player player)
    {
        return requests.stream()
                .filter(t -> t.targetPlayer.getUniqueId().equals(player.getUniqueId()))
                .toList();
    }

    //endregion Implementation of IManageRequests
}
