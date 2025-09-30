package xyz.nifeather.morph.misc.integrations.pingwheel;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.pwforked.PingwheelPluginForked;
import xyz.nifeather.pwforked.network.packets.S2CUpdatePingPacket;

public class PingWheelEventListener extends MorphPluginObject implements Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRegisterChannel(PlayerRegisterChannelEvent e)
    {
        var pingManager = PingwheelPluginForked.instance().pingManager();

        if (e.getChannel().equals(S2CUpdatePingPacket.ID))
            pingManager.setPresenter(e.getPlayer(), new FeatherMorphPresenter());
    }
}
