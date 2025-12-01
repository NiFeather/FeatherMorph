package xyz.nifeather.morph.network.multiInstance.slave;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.List;
import java.util.UUID;

public class VoidDataHolder implements IManagePlayerData
{
    @Override
    public @Nullable DisguiseMeta getDisguiseMeta(String rawString)
    {
        return null;
    }

    @Override
    public ObjectArrayList<DisguiseMeta> getAvaliableDisguisesFor(Player player)
    {
        return new ObjectArrayList<>();
    }

    @Override
    public boolean grantMorphToPlayer(Player player, String disguiseIdentifier)
    {
        return false;
    }

    @Override
    public boolean revokeMorphFromPlayer(Player player, String disguiseIdentifier)
    {
        return false;
    }

    @Override
    public @NotNull PlayerMeta getPlayerMeta(OfflinePlayer player)
    {
        return new PlayerMeta();
    }

    @Override
    public boolean reloadConfiguration()
    {
        return false;
    }

    @Override
    public boolean saveConfiguration()
    {
        return false;
    }

    @Override
    public void shouldLoadAllData(boolean shouldLoadAllData)
    {
    }

    @Override
    public List<PlayerMeta> getRange(List<UUID> list)
    {
        return List.of();
    }

    @Override
    public List<PlayerMeta> listAll()
    {
        return List.of();
    }
}
