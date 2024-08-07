package xiamomc.morph.misc.animation;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.animation.animations.AnimationSet;
import xiamomc.morph.misc.animation.animations.SnifferAnimationSet;
import xiamomc.morph.misc.animation.animations.WardenAnimationSet;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationHandler extends MorphPluginObject
{
    @Initializer
    private void load()
    {
        this.registerAnimSet(EntityType.WARDEN.getKey().asString(), new WardenAnimationSet());
        this.registerAnimSet(EntityType.SNIFFER.getKey().asString(), new SnifferAnimationSet());
    }

    // AnimationID <-> AnimationSequence
    private final Map<String, AnimationSet> animSets = new ConcurrentHashMap<>();

    private void registerAnimSet(String disguiseIdentifier, AnimationSet animationSet)
    {
        animSets.put(disguiseIdentifier, animationSet);
    }

    @NotNull
    public List<String> getAvailableAnimationsFor(String disguiseIdentifier)
    {
        var animSet = this.animSets.getOrDefault(disguiseIdentifier, null);
        if (animSet == null) return List.of();

        return animSet.getAvailableAnimationsForClient();
    }

    @NotNull
    public List<SingleAnimation> getSequenceFor(String disguiseIdentifier, String animationIdentifier)
    {
        var animSet = this.animSets.getOrDefault(disguiseIdentifier, null);
        if (animSet == null) return List.of();

        return animSet.sequenceOf(animationIdentifier);
    }
}
