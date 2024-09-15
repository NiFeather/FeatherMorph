package xiamomc.morph.providers.animation.provider;

import org.bukkit.entity.EntityType;
import xiamomc.morph.providers.animation.AnimationProvider;
import xiamomc.morph.providers.animation.AnimationSet;
import xiamomc.morph.providers.animation.bundled.FallbackAnimationSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class DefaultAnimationProvider extends AnimationProvider
{
    protected final AnimationSet fallbackAnimationSet = new FallbackAnimationSet();

    // DisguiseID <-> AnimationSet
    protected final Map<String, AnimationSet> animSets = new ConcurrentHashMap<>();

    protected void registerAnimSet(EntityType type, AnimationSet animationSet)
    {
        this.registerAnimSet(type.getKey().asString(), animationSet);
    }

    protected void registerAnimSet(String disguiseIdentifier, AnimationSet animationSet)
    {
        animSets.put(disguiseIdentifier, animationSet);
    }
}
