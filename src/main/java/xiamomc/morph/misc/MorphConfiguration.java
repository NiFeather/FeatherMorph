package xiamomc.morph.misc;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class MorphConfiguration
{
    @Expose
    public ArrayList<PlayerMorphConfiguration> playerMorphConfigurations = new ArrayList<>();

    @Expose
    public int Version;
}
