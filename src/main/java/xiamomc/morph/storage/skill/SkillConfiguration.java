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
import xiamomc.morph.skills.options.ProjectiveConfiguration;
import xiamomc.morph.skills.options.TeleportConfiguration;

import java.util.List;
import java.util.Map;

public class SkillConfiguration
{
    public SkillConfiguration()
    {
    }

    /**
     * 创建一个技能配置
     *
     * @param mobId 生物ID
     * @param cd CD时间
     * @param skillIdentifier 技能ID
     */
    public SkillConfiguration(String mobId, int cd, NamespacedKey skillIdentifier)
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
    public SkillConfiguration(EntityType type, int cd, NamespacedKey skillIdentifier)
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
    public SkillConfiguration(NamespacedKey key, int cd, NamespacedKey skillIdentifier)
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

    public void setIdentifier(String id)
    {
        this.identifier = id;
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

    public void setCooldown(int newCd)
    {
        this.cooldown = newCd;
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
    public void setSkillIdentifier(NamespacedKey key)
    {
        skillIdentifier = key;
        rawSkillidentifier = key.asString();
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

    private void setAbilitiyIdentifiers(List<String> identifiers)
    {
        abilitiyIdentifiers.clear();

        if (identifiers != null)
            abilitiyIdentifiers.addAll(identifiers);
    }

    public void addAbilityIdentifier(NamespacedKey id)
    {
        var idString = id.asString();

        if (abilitiyIdentifiers.stream().anyMatch(s -> s.equals(idString)))
            return;

        this.abilitiyIdentifiers.add(idString);
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
     * 添加一个技能设置用于存储
     *
     * @param identifier 技能ID
     * @param option 目标设置
     */
    public void addOption(NamespacedKey identifier, ISkillOption option)
    {
        Map<String, Object> currentOptionMap = null;

        if (options != null)
            currentOptionMap = options.getOrDefault(identifier.asString(), null);

        if (currentOptionMap != null)
        {
            var map = option.toMap();

            map.forEach(currentOptionMap::putIfAbsent);
        }
        else
        {
            this.setOption(identifier.asString(), option.toMap());
        }
    }

    public void setOption(String identifier, Map<String, Object> map)
    {
        if (options != null)
            this.options.put(identifier, map);
    }

    public void setOption(String identifier, ISkillOption option)
    {
        if (options != null && option != null)
            setOption(identifier, option.toMap());
    }

    //endregion 技能设置

    @Override
    public String toString()
    {
        return this.identifier + "的技能配置{" + cooldown + "tick冷却, 技能类型:" + skillIdentifier + "}";
    }


    @Expose(serialize = false)
    @SerializedName("projective")
    private ProjectiveConfiguration projectiveConfiguration;

    @Deprecated
    public ProjectiveConfiguration getProjectiveConfiguration()
    {
        return projectiveConfiguration;
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
