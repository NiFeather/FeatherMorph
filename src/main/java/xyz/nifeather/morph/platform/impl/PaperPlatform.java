package xyz.nifeather.morph.platform.impl;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.platform.IPlatform;

import java.util.List;

public class PaperPlatform implements IPlatform
{
    private final FeatherMorphMain plugin;

    public PaperPlatform(FeatherMorphMain plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public List<Player> onlinePlayers()
    {
        return ImmutableList.copyOf(Bukkit.getOnlinePlayers());
    }
}
