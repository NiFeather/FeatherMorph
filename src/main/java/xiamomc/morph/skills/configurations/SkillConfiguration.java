package xiamomc.morph.skills.configurations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.skills.SkillType;

public class SkillConfiguration
{
    public SkillConfiguration()
    {
    }

    public SkillConfiguration(String mobId, int cd, NamespacedKey type)
    {
        this.identifier = mobId;
        this.cooldown = cd;
        setSkillType(type);
    }

    public SkillConfiguration(EntityType type, int cd, NamespacedKey skillType)
    {
        this(type.getKey(), cd, skillType);
    }

    public SkillConfiguration(NamespacedKey key, int cd, NamespacedKey skillIdentifier)
    {
        this.cooldown = cd;
        setSkillType(skillIdentifier);
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

    private void setSkillType(NamespacedKey key)
    {
        skillIdentifier = key;
        rawSkillidentifier = key.asString();
    }

    @Expose
    @Nullable
    @SerializedName("summon")
    private SummonConfiguration summonConfiguration;

    @Expose
    @Nullable
    @SerializedName("projective")
    private ProjectiveConfiguration projectiveConfiguration;

    @Expose
    @Nullable
    @SerializedName("effect")
    private EffectConfiguration effectConfiguration;

    @Expose
    @Nullable
    @SerializedName("explosion")
    private ExplosionConfiguration explosionConfiguration;

    @Nullable
    public ProjectiveConfiguration getProjectiveConfiguration()
    {
        return projectiveConfiguration;
    }

    public void setProjectiveConfiguration(@Nullable ProjectiveConfiguration val)
    {
        this.projectiveConfiguration = val;
    }

    @Nullable
    public EffectConfiguration getEffectConfiguration()
    {
        return effectConfiguration;
    }

    public void setEffectConfiguration(@Nullable EffectConfiguration val)
    {
        this.effectConfiguration = val;
    }

    @Nullable
    public ExplosionConfiguration getExplosionConfiguration()
    {
        return explosionConfiguration;
    }

    public void setExplosionConfiguration(@Nullable ExplosionConfiguration val)
    {
        this.explosionConfiguration = val;
    }

    @Nullable
    public SummonConfiguration getSummonConfiguration()
    {
        return summonConfiguration;
    }

    public void setSummonConfiguration(@Nullable SummonConfiguration val)
    {
        this.summonConfiguration = val;
    }

    @Override
    public String toString()
    {
        return this.identifier + "的技能配置{" + cooldown + "tick冷却, 技能类型:" + skillIdentifier + "}";
    }
}
