package xyz.nifeather.morph.platform.impl.paper.entity;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.platform.CurrentPlatform;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.entity.IPlatformPlayer;

import java.util.Set;

public class PaperPlayer extends PaperEntity implements IPlatformPlayer
{
    private final Player handle;

    public PaperPlayer(Player player)
    {
        super(player);
        this.handle = player;
    }

    public Player handle()
    {
        return handle;
    }

    @Override
    public String playerName()
    {
        return handle.getName();
    }

    @Override
    public boolean hasPermission(String perm)
    {
        return handle.hasPermission(perm);
    }

    @Override
    public void sendMessage(Component adventureComponent)
    {
        handle.sendMessage(adventureComponent);
    }

    @Override
    public void sendMessage(String... msg)
    {
        handle.sendMessage(msg);
    }

    @Override
    public void sendActionBar(Component component)
    {
        handle.sendActionBar(component);
    }

    @Override
    public boolean isOnline()
    {
        return handle.isOnline();
    }

    @Override
    public void kick(Component reason)
    {
        handle.kick(reason);
    }

    @Override
    public IPlatformEntity getTargetEntity(int distance)
    {
        var target = handle.getTargetEntity(distance);

        if (!(target instanceof LivingEntity living)) return null;

        return CurrentPlatform.instance().entityLookup().getPlatformEntity(living);
    }

    @Override
    public Set<String> getListeningPluginChannels()
    {
        return handle.getListeningPluginChannels();
    }

    @Override
    public void sendPluginMessage(String channel, byte @NotNull [] buffer)
    {
        handle.sendPluginMessage(FeatherMorphMain.getInstance(), channel, buffer);
    }
}
