package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DisguiseInfo
{
    @SerializedName("Type")
    @Expose(serialize = false)
    public EntityType type;

    public String rawString;

    public final boolean isPlayerDisguise()
    {
        return type == EntityType.PLAYER;
    }

    public boolean isCustomDisguise()
    {
        return type == EntityType.UNKNOWN;
    }

    @Expose
    public String playerDisguiseTargetName;

    public DisguiseInfo(EntityType type)
    {
        this.type = type;
        this.rawString = type.getKey().asString();
    }

    public DisguiseInfo(@NotNull String rawString, boolean isPlayerDisguise)
    {
        this.rawString = rawString;

        if (isPlayerDisguise)
        {
            this.type = EntityType.PLAYER;
            this.playerDisguiseTargetName = rawString;
        }
        else
            this.type = EntityType.UNKNOWN;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof DisguiseInfo di)) return false;

        if (!this.isValidate()) return false;

        if (!isPlayerDisguise())
            return this.type.equals(di.type);
        else
            return this.type.equals(di.type)
                    && this.playerDisguiseTargetName.equals(di.playerDisguiseTargetName);
    }

    public boolean equals(EntityType type)
    {
        if (!this.isValidate()) return false;

        return this.type.equals(type);
    }

    public boolean equals(String rawString)
    {
        if (!this.isValidate()) return false;

        if (isCustomDisguise()) return this.rawString.equals(rawString);
        else return isPlayerDisguise() && this.playerDisguiseTargetName.equals(rawString);
    }

    public boolean isValidate()
    {
        if (isCustomDisguise()) return rawString != null;
        else return isPlayerDisguise() ? this.playerDisguiseTargetName != null : this.type != null;
    }

    public String getKey()
    {
        if (!this.isValidate()) return "invalid";

        if (isCustomDisguise()) return "ld:" + rawString;
        else return isPlayerDisguise() ? "player:" + playerDisguiseTargetName : type.getKey().asString();
    }

    @Override
    public String toString()
    {
        return "DisguiseInfo[Type=" + this.type + ", isPlayerDisguise=" + this.isPlayerDisguise() + ", targetPlayerName=" + this.playerDisguiseTargetName + "]";
    }
}
