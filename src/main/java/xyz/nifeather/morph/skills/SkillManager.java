package xyz.nifeather.morph.skills;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.events.lifecycle.SkillsFinishedInitializeEvent;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.skills.impl.*;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;
import xyz.nifeather.morph.storage.skill.SkillsConfigurationStoreNew;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class SkillManager extends MorphPluginObject
{
    /**
     * 已注册的技能
     */
    private final Map<String, ISkill<?>> skills = new ConcurrentHashMap<>();

    /**
     * 获取已注册的技能
     *
     * @return 技能列表
     */
    public List<ISkill<?>> getRegistedSkills()
    {
        return skills.values().stream().toList();
    }

    private final CooldownManager cooldownManager = new CooldownManager();

    public CooldownManager cooldownManager()
    {
        return cooldownManager;
    }

    @Resolved
    private SkillsConfigurationStoreNew store;

    @Initializer
    private void load()
    {
        registerSkills(ObjectList.of(
                new ApplyEffectMorphSkill(),
                new ExplodeMorphSkill(),
                new InventoryMorphSkill(),
                new LaunchProjectileMorphSkill(),
                new EvokerMorphSkill(),
                new TeleportMorphSkill(),
                new SonicBoomMorphSkill(),
                new SplashPotionSkill(),
                new GuardianSkill(),

                NoneMorphSkill.instance
        ));

        Bukkit.getPluginManager().callEvent(new SkillsFinishedInitializeEvent(this));
    }

    /**
     * 注册一批技能
     * @param skills 技能列表
     * @return 所有操作是否成功
     */
    public boolean registerSkills(List<ISkill<?>> skills)
    {
        var success = new AtomicBoolean(true);

        skills.forEach(s ->
        {
            if (!registerSkill(s)) success.set(false);
        });

        return success.get();
    }

    /**
     * 注册一个技能
     * @param skill 技能
     * @return 操作是否成功
     */
    public boolean registerSkill(ISkill<?> skill)
    {
        //logger.info("Registering skill: " + skill.getIdentifier().asString());

        if (skills.containsKey(skill.getIdentifier().asString()))
        {
            logger.error("Can't register skill: Another skill instance has already registered as " + skill.getIdentifier().asString() + " !");
            return false;
        }

        if (skill.getIdentifier().equals(SkillNames.UNKNOWN))
        {
            logger.error("Can't register skill: Illegal skill identifier: " + SkillNames.UNKNOWN);
            return false;
        }

        skills.put(skill.getIdentifier().asString(), skill);

        return true;
    }

    @NotNull
    public ISkill<?> lookupDisguiseSkill(String disguiseIdentifier)
    {
        var configuration = store.get(disguiseIdentifier);
        if (configuration == null) return NoneMorphSkill.instance;

        return this.getSkill(configuration.getSkillIdentifier().asString());
    }

    public SkillAbilityConfigContainer getConfiguration(String lookupId)
    {
        return store.get(lookupId);
    }

    public boolean hasSkillAbilityConfiguration(String lookupId)
    {
        return getConfiguration(lookupId) != null;
    }

    /**
     * @apiNote You may also want to check for {@link SkillManager#hasSkillAbilityConfiguration(String)},
     *          or this will throw {@link NullPointerException} if the given disguise doesn't have a matching configuration file.
     *
     * @throws ParseErrorException If there's a parse error
     * @throws NullPointerException If the disguise doesn't have a matching configuration file
     */
    public <O extends ISkillAbilityOption> O lookupOptionFor(ISkill<O> skill, String skillLookup) throws ParseErrorException, NullPointerException
    {
        var configContainer = store.get(skillLookup);
        var optionMap = Objects.requireNonNull(configContainer, "No configuration for id " + skillLookup)
                .getSkillOptions(skill);

        return skill.optionHandler().acceptNullableOptions()
                ? skill.optionHandler().readOptionNullable(optionMap)
                : skill.optionHandler().readOption(optionMap);
    }

    /**
     * 获取和identifier匹配的技能
     *
     * @param skillIdentifier 技能ID
     * @return {@link ISkill}
     * @apiNote 如果未找到则返回 {@link NoneMorphSkill#instance}
     */
    @NotNull
    public ISkill<?> getSkill(String skillIdentifier)
    {
        return this.skills.getOrDefault(skillIdentifier, NoneMorphSkill.instance);
    }

    /**
     * 获取技能冷却
     *
     * @param uuid 玩家UUID
     * @param disguiseIdentifier 技能ID
     * @return 技能信息，为null则传入的实体类型是null
     */
    public long getAvailableAfter(UUID uuid, @Nullable String disguiseIdentifier)
    {
        return cooldownManager.pull(uuid, disguiseIdentifier);
    }

    /**
     * 某个实体类型是否有技能
     *
     * @param disguiseIdentifier 实体ID
     * @return 是否拥有技能
     */
    public boolean hasSkill(String disguiseIdentifier)
    {
        var container = store.get(disguiseIdentifier);
        if (container == null) return false;

        return !SkillNames.UNKNOWN.equals(container.getSkillIdentifier())
                && !SkillNames.NONE.equals(container.getSkillIdentifier());
    }

    /**
     * 某个实体类型是否拥有某个特定的技能
     * @param id 实体ID
     * @param skillKey 目标技能的Key
     * @return 是否拥有
     */
    public boolean hasSpeficSkill(String id, NamespacedKey skillKey)
    {
        var container = store.get(id);
        if (container == null) return false;

        if (SkillNames.UNKNOWN.equals(container.getSkillIdentifier())) return false;

        return container.getSkillIdentifier().equals(skillKey);
    }

    /**
     * 清除 CooldownManager 中不再需要记录的冷却信息
     */
    public void trim()
    {
        cooldownManager.trim();
    }
}
