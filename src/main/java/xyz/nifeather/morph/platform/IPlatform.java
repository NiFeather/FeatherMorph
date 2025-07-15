package xyz.nifeather.morph.platform;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface IPlatform
{
    @Unmodifiable
    public List<Player> onlinePlayers();
}
