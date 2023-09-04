package xiamomc.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class PlayerMetaContainer
{
    @Expose
    public List<PlayerMeta> playerMetas = new ObjectArrayList<>();

    @Expose
    public int Version;
}
