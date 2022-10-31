package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.skills.SkillType;

import java.util.Map;

public class ExplosionConfiguration implements ISkillOption
{
    public ExplosionConfiguration()
    {
    }

    public ExplosionConfiguration(boolean killsSelf, int strength, boolean setsFire)
    {
        this.killsSelf = killsSelf;
        this.strength = strength;
        this.setsFire = setsFire;
    }

    @Expose
    private boolean killsSelf;

    public boolean killsSelf()
    {
        return killsSelf;
    }

    @Expose
    private int strength;

    public int getStrength()
    {
        return strength;
    }

    @Expose
    private boolean setsFire;

    public boolean setsFire()
    {
        return setsFire;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("kills_self", killsSelf);
        map.put("strength", strength);
        map.put("sets_fire", setsFire);

        return map;
    }

    @Override
    public ExplosionConfiguration fromMap(Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new ExplosionConfiguration();

        instance.killsSelf = tryGet(map, "kills_self", true);
        instance.strength = tryGetInt(map, "strength", 3);
        instance.setsFire = tryGet(map, "sets_fire", false);

        return instance;
    }
}
