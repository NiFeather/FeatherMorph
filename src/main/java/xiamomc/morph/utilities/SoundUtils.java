package xiamomc.morph.utilities;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.sounds.SoundEvent;

public class SoundUtils
{
    @SuppressWarnings("PatternValidation")
    public static Sound toBukkitSound(EntityTypeUtils.SoundInfo si)
    {
        if (si.sound() == null)
            return null;

        var resLoc = si.sound().getLocation();
        var sound = Sound.sound().source(Sound.Source.PLAYER).volume(si.volume()).pitch(1F)
                .type(Key.key(resLoc.getNamespace(), resLoc.getPath())).build();

        return sound;
    }
}
