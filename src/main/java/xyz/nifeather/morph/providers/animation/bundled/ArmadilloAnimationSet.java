package xyz.nifeather.morph.providers.animation.bundled;

import org.bukkit.Sound;
import org.bukkit.entity.Armadillo;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.ArmadilloPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.List;

public class ArmadilloAnimationSet extends AnimationSet
{
    private static ArmadilloPropertyCollection properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(ArmadilloPropertyCollection.class);
    }

    public final PlayableAction PANIC_ROLLING = PlayableAction.builder()
            .addStage(b ->
                    b.duration(10)
                            .legacyName(AnimationNames.PANIC_ROLLING)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().STATE, Armadillo.State.ROLLING);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_ROLL, 1, 1);
                            }))
            .addStage(b ->
                    b.duration(50)
                            .legacyName(AnimationNames.PANIC_SCARED)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().STATE, Armadillo.State.SCARED);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_LAND, 1, 1);
                            }))
            .addStage(b ->
                    b.duration(30)
                            .legacyName(AnimationNames.PANIC_UNROLLING)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().STATE, Armadillo.State.UNROLLING);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_UNROLL_START, 1, 1);
                            }))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.PANIC_IDLE)
                            .onPlay(state ->
                            {
                                state.disguisePropertyHandler().set(properties().STATE, Armadillo.State.IDLE);

                                var player = state.getPlayer();
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARMADILLO_UNROLL_FINISH, 1, 1);
                            }))
            .addStage(b ->
                    b.duration(0)
                            .legacyName(AnimationNames.RESET))
            .build();

    public ArmadilloAnimationSet()
    {
        register(AnimationNames.PANIC, PANIC_ROLLING);
    }

    @Override
    public List<String> getAvailableAnimationsForClient()
    {
        return List.of(AnimationNames.PANIC);
    }
}
