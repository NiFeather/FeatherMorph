package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import com.destroystokyo.paper.ClientOption;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.listeners.ProtocolListener;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Optional;

public class PlayerWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.PLAYER);
    }

    public PlayerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PLAYER);
    }

    @Override
    protected void doSync()
    {
        this.write(ValueIndex.PLAYER.SKIN, (byte)getBindingPlayer().getClientOption(ClientOption.SKIN_PARTS).getRaw());
        this.write(ValueIndex.PLAYER.MAINHAND, (byte)getBindingPlayer().getMainHand().ordinal());

        super.doSync();
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Override
    protected void onCustomWrite(RegistryKey<?> key, Object oldVal, Object newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.DISPLAY_FAKE_EQUIPMENT) || key.equals(EntryIndex.EQUIPMENT))
            sendPacketToAffectedPlayers(packetFactory.getEquipmentPacket(getBindingPlayer(), this));
    }
}
