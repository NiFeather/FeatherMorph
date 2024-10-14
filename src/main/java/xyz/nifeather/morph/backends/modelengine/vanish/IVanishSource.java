package xyz.nifeather.morph.backends.modelengine.vanish;

import org.bukkit.entity.Player;

public interface IVanishSource
{
    public void vanishPlayer(Player player);

    public void cancelVanish(Player player);
}
