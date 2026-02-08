package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing;

import com.destroystokyo.paper.ClientOption;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MainHand;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;

import java.util.List;

public class PlayerBindTarget extends LivingEntityBindTarget
{
    private final Player bindingPlayer;

    public PlayerBindTarget(Player player)
    {
        super(player);
        this.bindingPlayer = player;
    }

    @Override
    public boolean isActive()
    {
        return bindingPlayer.isConnected();
    }

    @Override
    public List<Player> viewingPlayers()
    {
        return WatcherUtils.getAffectedPlayers(bindingPlayer);
    }

    @Override
    public byte skinFlags()
    {
        return (byte) bindingPlayer.getClientOption(ClientOption.SKIN_PARTS).getRaw();
    }

    @Override
    public MainHand mainHand()
    {
        return bindingPlayer.getMainHand();
    }
}
