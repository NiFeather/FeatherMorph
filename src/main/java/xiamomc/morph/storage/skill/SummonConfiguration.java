package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import org.bukkit.entity.EntityType;
import xiamomc.morph.misc.EntityTypeUtils;

public class SummonConfiguration
{
    @Expose
    private String name;

    public String getName()
    {
        return name;
    }

    public EntityType getEntityType()
    {
        var type = EntityTypeUtils.fromString(name);

        return type == EntityType.UNKNOWN ? null : type;
    }
}
