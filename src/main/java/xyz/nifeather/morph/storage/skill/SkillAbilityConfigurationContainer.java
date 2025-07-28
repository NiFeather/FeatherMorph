package xyz.nifeather.morph.storage.skill;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;

public class SkillAbilityConfigurationContainer
{
    @Expose
    public List<SkillAbilityConfiguration> configurations = Collections.synchronizedList(new ObjectArrayList<>());

    @Expose
    public int version;
}
