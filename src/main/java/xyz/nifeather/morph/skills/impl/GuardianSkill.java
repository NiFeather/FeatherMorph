package xyz.nifeather.morph.skills.impl;

import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Difficulty;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.GuardianProperties;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;

public class GuardianSkill extends DelayedMorphSkill<NoOpConfiguration>
{
    private final GuardianProperties properties;

    public GuardianSkill()
    {
        properties = DisguiseProperties.INSTANCE.getOrThrow(GuardianProperties.class);
    }

    @Override
    protected int getExecuteDelay(SkillAbilityConfigContainer configuration, NoOpConfiguration option)
    {
        return 80; // Normal guardian has 80, while elder guardian has 60
    }

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state,
                                       @NotNull SkillAbilityConfigContainer configuration, @NotNull NoOpConfiguration option)
    {
        if (state.getEntityType() != EntityType.GUARDIAN)
            return ExecuteResult.fail(20);

        int distanceLimit = 16;
        var targetEntity = player.getTargetEntity(distanceLimit);

        if (targetEntity == null)
        {
            sendDenyMessageToPlayer(player, SkillStrings.noTargetString()
                    .withLocale(MessageUtils.getLocale(player))
                    .resolve("distance", "" + distanceLimit)
                    .toComponent());

            return ExecuteResult.fail(20);
        }

        if (!(targetEntity instanceof LivingEntity living))
        {
            var component = SkillStrings.targetNotSuitableString().withLocale(MessageUtils.getLocale(player)).toComponent();
            sendDenyMessageToPlayer(player, component);

            return ExecuteResult.fail(20);
        }

        state.disguisePropertyHandler().set(properties.ATTACK_TARGET, targetEntity.getEntityId());
        state.getDisguiseWrapper().writeProperty(properties.ATTACK_TARGET, targetEntity.getEntityId());

        var id = RandomStringUtils.secure().randomAlphabetic(8);
        state.setSessionData(DATAKEY, targetEntity);
        state.setSessionData(EXECUTE_ID, id);
        this.scheduleOn(player, () -> updateState(state, player, living, id));

        return ExecuteResult.success(configuration.getSkillCooldown());
    }

    private static final int MAX_TRACE_DISTANCE = 16;

    private static final String DATAKEY = "feathermorph:guardian_attack_target";
    private static final String EXECUTE_ID = "feathermorph:guardian_skill_execute_id";

    // 对准目标时显示光束，其他情况隐藏
    private void updateState(DisguiseState state,
                             Player player, LivingEntity targetEntity,
                             String executeID)
    {
        if (state.disposed())
            return;

        if (!state.getSessionDataOr(EXECUTE_ID, String.class, "").equals(executeID))
            return;

        int id = targetEntity.equals(player.getTargetEntity(MAX_TRACE_DISTANCE)) ? targetEntity.getEntityId() : 0;

        state.disguisePropertyHandler().set(properties.ATTACK_TARGET, id);
        state.getDisguiseWrapper().writeProperty(properties.ATTACK_TARGET, id);

        this.scheduleOn(player, () -> updateState(state, player, targetEntity, executeID), 5);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfigContainer configuration, NoOpConfiguration option)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(GuardianProperties.class);
        state.disguisePropertyHandler().set(properties.ATTACK_TARGET, 0);
        state.getDisguiseWrapper().writeProperty(properties.ATTACK_TARGET, 0);

        state.removeSessionData(EXECUTE_ID);

        var platformEntity = state.getSessionData(DATAKEY, LivingEntity.class);
        if (platformEntity == null)
            return;

        state.removeSessionData(DATAKEY);

        //region Magic Damage

        double magicDamage = 1f;

        if (platformEntity.getWorld().getDifficulty() == Difficulty.HARD)
            magicDamage += 2f;

        if (state.getEntityType() == EntityType.ELDER_GUARDIAN)
            magicDamage += 2f;

        if (!platformEntity.equals(player.getTargetEntity(MAX_TRACE_DISTANCE)))
            return;

        var magicDamageSource = DamageSource.builder(DamageType.INDIRECT_MAGIC)
                .withDirectEntity(player)
                .withDamageLocation(player.getLocation())
                .build();

        platformEntity.damage(magicDamage, magicDamageSource);

        //endregion

        //region Direct Damage

        double directDamage = switch (platformEntity.getWorld().getDifficulty())
        {
            case PEACEFUL -> 0;
            case EASY -> 4;
            case NORMAL -> 6;
            case HARD -> 9;
        };

        var directDamageSource = DamageSource.builder(DamageType.GENERIC)
                .withDirectEntity(player)
                .withDamageLocation(player.getLocation())
                .build();

        platformEntity.damage(directDamage, directDamageSource);

        //endregion Direct Damage
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.GUARDIAN;
    }

    @Override
    public ISkillAbilityOptionHandler<NoOpConfiguration> optionHandler()
    {
        return NoOpConfiguration.OPTION_HANDLER;
    }
}
