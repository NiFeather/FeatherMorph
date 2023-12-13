package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.destroystokyo.paper.ClientOption;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

import java.util.Optional;

public class PlayerWatcher extends LivingEntityWatcher
{
    public PlayerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.PLAYER);

        register(ValueIndex.PLAYER);
    }

    @Override
    protected void doSync()
    {
        this.write(ValueIndex.PLAYER.SKIN, (byte)getBindingPlayer().getClientOption(ClientOption.SKIN_PARTS).getRaw());
        this.write(ValueIndex.PLAYER.MAINHAND, (byte)getBindingPlayer().getMainHand().ordinal());

        super.doSync();
    }
}
