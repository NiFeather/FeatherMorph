package xiamomc.morph.misc.animation;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseTypes;
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
        this.registerAnimSet(EntityType.WARDEN, new WardenAnimationSet());
        this.registerAnimSet(EntityType.SNIFFER, new SnifferAnimationSet());
        this.registerAnimSet(EntityType.ALLAY, new AllayAnimationSet());
        this.registerAnimSet(EntityType.ARMADILLO, new ArmadilloAnimationSet());
        this.registerAnimSet(EntityType.SHULKER, new ShulkerAnimationSet());
        this.registerAnimSet(EntityType.CAT, new CatAnimationSet());

        // Disabled because parrot dancing is not controlled directly by the metadata or event
        // this.registerAnimSet(EntityType.PARROT.getKey().asString(), new ParrotAnimationSet());

        this.registerAnimSet(EntityType.PIGLIN, new PiglinAnimationSet());
        this.registerAnimSet(EntityType.PUFFERFISH, new PufferfishAnimationSet());
        this.registerAnimSet(EntityType.FOX, new FoxAnimationSet());
        this.registerAnimSet(EntityType.FROG, new FrogAnimationSet());
        this.registerAnimSet(EntityType.WOLF, new WolfAnimationSet());
        this.registerAnimSet(EntityType.PANDA, new PandaAnimationSet());
        this.registerAnimSet(playerDisguiseId, new PlayerAnimationSet());
    }

    private final String playerDisguiseId = DisguiseTypes.PLAYER.getNameSpace() + ":" + MorphManager.disguiseFallbackName;

    // AnimationID <-> AnimationSequence
    private final Map<String, AnimationSet> animSets = new ConcurrentHashMap<>();

    private void registerAnimSet(EntityType type, AnimationSet animationSet)
    {
        this.registerAnimSet(type.getKey().asString(), animationSet);
    }

    private void registerAnimSet(String disguiseIdentifier, AnimationSet animationSet)
    {
        animSets.put(disguiseIdentifier, animationSet);
    }

    @NotNull
    public List<String> getAvailableAnimationsFor(String disguiseIdentifier)
    {
        if (disguiseIdentifier.startsWith("player:"))
            disguiseIdentifier = playerDisguiseId;

        var animSet = this.animSets.getOrDefault(disguiseIdentifier, null);
        if (animSet == null) return List.of();

        return animSet.getAvailableAnimationsForClient();
    }

    @NotNull
    public List<SingleAnimation> getSequenceFor(String disguiseIdentifier, String animationIdentifier)
    {
        if (disguiseIdentifier.startsWith("player:"))
            disguiseIdentifier = playerDisguiseId;

        var animSet = this.animSets.getOrDefault(disguiseIdentifier, null);
        if (animSet == null) return List.of();

        return animSet.sequenceOf(animationIdentifier);
    }
}
