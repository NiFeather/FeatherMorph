package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

import java.util.List;

/**
 * Listener used to override the metadata packet, so that the client won't panic when it received player's meta but the player is disguised as a mob.
 */
public class MetaPacketListener extends ProtocolListener
{
    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public String getIdentifier()
    {
        return "meta_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        var wrapper = new WrapperPlayServerEntityMetadata(event);
        this.onMetaPacket(wrapper, event);
    }

    private void onMetaPacket(WrapperPlayServerEntityMetadata packet, PacketSendEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerFrom(packet.getEntityId());

        // How could this be?!
        if (sourceNmsEntity == null)
            return;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        if (sourcePlayer.equals(packetEvent.getPlayer())) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        //然后获取此包要发送的目标玩家
        var targetPlayer = packetEvent.getPlayer();

        //只拦截其他人的Meta
        if (targetPlayer == sourcePlayer)
            return;

        var wrapper = new WrapperPlayServerEntityMetadata(packetEvent);

        //取得来源玩家的伪装后的Meta，发送给目标玩家
        //从包里移除玩家meta中不属于BASE_LIVING的部分
        var isPlayerDisguise = watcher.getEntityType() == EntityType.PLAYER;
        this.rebuildServerMetaPacket(
                isPlayerDisguise ? ValueIndex.PLAYER : ValueIndex.BASE_LIVING,
                watcher,
                wrapper);
    }

    /**
     * 重构服务器将要发送的Meta包
     */
    public void rebuildServerMetaPacket(AbstractValues av, SingleWatcher watcher, WrapperPlayServerEntityMetadata packetWrapper)
    {
        var values = av.getValues();

        //获取原Meta包中的数据
        var originalData = packetWrapper.getEntityMetadata();

        // 如果Meta包里有咱的标记，那么移除标记并返回原包
        if (originalData.removeIf(wrapped -> wrapped.getValue().equals(PacketFactory.MARK_DONT_PROCESS)))
        {
            packetWrapper.setEntityMetadata(originalData);
            return;
        }

        List<EntityData<?>> valuesToOverwrite = new ObjectArrayList<>();
        var blockedValues = watcher.getBlockedValues();

        for (EntityData<?> raw : originalData)
        {
            var index = raw.getIndex();

            // 跳过被屏蔽的数据
            if (blockedValues.contains(index))
                continue;

            // 寻找与其匹配的SingleValue
            var singleValue = values.stream()
                    .filter(sv -> sv.index() == index && raw.getType().equals(sv.type()))
                    .findFirst().orElse(null);

            // 如果没有找到，则代表此Index和伪装不兼容，跳过
            if (singleValue == null)
                continue;

            // 如果 Watcher 中有覆盖的有对应的值，则重新包装，否则原样返回
            var val = watcher.readOr(singleValue, null);

            if (val != null)
            {
                var wrapped = new EntityData<>(singleValue.index(), (EntityDataType<? super Object>) singleValue.type(), val);

                valuesToOverwrite.add(wrapped);
            }
            else
            {
                valuesToOverwrite.add(raw);
            }
        }

        packetWrapper.setEntityMetadata(valuesToOverwrite);
    }
}
