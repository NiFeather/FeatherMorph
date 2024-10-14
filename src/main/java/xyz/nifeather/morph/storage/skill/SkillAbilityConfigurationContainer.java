package xyz.nifeather.morph.storage.skill;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class SkillAbilityConfigurationContainer
{
    @Expose
    public List<SkillAbilityConfiguration> configurations = new ObjectArrayList<>();

    @Expose
    public int version;
}
