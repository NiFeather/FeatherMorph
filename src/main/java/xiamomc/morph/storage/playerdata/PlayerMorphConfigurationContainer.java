package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;

public class PlayerMorphConfigurationContainer
{
    @Expose
    public List<PlayerMorphConfiguration> playerMorphConfigurations = new ObjectArrayList<>();

    @Expose
    public int Version;
}
