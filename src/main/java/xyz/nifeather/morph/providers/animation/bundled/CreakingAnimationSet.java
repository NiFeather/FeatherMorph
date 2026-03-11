package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.CreakingPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class CreakingAnimationSet extends AnimationSet
{
    private static CreakingPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(CreakingPropertyCollection.class);
    }

    public final PlayableAction EYE_GLOW = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.MAKE_ACTIVE)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().EYES_GLOWING, true);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREAKING_ACTIVATE, SoundCategory.HOSTILE, 1, 1);
                            }))
            .build();

    public final PlayableAction DISABLE_EYE_GLOW = PlayableAction.builder()
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.MAKE_INACTIVE)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().EYES_GLOWING, false);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREAKING_FREEZE, SoundCategory.HOSTILE, 1, 1);
                            }))
            .build();

    public CreakingAnimationSet()
    {
        register(AnimationNames.DISPLAY_EYE_GLOW, EYE_GLOW);
        register(AnimationNames.DISPLAY_STOP_EYE_GLOW, DISABLE_EYE_GLOW);
    }
}
