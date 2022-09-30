package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;
import xiamomc.pluginbase.Annotations.NotSerializable;
import xiamomc.pluginbase.Annotations.Serializable;

public class DisguiseInfo
{
    @Serializable("Type")
    @SerializedName("Type")
    public EntityType type;

    @NotSerializable
    @Expose(deserialize = false, serialize = false)
    public boolean isPlayerDisguise;

    @Serializable("TargetPlayerName")
    public String playerDisguiseTargetName;

    private DisguiseInfo()
    {
    }

    public DisguiseInfo(EntityType type)
    {
        this.type = type;

        this.isPlayerDisguise = type.equals(EntityType.PLAYER);
    }

    public DisguiseInfo(String playerName)
    {
        this.type = EntityType.PLAYER;
        this.isPlayerDisguise = true;
        this.playerDisguiseTargetName = playerName;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof DisguiseInfo di)) return false;

        if (!this.isValidate()) return false;

        if (!isPlayerDisguise)
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

        return this.isPlayerDisguise && this.playerDisguiseTargetName.equals(playerName);
    }

    public boolean isValidate()
    {
        if (!this.isPlayerDisguise) return this.type != null;
        else return this.type != null && this.playerDisguiseTargetName != null;
    }
}
