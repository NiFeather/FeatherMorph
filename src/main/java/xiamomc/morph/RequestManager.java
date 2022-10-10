package xiamomc.morph;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.misc.MessageUtils;
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
            source.sendMessage(MessageUtils.prefixes(source, Component.text("你已经向" + target + "发送过一个请求了")));
            return;
        }

        var req = new RequestInfo();
        req.sourcePlayer = source;
        req.targetPlayer = target;
        req.ticksRemain = 1200;

        requests.add(req);

        var msg = Component.translatable("你收到了来自")
                .append(Component.text(source.getName()))
                .append(Component.translatable("的交换请求！"));

        target.sendMessage(MessageUtils.prefixes(target, msg));

        source.sendMessage(MessageUtils.prefixes(source, Component.translatable("请求已发送！对方将有1分钟的时间来接受")));
    }

    @Override
    public void acceptRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, Component.text("未找到目标请求，可能已经过期？")));
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        data.grantPlayerMorphToPlayer(target, source.getName());
        data.grantPlayerMorphToPlayer(source, target.getName());

        target.sendMessage(MessageUtils.prefixes(target, Component.text("成功与" + source.getName() + "交换！")));
        source.sendMessage(MessageUtils.prefixes(source, Component.text("成功与" + target.getName() + "交换！")));
    }

    @Override
    public void denyRequest(Player source, Player target)
    {
        var match = requests.stream()
                .filter(i -> i.sourcePlayer.getUniqueId().equals(target.getUniqueId())
                        && i.targetPlayer.getUniqueId().equals(source.getUniqueId())).findFirst();

        if (match.isEmpty())
        {
            source.sendMessage(MessageUtils.prefixes(source, Component.text("未找到目标请求，可能已经过期？")));

            //"未找到目标请求，可能已经过期？"
            return;
        }

        var req = match.get();
        req.ticksRemain = -1;

        var msg = Component.text("请求已拒绝");

        target.sendMessage(MessageUtils.prefixes(target, Component.text("发往" + source.getName() + "的").append(msg)));
        source.sendMessage(MessageUtils.prefixes(source, Component.text("来自" + target.getName() + "的").append(msg)));
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
