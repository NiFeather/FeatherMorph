package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import javax.print.DocFlavor;

public class DisguiseInfo
{
    @SerializedName("Type")
    @Expose(serialize = false)
    @Deprecated
    //仅更新配置时使用
    public EntityType type;

    private final EntityType entityType;

    public EntityType getEntityType()
    {
        return entityType;
    }

    public final String rawString;

    @NotNull
    private final DisguiseTypes disguiseType;

    @NotNull
    public DisguiseTypes getDisguiseType()
    {
        return disguiseType;
    }

    public final boolean isPlayerDisguise()
    {
        return disguiseType == DisguiseTypes.PLAYER;
    }

    public boolean isCustomDisguise()
    {
        return disguiseType == DisguiseTypes.LD;
    }

    /**
     * 不带"player:"的玩家伪装名称
     */
    @Expose
    public String playerDisguiseTargetName;

    public DisguiseInfo(@NotNull String rawString)
    {
        this.rawString = rawString;
        this.disguiseType = DisguiseTypes.fromId(rawString);

        switch (disguiseType)
        {
            case PLAYER ->
            {
                this.entityType = EntityType.PLAYER;
                this.playerDisguiseTargetName = disguiseType.toStrippedId(rawString);
            }
            case VANILLA -> this.entityType = EntityTypeUtils.fromString(rawString);
            default -> this.entityType = EntityType.UNKNOWN;
        }
    }

    @Override
    public int hashCode()
    {
        var str = entityType + rawString + disguiseType;
        return str.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof DisguiseInfo di)) return false;

        return this.equals(di.rawString);
    }

    public boolean equals(EntityType type)
    {
        if (!this.isValidate()) return false;

        return this.entityType.equals(type);
    }

    public boolean equals(String rawString)
    {
        if (!this.isValidate()) return false;

        return this.disguiseType != DisguiseTypes.UNKNOWN && this.rawString.equals(rawString);
    }

    /**
     * SAN值检查
     * @return 是否通过
     */
    public boolean isValidate()
    {
        var typeValid = switch (this.disguiseType)
                {
                    case PLAYER -> this.playerDisguiseTargetName != null;
                    case VANILLA -> this.entityType != null && this.entityType != EntityType.UNKNOWN;
                    case UNKNOWN -> false;
                    default -> true;
                };

        return this.rawString != null && typeValid;
    }

    /**
     * 获取可用于存储的键名
     * @return 键名
     */
    public String getKey()
    {
        if (!this.isValidate())
        {
            LoggerFactory.getLogger("morph").warn("INVALID TYPE FOR:" + rawString);
            return "invalid";
        }

        return rawString;
    }

    /**
     * 将此info转换为可以显示的Component
     * @return Component
     */
    public Component asComponent()
    {
        if (!this.isValidate()) return Component.text("invalid");

        if (disguiseType == DisguiseTypes.VANILLA)
            return Component.translatable(entityType.translationKey());
        else
            return Component.text(disguiseType.toStrippedId(this.rawString));
    }

    @Override
    public String toString()
    {
        return "DisguiseInfo[Type=" + this.entityType + ", DisguiseType=" + this.getDisguiseType() + ", targetPlayerName=" + this.playerDisguiseTargetName + "]";
    }
}
