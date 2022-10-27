package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.skills.SkillType;

import java.util.ArrayList;
import java.util.List;

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
     * @param type 技能ID
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

    private void setIdentifier(NamespacedKey key)
    {
        this.identifier = key.asString();
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

    @Expose
    @Nullable
    @SerializedName("summon")
    private SummonConfiguration summonConfiguration;

    @Nullable
    public SummonConfiguration getSummonConfiguration()
    {
        return summonConfiguration;
    }

    public void setSummonConfiguration(@Nullable SummonConfiguration val)
    {
        this.summonConfiguration = val;
    }

    @Expose
    @Nullable
    @SerializedName("projective")
    private ProjectiveConfiguration projectiveConfiguration;

    @Nullable
    public ProjectiveConfiguration getProjectiveConfiguration()
    {
        return projectiveConfiguration;
    }

    public void setProjectiveConfiguration(@Nullable ProjectiveConfiguration val)
    {
        this.projectiveConfiguration = val;
    }

    @Expose
    @Nullable
    @SerializedName("effect")
    private EffectConfiguration effectConfiguration;

    @Nullable
    public EffectConfiguration getEffectConfiguration()
    {
        return effectConfiguration;
    }

    public void setEffectConfiguration(@Nullable EffectConfiguration val)
    {
        this.effectConfiguration = val;
    }

    @Expose
    @Nullable
    @SerializedName("explosion")
    private ExplosionConfiguration explosionConfiguration;

    @Nullable
    public ExplosionConfiguration getExplosionConfiguration()
    {
        return explosionConfiguration;
    }

    public void setExplosionConfiguration(@Nullable ExplosionConfiguration val)
    {
        this.explosionConfiguration = val;
    }

    @Expose
    @Nullable
    @SerializedName("teleport")
    private TeleportConfiguration teleportConfiguration;

    @Nullable
    public TeleportConfiguration getTeleportConfiguration()
    {
        return teleportConfiguration;
    }

    public void setTeleportConfiguration(@Nullable TeleportConfiguration val)
    {
        this.teleportConfiguration = val;
    }

    //endregion

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

    private final List<IMorphAbility> abilities = new ObjectArrayList<>();

    public List<IMorphAbility> getAbilities()
    {
        return abilities;
    }

    public void setAbilities(List<IMorphAbility> newAbilities)
    {
        abilities.clear();

        if (newAbilities != null)
            abilities.addAll(newAbilities);
    }

    //endregion

    @Override
    public String toString()
    {
        return this.identifier + "的技能配置{" + cooldown + "tick冷却, 技能类型:" + skillIdentifier + "}";
    }
}
