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
