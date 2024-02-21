package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.EffectConfiguration;
import xiamomc.morph.skills.options.ExplosionConfiguration;
import xiamomc.morph.skills.options.ProjectileConfiguration;
import xiamomc.morph.skills.options.TeleportConfiguration;

import java.util.List;
import java.util.Map;

public class SkillAbilityConfiguration
{
    public SkillAbilityConfiguration()
    {
    }

    /**
     * 创建一个技能配置
     *
     * @param mobId 生物ID
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     */
    public SkillAbilityConfiguration(String mobId, int cd, NamespacedKey skillIdentifier)
    {
        this.identifier = mobId;
        this.cooldown = cd;
        setSkillIdentifier(skillIdentifier);
    }

    /**
     * 创建一个技能配置
     *
     * @param type 生物类型
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     */
    public SkillAbilityConfiguration(EntityType type, int cd, NamespacedKey skillIdentifier)
    {
        this(type.getKey(), cd, skillIdentifier);
    }

    /**
     * 创建一个技能配置
     *
     * @param key 生物ID
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     */
    public SkillAbilityConfiguration(NamespacedKey key, int cd, NamespacedKey skillIdentifier)
    {
        this.cooldown = cd;
        setSkillIdentifier(skillIdentifier);
        setIdentifier(key);
    }

    @Expose
    @SerializedName("mobId")
    private String identifier;

    /**
     * 获取和配置对应的伪装ID
     *
     * @return 伪装ID
     */
    @NotNull
    public String getIdentifier()
    {
        return identifier;
    }

    public void setIdentifier(NamespacedKey key)
    {
        this.identifier = key.asString();
    }

    public SkillAbilityConfiguration setIdentifier(String id)
    {
        this.identifier = id;

        return this;
    }

    //region 主动技能

    @Expose
    @SerializedName("skillCooldown")
    private int cooldown;

    /**
     * 获取技能的默认冷却时间
     *
     * @return 默认冷却时间
     */
    public int getCooldown()
    {
        return cooldown;
    }

    public SkillAbilityConfiguration setCooldown(int newCd)
    {
        this.cooldown = newCd;
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
            if (rawSkillidentifier == null) k = SkillType.NONE;
            else k = NamespacedKey.fromString(rawSkillidentifier);

            skillIdentifier = k == null ? SkillType.UNKNOWN : k;
        }

        return skillIdentifier;
    }

    /**
     * 设置技能ID
     *
     * @apiNote 内部方法
     * @param key ID
     */
    public SkillAbilityConfiguration setSkillIdentifier(NamespacedKey key)
    {
        skillIdentifier = key;
        rawSkillidentifier = key.asString();

        return this;
    }

    //endregion 主动技能

    //region 被动技能

    @Expose
    @SerializedName("abilities")
    private final List<String> abilitiyIdentifiers = new ObjectArrayList<>();

    public List<String> getAbilitiyIdentifiers()
    {
        return abilitiyIdentifiers;
    }

    private SkillAbilityConfiguration setAbilitiyIdentifiers(List<String> identifiers)
    {
        abilitiyIdentifiers.clear();

        if (identifiers != null)
            abilitiyIdentifiers.addAll(identifiers);

        return this;
    }

    public SkillAbilityConfiguration addAbilityIdentifier(NamespacedKey id)
    {
        var idString = id.asString();

        if (abilitiyIdentifiers.stream().anyMatch(s -> s.equals(idString)))
            return this;

        this.abilitiyIdentifiers.add(idString);

        return this;
    }

    //endregion

    //region 技能设置

    @Expose
    @Nullable
    @SerializedName("settings")
    private Object2ObjectOpenHashMap<String, Map<String, Object>> options = new Object2ObjectOpenHashMap<>();

    /**
     * 获取某个主动技能的技能设置
     *
     * @param skill 目标技能
     * @return 技能设置Map
     */
    @Nullable
    public Map<String, Object> getSkillOptions(IMorphSkill<?> skill)
    {
        if (options == null || skill == null) return null;

        return options.get(skill.getIdentifier().asString());
    }

    @Nullable
    public ISkillOption getAbilityOptions(IMorphAbility<?> ability)
    {
        if (options == null || ability == null) return null;

        return ability.getDefaultOption().fromMap(options.get(ability.getIdentifier().asString()));
    }

    /**
     * 获取某个技能的原始设置
     *
     * @param ability 被动技能
     * @return 技能设置Map
     */
    @Nullable
    public Map<String, Object> getRawOptions(IMorphAbility<?> ability)
    {
        if (options == null || ability == null) return null;

        return options.get(ability.getIdentifier().asString());
    }

    public boolean moveOption(NamespacedKey oldIdentifier, NamespacedKey newIdentifier)
    {
        if (options == null) return false;

        var option = options.getOrDefault(oldIdentifier.asString(), null);
        if (option != null)
        {
            options.remove(oldIdentifier.asString());
            options.put(newIdentifier.asString(), option);

            return true;
        }

        return false;
    }

    /**
     * 添加一个技能设置用于存储。
     * <bold>若此存储已经有此技能的配置，那么将更新不存在的条目</bold>
     *
     * @apiNote 如果要更新一个列表、集合或类似的东西，请使用 {@link SkillAbilityConfiguration#setOption(String, ISkillOption)}
     * @param identifier 技能ID
     * @param option 目标设置
     */
    public SkillAbilityConfiguration withOption(NamespacedKey identifier, ISkillOption option)
    {
        Map<String, Object> currentOptionMap = options == null ? null : options.getOrDefault(identifier.asString(), null);

        if (currentOptionMap != null)
        {
            var map = option.toMap();

            map.forEach(currentOptionMap::putIfAbsent);
        }
        else
        {
            this.setOption(identifier.asString(), option.toMap());
        }

        return this;
    }

    public SkillAbilityConfiguration setOption(String identifier, Map<String, Object> map)
    {
        if (options != null)
            this.options.put(identifier, map);

        return this;
    }

    /**
     * 将给定ID对应的配置设置或覆盖为给定的option
     * @param identifier 目标ID
     * @param option 要设置或覆盖的{@link ISkillOption}
     * @return this
     */
    public SkillAbilityConfiguration setOption(String identifier, ISkillOption option)
    {
        if (options != null && option != null)
            return setOption(identifier, option.toMap());

        return this;
    }

    //endregion 技能设置

    @Override
    public String toString()
    {
        return "Skill configuration for disguise " + this.identifier;
    }


    @Expose(serialize = false)
    @SerializedName("projective")
    private ProjectileConfiguration projectileConfiguration;

    @Deprecated
    public ProjectileConfiguration getProjectiveConfiguration()
    {
        return projectileConfiguration;
    }

    @Expose(serialize = false)
    @SerializedName("effect")
    private EffectConfiguration effectConfiguration;

    @Deprecated
    public EffectConfiguration getEffectConfiguration()
    {
        return effectConfiguration;
    }

    @Expose(serialize = false)
    @SerializedName("explosion")
    private ExplosionConfiguration explosionConfiguration;

    @Deprecated
    public ExplosionConfiguration getExplosionConfiguration()
    {
        return explosionConfiguration;
    }

    @Expose(serialize = false)
    @SerializedName("teleport")
    private TeleportConfiguration teleportConfiguration;

    @Deprecated
    public TeleportConfiguration getTeleportConfiguration()
    {
        return teleportConfiguration;
    }
}
