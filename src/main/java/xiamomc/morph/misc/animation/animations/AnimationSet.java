package xiamomc.morph.misc.animation.animations;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.animation.AnimationNames;
import xiamomc.morph.misc.animation.SingleAnimation;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AnimationSet
{
    public final SingleAnimation POSE_RESET = new SingleAnimation(AnimationNames.POSE_RESET, 1, false);

    private final Map<String, List<SingleAnimation>> animationMap = new ConcurrentHashMap<>();

    protected void register(String animationId, List<SingleAnimation> animations)
    {
        animationMap.put(animationId, animations);
    }

    @NotNull
    public List<SingleAnimation> sequenceOf(String animationId)
    {
        return animationMap.getOrDefault(animationId, List.of());
    }

    public abstract List<String> getAvailableAnimationsForClient();
}