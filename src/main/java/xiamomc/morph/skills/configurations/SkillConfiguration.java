package xiamomc.morph.skills.configurations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.skills.SkillType;

public class SkillConfiguration
{
    public SkillConfiguration()
    {
    }

    public SkillConfiguration(String mobId, int cd, SkillType type)
    {
        this.mobIdentifier = mobId;
        this.cooldown = cd;
        this.skillIdentifier = type;
    }

    public SkillConfiguration(EntityType type, int cd, SkillType skillType)
    {
        this(type.getKey().asString(), cd, skillType);
    }

    @Expose
    @SerializedName("mobId")
    private String mobIdentifier;

    @Nullable
    public EntityType getEntityType()
    {
        var type = EntityTypeUtils.fromString(mobIdentifier);

        return type == EntityType.UNKNOWN ? null : type;
    }

    @Expose
    @SerializedName("skillCooldown")
    private int cooldown;

    public int getCooldown()
    {
        return cooldown;
    }

    @Expose
    @SerializedName("skillID")
    private SkillType skillIdentifier;

    public SkillType getSkillType()
    {
        return skillIdentifier;
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
        return "生物" + this.mobIdentifier + "的技能配置{" + cooldown + "tick冷却, 技能类型:" + skillIdentifier + "}";
    }
}
