package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;

import java.util.List;

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
    public void onPacketSending(PacketEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA)
            return;

        onMetaPacket((ClientboundSetEntityDataPacket) event.getPacket().getHandle(), event);
    }

    private void onMetaPacket(ClientboundSetEntityDataPacket packet, PacketEvent packetEvent)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerFrom(packet.id());

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

        //不要二次处理来自我们自己的包
        //并且不要处理同为玩家的Meta包
        var packetContainer = packetEvent.getPacket();

        //取得来源玩家的伪装后的Meta，发送给目标玩家
        //从包里移除玩家meta中不属于BASE_LIVING的部分
        var isPlayerDisguise = watcher.getEntityType() == EntityType.PLAYER;
        var finalPacket = this.rebuildServerMetaPacket(
                isPlayerDisguise ? ValueIndex.PLAYER : ValueIndex.BASE_LIVING,
                watcher,
                packetContainer);

        if (finalPacket.getDataValueCollectionModifier().size() == 0)
            packetEvent.setCancelled(true);

        packetEvent.setPacket(finalPacket);
    }

    /**
     * 重构服务器将要发送的Meta包
     * <br>
     * 直接修改Meta包会导致一些玄学问题，例如修改后的值在之后被发给了不该收到的人
     * @return 剔除后的包
     */
    public PacketContainer rebuildServerMetaPacket(AbstractValues av, SingleWatcher watcher, PacketContainer originalPacket)
    {
        if (originalPacket.getType() != PacketType.Play.Server.ENTITY_METADATA)
            throw new IllegalArgumentException("Original packet is not a valid metadata packet!");

        var newPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        newPacket.getIntegers().write(0, originalPacket.getIntegers().read(0));

        var values = av.getValues();

        //获取原Meta包中的数据
        var originalData = originalPacket.getDataValueCollectionModifier().read(0);

        // 如果Meta包里有咱的标记，那么移除标记并返回原包
        if (originalData.removeIf(wrapped -> wrapped.getRawValue().equals(PacketFactory.MARK_DONT_PROCESS)))
        {
            newPacket.getDataValueCollectionModifier().write(0, originalData);
            return newPacket;
        }

        List<WrappedDataValue> valuesToAdd = new ObjectArrayList<>();
        var blockedValues = watcher.getBlockedValues();

        for (WrappedDataValue w : originalData)
        {
            var index = w.getIndex();

            // 跳过被屏蔽的数据
            if (blockedValues.contains(index))
                continue;

            // 寻找与其匹配的SingleValue
            //
            // todo: 自从去NMS化之后，一些同一个Index上的值和他们对应的SingleValue用旧办法来看已经不再匹配了
            //       因此需要寻找别的方法来更全面地证明某个Index和SingleValue匹配
            //       现在我们只能临时删除class验证
            var singleValue = values.stream()
                    .filter(sv -> sv.index() == index)
                    .findFirst().orElse(null);

            // 如果没有找到，则代表此Index和伪装不兼容，跳过
            if (singleValue == null)
                continue;

            // 如果 Watcher 中有覆盖的有对应的值，则重新包装，否则原样返回
            var val = watcher.readOr(singleValue.index(), null);

            if (val != null)
            {
                var wrapped = ((SingleValue<Object>)singleValue).wrap(val);

                valuesToAdd.add(wrapped);
            }
            else
            {
                valuesToAdd.add(w);
            }
        }

        newPacket.getDataValueCollectionModifier().write(0, valuesToAdd);

        return newPacket;
    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
    }

    private final ListeningWhitelist listeningWhitelist = ListeningWhitelist
            .newBuilder()
            .types(PacketType.Play.Server.ENTITY_METADATA)
            .build();

    @Override
    public ListeningWhitelist getSendingWhitelist()
    {
        return listeningWhitelist;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
    }
}
