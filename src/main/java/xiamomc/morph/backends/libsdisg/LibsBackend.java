package xiamomc.morph.backends.libsdisg;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.parser.DisguiseParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;

import java.util.Map;

public class LibsBackend extends DisguiseBackend<Disguise, LibsDisguiseWrapper>
{
    public LibsBackend() throws NoClassDefFoundError
    {
    }


    @Override
    public DisguiseWrapper<Disguise> createInstance(@NotNull Entity targetEntity)
    {
        var instance = DisguiseAPI.constructDisguise(targetEntity);
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance);
    }

    @Override
    public DisguiseWrapper<Disguise> createInstance(EntityType entityType)
    {
        var instance = new MobDisguise(DisguiseType.getType(entityType));
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance);
    }

    @Override
    public DisguiseWrapper<Disguise> createPlayerInstance(String playerName)
    {
        var instance = new PlayerDisguise(playerName);
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance);
    }

    @Override
    public DisguiseWrapper<Disguise> fromOfflineString(String offlineStr)
    {
        try
        {
            var disg = DisguiseParser.parseDisguise(offlineStr);
            return new LibsDisguiseWrapper(disg);
        }
        catch (Throwable t)
        {
            logger.error("Unable to parse from offline string: %s".formatted(t.getMessage()));
            t.printStackTrace();

            return null;
        }
    }

    public DisguiseWrapper<Disguise> createInstanceDirect(Disguise disguise)
    {
        return new LibsDisguiseWrapper(disguise);
    }

    @Override
    public Disguise createRawInstance(Entity entity)
    {
        return null;
    }

    @Override
    public boolean isDisguised(Entity target)
    {
        return DisguiseAPI.isDisguised(target);
    }

    private final Map<Player, LibsDisguiseWrapper> playerLibsDisguiseWrapperMap = new Object2ObjectOpenHashMap<>();

    @Override
    public LibsDisguiseWrapper getDisguise(Entity target)
    {
        if (!(target instanceof Player player)) return null;

        return playerLibsDisguiseWrapperMap.getOrDefault(player, null);
    }

    @Override
    public boolean disguise(Player player, DisguiseWrapper<?> rawWrapper)
    {
        if (!(rawWrapper instanceof LibsDisguiseWrapper wrapper))
            return false;

        try
        {
            playerLibsDisguiseWrapperMap.put(player, wrapper);
            DisguiseAPI.disguiseEntity(player, wrapper.getInstance());

            if (wrapper.getEntityType().equals(EntityType.BAT))
                wrapper.getInstance().getWatcher().setYModifier(-1.6f);

            return true;
        }
        catch (Throwable t)
        {
            logger.warn("Unable to disguise player: %s".formatted(t.getMessage()));
            t.printStackTrace();

            return false;
        }
    }

    @Override
    public boolean unDisguise(Player player)
    {
        var wrapper = playerLibsDisguiseWrapperMap.getOrDefault(player, null);

        if (wrapper != null)
        {
            playerLibsDisguiseWrapperMap.remove(player);
            return wrapper.getInstance().removeDisguise();
        }

        return true;
    }
}
