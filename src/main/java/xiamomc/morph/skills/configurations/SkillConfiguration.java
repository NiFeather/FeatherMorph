package xiamomc.morph.skills.configurations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.EntityType;
import org.checkerframework.checker.units.qual.K;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.skills.SkillType;

public class SkillConfiguration
{
    public SkillConfiguration()
    {
    }

    public SkillConfiguration(String mobId, int cd, Key type)
    {
        this.mobIdentifier = mobId;
        this.cooldown = cd;
        setSkillType(type);
    }

    public SkillConfiguration(EntityType type, int cd, Key skillType)
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
    @SerializedName("skillId")
    private String rawSkillidentifier;

    @Expose(deserialize = false, serialize = false)
    private Key skillIdentifier;

    public Key getSkillType()
    {
        if (skillIdentifier == null)
            skillIdentifier = rawSkillidentifier == null ? SkillType.UNKNOWN : Key.key(rawSkillidentifier);

        return skillIdentifier;
    }

    private void setSkillType(Key key)
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
        return "生物" + this.mobIdentifier + "的技能配置{" + cooldown + "tick冷却, 技能类型:" + skillIdentifier + "}";
    }
}
