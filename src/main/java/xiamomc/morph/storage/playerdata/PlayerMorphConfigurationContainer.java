package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class PlayerMorphConfigurationContainer
{
    @Expose
    public ArrayList<PlayerMorphConfiguration> playerMorphConfigurations = new ArrayList<>();

    @Expose
    public int Version;
}
