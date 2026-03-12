package xyz.nifeather.morph.providers.animation.bundled;

import net.minecraft.world.entity.ai.behavior.warden.Sniffing;
import net.minecraft.world.entity.monster.warden.WardenAi;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Pose;
import xyz.nifeather.morph.misc.AnimationNames;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.providers.animation.AnimationSet;
import xyz.nifeather.morph.providers.animation.PlayableAction;

import java.util.List;

public class WardenAnimationSet extends AnimationSet
{
    private BaseLivingEntityPropertyCollection<?> properties()
    {
        return DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);
    }

    public final PlayableAction SNIFF = PlayableAction.builder()
            .addStage(b ->
            {
                // See net.minecraft.world.entity.ai.behavior.Behavior#DEFAULT_DURATION
                b.duration(60)
                        .legacyName(AnimationNames.SNIFF)
                        .onPlay(state ->
                        {
                            state.requestSkillState(this, true);
                            state.disguisePropertyHandler().setTemp(properties().STATIC_POSE, Pose.SNIFFING);

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_SNIFF, SoundCategory.HOSTILE, 5, 1);
                        })
                        .onFinish(state ->
                        {
                            state.requestSkillState(this, false);
                            state.disguisePropertyHandler().discardTemporaryProperty(properties().STATIC_POSE);
                        });
            })
            .addStage(b -> b.duration(0).legacyName(AnimationNames.TRY_RESET))
            .build();

    public final PlayableAction ROAR = PlayableAction.builder()
            .addStage(b ->
            {
                // For the 25 ticks of duration, see net.minecraft.world.entity.ai.behavior.warden.Roar#start()
                b.duration(25)
                        .legacyName(AnimationNames.ROAR)
                        .onPlay(state ->
                        {
                            state.requestSkillState(this, true);
                            state.disguisePropertyHandler().setTemp(properties().STATIC_POSE, Pose.ROARING);
                        })
                        .onFinish(state ->
                        {
                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 3, 1);

                            state.requestSkillState(this, false);
                            state.disguisePropertyHandler().discardTemporaryProperty(properties().STATIC_POSE);
                        });
            })
            .addStage(b -> b.duration(0).legacyName(AnimationNames.ROAR_SOUND))
            .addStage(b -> b.duration(0).legacyName(AnimationNames.TRY_RESET))
            .build();

    public final PlayableAction DIGDOWN_AND_VANISH = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = properties();

                // See WardenAi -> DIGGING_DURATION
                b.duration(100)
                        .legacyName(AnimationNames.DIGDOWN)
                        .onPlay(state ->
                        {
                            var player = state.getPlayer();
                            state.disguisePropertyHandler().setTemp(properties.STATIC_POSE, Pose.DIGGING);

                            state.requestAmbientState(this, true);
                            state.requestSkillState(this, true);

                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_DIG, 5, 1);
                        })
                        .onFinish(state ->
                        {
                            state.disguisePropertyHandler().setTemp(properties.INVISIBLE, true);
                            state.disguisePropertyHandler().discardTemporaryProperty(properties.STATIC_POSE);
                            state.requestBossbarState(this, true);
                        });
            })
            .addStage(b -> b.duration(0).legacyName(AnimationNames.VANISH))
            .build();

    public final PlayableAction APPEAR = PlayableAction.builder()
            .addStage(b ->
            {
                var properties = properties();

                // See net.minecraft.world.entity.monster.warden.WardenAi#EMERGE_DURATION
                b.duration(134)
                        .legacyName(AnimationNames.APPEAR)
                        .onPlay(state ->
                        {
                            state.disguisePropertyHandler().discardTemporaryProperty(properties.INVISIBLE);
                            state.disguisePropertyHandler().setTemp(properties.STATIC_POSE, Pose.EMERGING);

                            state.getDisguiseWrapper().getBackend().respawnDisguise(state.getDisguiseWrapper());

                            var player = state.getPlayer();
                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_EMERGE, 5, 1);
                        })
                        .onFinish(state ->
                        {
                            state.requestAmbientState(this, false);
                            state.requestBossbarState(this, false);
                            state.requestSkillState(this, false);

                            state.disguisePropertyHandler().discardTemporaryProperty(properties.STATIC_POSE);
                        });
            })
            .addStage(b -> b.duration(0).legacyName(AnimationNames.RESET))
            .build();

    public WardenAnimationSet()
    {
        register(AnimationNames.DIGDOWN, DIGDOWN_AND_VANISH);
        register(AnimationNames.ROAR, ROAR);
        register(AnimationNames.APPEAR, APPEAR);
        register(AnimationNames.SNIFF, SNIFF);
    }
}
