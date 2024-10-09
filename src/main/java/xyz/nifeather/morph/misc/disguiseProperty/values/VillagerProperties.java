package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Registry;
import org.bukkit.entity.Villager;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

public class VillagerProperties extends AbstractProperties
{
    public final SingleProperty<Villager.Type> TYPE = getSingle("villager_type", Villager.Type.PLAINS)
            .withRandom(Registry.VILLAGER_TYPE.stream().toList());

    public final SingleProperty<Villager.Profession> PROFESSION = getSingle("villager_profession", Villager.Profession.NONE)
            .withRandom(Registry.VILLAGER_PROFESSION.stream().toList());

    public final SingleProperty<Integer> LEVEL = getSingle("villager_level", 1)
            .withRandom(1, 2, 3, 4, 5, 6);

    public VillagerProperties()
    {
        registerSingle(TYPE, PROFESSION, LEVEL);
    }
}
