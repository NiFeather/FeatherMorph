package xiamomc.morph.misc.animation;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.animation.animations.*;
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
        this.registerAnimSet(EntityType.ALLAY.getKey().asString(), new AllayAnimationSet());
        this.registerAnimSet(EntityType.ARMADILLO.getKey().asString(), new ArmadilloAnimationSet());
        this.registerAnimSet(EntityType.SHULKER.getKey().asString(), new ShulkerAnimationSet());
        this.registerAnimSet(EntityType.CAT.getKey().asString(), new CatAnimationSet());

        // Disabled because parrot dancing is not controlled directly by the metadata or event
        // this.registerAnimSet(EntityType.PARROT.getKey().asString(), new ParrotAnimationSet());

        this.registerAnimSet(EntityType.PIGLIN.getKey().asString(), new PiglinAnimationSet());
        this.registerAnimSet(EntityType.PUFFERFISH.getKey().asString(), new PufferfishAnimationSet());
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
