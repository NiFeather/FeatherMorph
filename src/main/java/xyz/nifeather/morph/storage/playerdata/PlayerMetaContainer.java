package xyz.nifeather.morph.storage.playerdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerMetaContainer
{
    @Expose
    @SerializedName("playerMorphConfigurations")
    public List<PlayerMeta> playerMetas = new CopyOnWriteArrayList<>();

    @Expose
    public int Version;
}
