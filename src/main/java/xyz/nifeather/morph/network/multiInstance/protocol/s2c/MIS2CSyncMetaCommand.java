package xyz.nifeather.morph.network.multiInstance.protocol.s2c;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xyz.nifeather.morph.network.multiInstance.protocol.IMasterHandler;
import xyz.nifeather.morph.network.multiInstance.protocol.SocketPlayerMeta;
import xyz.nifeather.morph.network.utils.Asserts;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MIS2CSyncMetaCommand extends MIS2CCommand
{
    private final MetaSyncDataRoot data = new MetaSyncDataRoot();

    public List<SocketPlayerMeta> data()
    {
        return data.content;
    }

    public void appendMeta(SocketPlayerMeta meta)
    {
        data.content.add(meta);
    }

    public MIS2CSyncMetaCommand()
    {
        super("sync_player_meta");
    }

    public MIS2CSyncMetaCommand(MetaSyncDataRoot otherData)
    {
        this();

        data.content.addAll(otherData.content);
    }

    public MIS2CSyncMetaCommand(List<SocketPlayerMeta> metaList)
    {
        this();

        this.data.content.addAll(metaList);
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        var str = gson().toJson(this.data);

        var map = new ConcurrentHashMap<String, String>();

        map.put("meta_list", str);

        return map;
    }

    public static MIS2CSyncMetaCommand fromArguments(Map<String, String> arguments)
    {
        var metaList = Asserts.getStringOrThrow(arguments, "meta_list");

        var result = gson().fromJson(metaList, MetaSyncDataRoot.class);

        return new MIS2CSyncMetaCommand(result);
    }

    public static class MetaSyncDataRoot
    {
        @Expose
        @SerializedName("content")
        public List<SocketPlayerMeta> content = new ObjectArrayList<>();
    }

    @Override
    public void onCommand(IMasterHandler handler)
    {
        handler.onSyncMeta(this);
    }
}
