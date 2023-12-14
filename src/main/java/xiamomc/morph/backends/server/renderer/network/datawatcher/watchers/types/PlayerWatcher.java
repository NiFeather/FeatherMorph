package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.ClientOption;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.misc.DisguiseEquipment;

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

    @Override
    protected void onCustomWrite(RegistryKey<?> key, Object val)
    {
        super.onCustomWrite(key, val);
    }
}
