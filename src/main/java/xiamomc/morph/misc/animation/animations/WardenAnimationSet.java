package xiamomc.morph.misc.animation.animations;

import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.monster.warden.WardenAi;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;

public class WardenAnimationSet extends AnimationSet
{
    public final SingleAnimation SNIFF = new SingleAnimation(AnimationNames.SNIFF, Sniffing.DEFAULT_DURATION, true);

    // For the 25 ticks of delay, see net.minecraft.world.entity.ai.behavior.warden.Roar#start()
    public final SingleAnimation ROAR = new SingleAnimation(AnimationNames.ROAR, 25, true);
    public final SingleAnimation ROAR_SOUND = new SingleAnimation(AnimationNames.ROAR_SOUND, WardenAi.ROAR_DURATION - 25, false);

    // See WardenAi -> DIGGING_DURATION
    public final SingleAnimation DIGDOWN = new SingleAnimation(AnimationNames.DIGDOWN, 100, true);
    public final SingleAnimation VANISH = new SingleAnimation(AnimationNames.VANISH, 0, true);
    public final SingleAnimation APPEAR = new SingleAnimation(AnimationNames.APPEAR, WardenAi.EMERGE_DURATION, true);

    public WardenAnimationSet()
    {
        register(AnimationNames.ROAR, List.of(DISABLE_SKILL, ROAR, ROAR_SOUND, TRY_RESET, ENABLE_SKILL));
        register(AnimationNames.SNIFF, List.of(DISABLE_SKILL, SNIFF, TRY_RESET, ENABLE_SKILL));
        register(AnimationNames.DIGDOWN, List.of(DISABLE_SKILL, DISABLE_AMBIENT, DIGDOWN, VANISH, DISABLE_BOSSBAR));
        register(AnimationNames.APPEAR, List.of(ENABLE_BOSSBAR, APPEAR, ENABLE_SKILL, ENABLE_AMBIENT, RESET));
    }
}
