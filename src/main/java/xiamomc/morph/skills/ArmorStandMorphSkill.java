package xiamomc.morph.skills;

import org.bukkit.entity.EntityType;

public class ArmorStandMorphSkill extends InventoryMorphSkill
{
    @Override
    public EntityType getType()
    {
        return EntityType.ARMOR_STAND;
    }
}
