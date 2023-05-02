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


    /**
     * Gets the identifier of this backend.
     *
     * @return An identifier of this backend.
     */
    @Override
    public String getIdentifier()
    {
        return "ld";
    }

    @Override
    public DisguiseWrapper<Disguise> createInstance(@NotNull Entity targetEntity)
    {
        var instance = DisguiseAPI.constructDisguise(targetEntity);
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance, this);
    }

    @Override
    public DisguiseWrapper<Disguise> createInstance(EntityType entityType)
    {
        var instance = new MobDisguise(DisguiseType.getType(entityType));
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance, this);
    }

    @Override
    public DisguiseWrapper<Disguise> createPlayerInstance(String playerName)
    {
        var instance = new PlayerDisguise(playerName);
        //DisguiseAPI.disguiseEntity(player, instance);

        return new LibsDisguiseWrapper(instance, this);
    }

    public DisguiseWrapper<Disguise> createInstanceDirect(Disguise disguise)
    {
        return new LibsDisguiseWrapper(disguise, this);
    }

    @Override
    public Disguise createRawInstance(Entity entity)
    {
        return null;
    }

    @Override
    public boolean isDisguised(Entity target)
    {
        return playerLibsDisguiseWrapperMap.containsKey(target);
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

    /**
     * Deserialize a wrapper instance from the giving parameter
     *
     * @param offlineParameter The parameter to deserialize
     * @return A wrapper that presents the giving parameter.
     * null if invalid or illegal
     */
    @Override
    public @Nullable LibsDisguiseWrapper fromOfflineSave(String offlineParameter)
    {
        var strSpilt = offlineParameter.split("\\|", 2);

        if (strSpilt.length < 2) return null;

        if (!strSpilt[0].equals(getIdentifier()))
        {
            logger.info("Trying to deserialize a empty save: '%s'".formatted(offlineParameter));
            return null;
        }

        try
        {
            var disg = DisguiseParser.parseDisguise(strSpilt[1]);
            return new LibsDisguiseWrapper(disg, this);
        }
        catch (Throwable t)
        {
            logger.error("Unable to parse from offline string: %s".formatted(t.getMessage()));
            t.printStackTrace();

            return null;
        }
    }

    /**
     * Serialize a wrapper instance to a string that can be saved in the Offline Storage
     *
     * @param wrapper The target wrapper to save
     * @return A serialized string that can be deserialized to a wrapper in the future.
     * Null if the giving wrapper is not supported by this backend.
     */
    @Override
    public @Nullable String toOfflineSave(DisguiseWrapper<?> wrapper)
    {
        if (!(wrapper instanceof LibsDisguiseWrapper libsDisguiseWrapper)) return null;

        var disguise = libsDisguiseWrapper.getInstance();

        return DisguiseParser.parseToString(disguise);
    }
}
