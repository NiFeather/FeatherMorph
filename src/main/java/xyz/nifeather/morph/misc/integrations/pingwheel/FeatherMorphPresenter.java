package xyz.nifeather.morph.misc.integrations.pingwheel;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.pwforked.handling.PingData;
import xyz.nifeather.pwforked.handling.presenter.ModPresenter;

import java.util.Objects;
import java.util.UUID;

public class FeatherMorphPresenter extends ModPresenter
{
    @Override
    public void present(Player receiver, PingData pingData)
    {
        var api = FeatherMorphAPI.instance();
        if (api == null) return;

        UUID overridenTargetEntityUUID = pingData.selectedEntity();
        UUID overridenSourceUUID = pingData.whoPinged();

        // 假定这个 selectedEntity 是某个玩家的变形:
        // 查找与此UUID对应的玩家
        // 如果有，检查玩家是否和 receiver 是一致的
        // 如果一致，则更改发给此玩家的 PingData
        var selectedEntity = pingData.selectedEntity();
        if (selectedEntity != null)
        {
            var player = api.utilitiesAlpha().lookupPlayerFromDisguiseUUID(selectedEntity);
            if (receiver.equals(player))
                overridenTargetEntityUUID = player.getUniqueId();
        }

        // 然后，查看发出的玩家是否有在变形
        // 如果有，并且 receiver 不是 whoPinged ，将 whiPinged 设置为此变形的虚拟 UUID
        Player sender = Bukkit.getPlayer(pingData.whoPinged());
        if (sender != null && !receiver.equals(sender))
        {
            var disguiseUUID = api.utilitiesAlpha().lookupDisguiseUUIDFromPlayer(sender);
            if (disguiseUUID != null)
                overridenSourceUUID = disguiseUUID;
        }

        if (!Objects.equals(overridenTargetEntityUUID, pingData.selectedEntity())
                || !overridenSourceUUID.equals(pingData.whoPinged()))
        {
            pingData = new PingData(
                    overridenSourceUUID,
                    pingData.pingChannel(),
                    pingData.pingingLocation(), overridenTargetEntityUUID, pingData.desiredSequence()
            );
        }

        super.present(receiver, pingData);
    }
}
