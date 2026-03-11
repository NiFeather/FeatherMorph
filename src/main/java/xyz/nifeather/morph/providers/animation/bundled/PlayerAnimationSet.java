package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.entity.Pose;
import org.joml.Vector3i;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

public class PlayerAnimationSet extends AnimationSet
{
    private static PlayerPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);
    }

    public final PlayableAction LAY = PlayableAction.builder()
            .addStage(b -> b.duration(0).legacyName(AnimationNames.LAY)
                    .onPlay(state ->
                    {
                        var properties = properties();
                        state.disguisePropertyHandler().set(properties.STATIC_POSE, Pose.SLEEPING);

                        var playerPos = state.getPlayer().getLocation();
                        var vec3i = new Vector3i(playerPos.blockX(), playerPos.blockY(), playerPos.blockZ());
                        state.disguisePropertyHandler().set(properties.BED_POS, vec3i);
                    }))
            .build();

    public final PlayableAction CRAWL = PlayableAction.builder()
            .addStage(b -> b.duration(0).legacyName(AnimationNames.CRAWL)
                    .onPlay(state ->
                    {
                        var properties = properties();
                        resetPoseAndBed(state, properties);

                        state.disguisePropertyHandler().set(properties.STATIC_POSE, Pose.SWIMMING);
                    }))
            .build();

    public final PlayableAction STAND = PlayableAction.builder()
            .addStage(b -> b.duration(0).legacyName(AnimationNames.STANDUP)
                    .onPlay(state ->
                    {
                        var properties = properties();
                        resetPoseAndBed(state, properties);
                    }))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    private void resetPoseAndBed(DisguiseState state, PlayerPropertyCollection properties)
    {
        var propertyHandler = state.disguisePropertyHandler();
        propertyHandler.discardProperty(properties.BED_POS);
        propertyHandler.discardProperty(properties.STATIC_POSE);
    }

    public PlayerAnimationSet()
    {
        register(AnimationNames.LAY, LAY);
        register(AnimationNames.CRAWL, CRAWL);
        register(AnimationNames.STANDUP, STAND);
    }
}
