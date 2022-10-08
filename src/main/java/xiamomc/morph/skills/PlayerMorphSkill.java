package xiamomc.morph.skills;

import org.bukkit.entity.EntityType;

public class PlayerMorphSkill extends InventoryMorphSkill
{
    @Override
    public EntityType getType()
    {
        return EntityType.PLAYER;
    }
}
