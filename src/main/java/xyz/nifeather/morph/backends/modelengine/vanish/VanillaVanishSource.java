package xyz.nifeather.morph.backends.modelengine.vanish;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.nifeather.morph.MorphPluginObject;

public class VanillaVanishSource extends MorphPluginObject implements IVanishSource
{
    private final PotionEffect invisibleEffect = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 15, true, false);

    @Override
    public void vanishPlayer(Player player)
    {
        player.addPotionEffect(invisibleEffect);
    }

    @Override
    public void cancelVanish(Player player)
    {
        player.removePotionEffect(invisibleEffect.getType());
    }
}
