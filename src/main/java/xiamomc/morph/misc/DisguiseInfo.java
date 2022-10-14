package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;

public class DisguiseInfo
{
    @SerializedName("Type")
    @Expose
    public EntityType type;

    public final boolean isPlayerDisguise()
    {
        return type == EntityType.PLAYER;
    }

    @Expose
    public String playerDisguiseTargetName;

    public DisguiseInfo(EntityType type)
    {
        this.type = type;
    }

    public DisguiseInfo(String playerName)
    {
        this.type = EntityType.PLAYER;
        this.playerDisguiseTargetName = playerName;
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

    public boolean equals(String playerName)
    {
        if (!this.isValidate()) return false;

        return this.isPlayerDisguise() && this.playerDisguiseTargetName.equals(playerName);
    }

    public boolean isValidate()
    {
        if (!this.isPlayerDisguise()) return this.type != null;
        else return this.playerDisguiseTargetName != null;
    }

    public String getKey()
    {
        return isPlayerDisguise() ? "player:" + playerDisguiseTargetName : type.getKey().asString();
    }
}
