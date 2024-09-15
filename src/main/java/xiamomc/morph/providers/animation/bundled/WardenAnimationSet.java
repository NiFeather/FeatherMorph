package xiamomc.morph.providers.animation.bundled;

import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.monster.warden.WardenAi;
import xiamomc.morph.misc.AnimationNames;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.SingleAnimation;

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
        registerPersistent(AnimationNames.DIGDOWN, List.of(EXEC_DISABLE_SKILL, EXEC_DISABLE_AMBIENT, DIGDOWN, VANISH, EXEC_DISABLE_BOSSBAR));

        registerCommon(AnimationNames.ROAR, List.of(EXEC_DISABLE_SKILL, ROAR, ROAR_SOUND, TRY_RESET, EXEC_ENABLE_SKILL));
        registerCommon(AnimationNames.SNIFF, List.of(EXEC_DISABLE_SKILL, SNIFF, TRY_RESET, EXEC_ENABLE_SKILL));
        registerCommon(AnimationNames.APPEAR, List.of(EXEC_ENABLE_BOSSBAR, APPEAR, EXEC_ENABLE_SKILL, EXEC_ENABLE_AMBIENT, RESET));
    }
}
