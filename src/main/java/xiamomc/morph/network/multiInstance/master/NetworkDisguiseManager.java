package xiamomc.morph.network.multiInstance.master;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.OfflinePlayer;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.storage.playerdata.PlayerMeta;

import java.util.List;

public class NetworkDisguiseManager extends MorphPluginObject
{
    private final List<PlayerMeta> storedMeta = new ObjectArrayList<>();

    public List<PlayerMeta> listAllMeta()
    {
        return new ObjectArrayList<>(storedMeta);
    }

    public PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        var match = storedMeta.stream()
                .filter(m -> m.uniqueId.equals(player.getUniqueId()))
                .findFirst().orElse(null);

        if (match != null) return match;

        var newInstance = new PlayerMeta();
        newInstance.uniqueId = player.getUniqueId();
        storedMeta.add(newInstance);

        return newInstance;
    }

    private final List<DisguiseMeta> cachedMetas = new ObjectArrayList<>();

    public DisguiseMeta getDisguiseMeta(String rawString)
    {
        var type = DisguiseTypes.fromId(rawString);

        if (this.cachedMetas.stream().noneMatch(o -> o.equals(rawString)))
            cachedMetas.add(new DisguiseMeta(rawString, type));

        return cachedMetas.stream().filter(o -> o.equals(rawString)).findFirst().orElse(null);
    }
}
