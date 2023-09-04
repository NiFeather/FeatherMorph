package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.providers.DisguiseProvider;
import xiamomc.morph.utilities.EntityTypeUtils;

public class DisguiseMeta
{
    @SerializedName("Type")
    @Expose(serialize = false)
    @Deprecated
    //仅更新配置时使用
    public EntityType type;

    private final EntityType entityType;

    @ApiStatus.Internal
    public EntityType getEntityType()
    {
        return entityType;
    }

    @NotNull
    public final String rawIdentifier;

    public String getIdentifier()
    {
        return rawIdentifier;
    }

    @NotNull
    private final DisguiseTypes disguiseType;

    @Nullable
    private final DisguiseProvider provider;

    @Nullable
    public DisguiseProvider getProvider()
    {
        return provider;
    }

    /**
     * 获取伪装类型（玩家、生物、LD或未知）
     *
     * @return 伪装类型
     */
    @NotNull
    public DisguiseTypes getDisguiseType()
    {
        return disguiseType;
    }

    public final boolean isPlayerDisguise()
    {
        return disguiseType == DisguiseTypes.PLAYER;
    }

    public boolean isLocalDisguise()
    {
        return disguiseType == DisguiseTypes.LD;
    }

    /**
     * 不带"player:"的玩家伪装名称
     */
    @Expose
    public String playerDisguiseTargetName;

    public DisguiseMeta(@NotNull String rawIdentifier, DisguiseTypes disguiseType)
    {
        this.rawIdentifier = rawIdentifier;
        this.disguiseType = disguiseType;

        this.provider = MorphManager.getProvider(rawIdentifier);

        switch (disguiseType)
        {
            case PLAYER ->
            {
                this.entityType = EntityType.PLAYER;
                this.playerDisguiseTargetName = disguiseType.toStrippedId(rawIdentifier);
            }
            case VANILLA -> this.entityType = EntityTypeUtils.fromString(rawIdentifier);
            default -> this.entityType = EntityType.UNKNOWN;
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof DisguiseMeta di)) return false;

        return this.equals(di.rawIdentifier);
    }

    public boolean equals(EntityType type)
    {
        if (!this.isValid()) return false;

        return this.entityType.equals(type);
    }

    public boolean equals(String rawString)
    {
        if (!this.isValid()) return false;

        return this.disguiseType != DisguiseTypes.UNKNOWN && this.rawIdentifier.equals(rawString);
    }

    /**
     * SAN值检查
     * @return 是否通过
     */
    public boolean isValid()
    {
        return disguiseType != DisguiseTypes.UNKNOWN;
    }

    /**
     * 获取可用于存储的键名
     * @return 键名
     */
    public String getKey()
    {
        if (!this.isValid())
            return rawIdentifier;

        return rawIdentifier;
    }

    /**
     * 将此info转换为可以显示的Component
     * @return Component
     * @deprecated 请使用 {@link DisguiseMeta#asComponent(String)}
     */
    @Deprecated
    public Component asComponent()
    {
        return isValid()
                    ? provider == null
                        ? Component.text(rawIdentifier)
                        : provider.getDisplayName(rawIdentifier, null)
                    : Component.text(rawIdentifier);
    }

    public Component asComponent(String locale)
    {
        return isValid()
                    ? provider == null
                        ? Component.text(rawIdentifier)
                        : provider.getDisplayName(rawIdentifier, locale)
                    : Component.text(rawIdentifier);
    }

    @Override
    public String toString()
    {
        return "DisguiseMeta[Type=" + this.entityType + ", DisguiseType=" + this.getDisguiseType() + ", targetPlayerName=" + this.playerDisguiseTargetName + "]";
    }
}
