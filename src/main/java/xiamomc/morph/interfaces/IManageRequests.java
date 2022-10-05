package xiamomc.morph.interfaces;

import org.bukkit.entity.Player;
import xiamomc.morph.misc.RequestInfo;

import java.util.List;

public interface IManageRequests
{
    /**
     * 发起请求
     *
     * @param source 请求发起方
     * @param target 请求接受方
     */
    public void createRequest(Player source, Player target);

    /**
     * 接受请求
     *
     * @param source 请求接受方
     * @param target 请求发起方
     */
    public void acceptRequest(Player source, Player target);

    /**
     * 拒绝请求
     *
     * @param source 请求接受方
     * @param target 请求发起方
     */
    public void denyRequest(Player source, Player target);

    /**
     * 获取目标为player的所有请求
     *
     * @param player 目标玩家
     * @return 请求列表
     */
    public List<RequestInfo> getAvaliableRequestFor(Player player);
}
