package xiamomc.morph.skills.configurations;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class SkillConfigurationContainer
{
    @Expose
    public List<SkillConfiguration> configurations = new ArrayList<>();

    @Expose
    public int version;
}
