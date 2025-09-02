package xyz.nifeather.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.IAbility;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.skills.ISkill;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkillAbilityConfigContainer
{
    public SkillAbilityConfigContainer()
    {
    }

    /**
     * 创建一个技能配置
     *
     * @param skillCooldown CD时间
     * @param skillIdentifier 技能ID
     */
    public SkillAbilityConfigContainer(int skillCooldown, NamespacedKey skillIdentifier)
    {
        this.skillCooldown = skillCooldown;
        setSkillIdentifier(skillIdentifier);
    }

    @Nullable
    @Expose(serialize = false)
    @SerializedName("mobId")
    public String legacy_MobID;

    //region 主动技能

    @Expose
    @SerializedName("skillCooldown")
    private int skillCooldown;

    /**
     * 获取技能的默认冷却时间
     *
     * @return 默认冷却时间
     */
    public int getSkillCooldown()
    {
        return skillCooldown;
    }

    public SkillAbilityConfigContainer setSkillCooldown(int newCd)
    {
        this.skillCooldown = newCd;
        return this;
    }

    @Expose
    @SerializedName("skillId")
    private String rawSkillidentifier;

    @Expose(deserialize = false, serialize = false)
    private NamespacedKey skillIdentifier;

    /**
     * 获取技能的ID
     *
     * @return 技能ID
     */
    @NotNull
    public NamespacedKey getSkillIdentifier()
    {
        //没有配置技能ID -> NONE
        //技能ID转换出来的NameSpacedKey是null -> UNKNOWN
        if (skillIdentifier == null)
        {
            NamespacedKey k;
            if (rawSkillidentifier == null) k = SkillNames.NONE;
            else k = NamespacedKey.fromString(rawSkillidentifier);

            skillIdentifier = k == null ? SkillNames.UNKNOWN : k;
        }

        return skillIdentifier;
    }

    /**
     * 设置技能ID
     *
     * @apiNote 内部方法
     * @param key ID
     */
    public SkillAbilityConfigContainer setSkillIdentifier(NamespacedKey key)
    {
        skillIdentifier = key;
        rawSkillidentifier = key.asString();

        return this;
    }

    //endregion 主动技能

    //region 被动技能

    @Expose
    @SerializedName("abilities")
    private final List<String> abilitiyIdentifiers = new CopyOnWriteArrayList<>();

    public List<String> getAbilitiyIdentifiers()
    {
        return abilitiyIdentifiers;
    }

    private SkillAbilityConfigContainer setAbilities(List<String> identifiers)
    {
        abilitiyIdentifiers.clear();

        if (identifiers != null)
            abilitiyIdentifiers.addAll(identifiers);

        return this;
    }

    public SkillAbilityConfigContainer addAbility(NamespacedKey id)
    {
        var idString = id.asString();

        if (abilitiyIdentifiers.stream().anyMatch(s -> s.equals(idString)))
            return this;

        this.abilitiyIdentifiers.add(idString);

        return this;
    }

    //endregion

    //region 技能设置

    // id <-> Option Map
    @Expose
    @NotNull
    @SerializedName("settings")
    private final Map<String, Map<String, Object>> options = new ConcurrentHashMap<>();

    /**
     * 获取某个主动技能的技能设置
     *
     * @param skill 目标技能
     * @return 技能设置Map
     */
    @NotNull
    public Map<String, Object> getSkillOptions(ISkill<?> skill)
    {
        if (skill == null) return Map.of();

        return options.getOrDefault(skill.getIdentifier().asString(), Map.of());
    }

    public <T extends ISkillAbilityOption> T readOptions(NamespacedKey abilityIdentifier,
                                                         ISkillAbilityOptionHandler<T> optionHandler) throws ParseErrorException, NullPointerException
    {
        var gsonMap = options.getOrDefault(abilityIdentifier.asString(), null);

        if (optionHandler.acceptNullableOptions())
            return optionHandler.readOptionNullable(gsonMap);
        else if (gsonMap == null)
            throw new NullPointerException("Null option map for ability '%s'".formatted(abilityIdentifier));

        return optionHandler.readOption(gsonMap);
    }

    @Nullable
    public <T extends ISkillAbilityOption> T readOptions(@Nullable IAbility<T> ability) throws ParseErrorException, NullPointerException
    {
        if (ability == null) return null;

        return readOptions(ability.getIdentifier(), ability.optionHandler());
    }

    public <T extends ISkillAbilityOption> T readOptions(@Nullable ISkill<T> skill) throws ParseErrorException, NullPointerException
    {
        if (skill == null) return null;

        return readOptions(skill.getIdentifier(), skill.optionHandler());
    }

    /**
     * Clear the current option for the given skill/ability, then sets the new value
     */
    public <O extends ISkillAbilityOption> SkillAbilityConfigContainer setOption(NamespacedKey identifier,
                                                                                 ISkillAbilityOptionHandler<O> optionHandler,
                                                                                 O option)
    {
        Map<String, Object> currentOptionMap = options.getOrDefault(identifier.asString(), null);

        if (currentOptionMap == null)
        {
            var newMap = new ConcurrentHashMap<String, Object>();
            options.put(identifier.asString(), newMap);
            currentOptionMap = newMap;
        }

        var optionMap = new Object2ObjectOpenHashMap<String, Object>();
        optionHandler.writeOption(option, optionMap);
        currentOptionMap.clear();
        currentOptionMap.putAll(optionMap);

        return this;
    }

    /**
     * 添加一个技能设置用于存储。
     * <bold>若此存储已经有此技能的配置，那么将更新不存在的条目</bold>
     *
     * @param option 目标设置
     */
    public <O extends ISkillAbilityOption> SkillAbilityConfigContainer appendOption(NamespacedKey identifier,
                                                                                    ISkillAbilityOptionHandler<O> optionHandler,
                                                                                    O option)
    {
        Map<String, Object> currentOptionMap = options.getOrDefault(identifier.asString(), null);

        if (currentOptionMap == null)
        {
            var newMap = new ConcurrentHashMap<String, Object>();
            options.put(identifier.asString(), newMap);
            currentOptionMap = newMap;
        }

        var optionMap = new Object2ObjectOpenHashMap<String, Object>();
        optionHandler.writeOption(option, optionMap);
        optionMap.forEach(currentOptionMap::putIfAbsent);

        return this;
    }

    //endregion 技能设置
}
