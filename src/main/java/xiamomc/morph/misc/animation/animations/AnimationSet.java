package xiamomc.morph.misc.animation.animations;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnimationSet
{
    public static final SingleAnimation RESET = new SingleAnimation(AnimationNames.RESET, 1, true);
    public static final SingleAnimation TRY_RESET = new SingleAnimation(AnimationNames.TRY_RESET, 1, true);
    public static final SingleAnimation DISABLE_SKILL = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_SKILL, 0, false);
    public static final SingleAnimation ENABLE_SKILL = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_SKILL, 0, false);
    public static final SingleAnimation DISABLE_AMBIENT = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_AMBIENT, 0, false);
    public static final SingleAnimation ENABLE_AMBIENT = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_AMBIENT, 0, false);
    public static final SingleAnimation DISABLE_BOSSBAR = new SingleAnimation(AnimationNames.INTERNAL_DISABLE_BOSSBAR, 0, false);
    public static final SingleAnimation ENABLE_BOSSBAR = new SingleAnimation(AnimationNames.INTERNAL_ENABLE_BOSSBAR, 0, false);

    private final Map<String, List<SingleAnimation>> animationMap = new ConcurrentHashMap<>();

    private final List<String> registeredAnimations = new ObjectArrayList<>();

    protected void register(String animationId, List<SingleAnimation> animations)
    {
        animationMap.put(animationId, animations);
        registeredAnimations.add(animationId);
    }

    @NotNull
    public List<SingleAnimation> sequenceOf(String animationId)
    {
        return animationMap.getOrDefault(animationId, List.of());
    }

    public List<String> getAvailableAnimationsForClient()
    {
        return new ObjectArrayList<>(registeredAnimations);
    }
}
