package xiamomc.morph.misc;

import org.bukkit.entity.Player;

public class RequestInfo
{
    /**
     * 请求发起方
     */
    public Player sourcePlayer;

    /**
     * 请求接受方
     */
    public Player targetPlayer;

    /**
     * 此请求还要多久过期
     */
    public int ticksRemain;

    /**
     * 此申请的ID
     */
    public int requestID;
}
