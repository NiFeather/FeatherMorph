package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;

public class SkillConfigurationContainer
{
    @Expose
    public List<SkillConfiguration> configurations = new ObjectArrayList<>();

    @Expose
    public int version;
}
