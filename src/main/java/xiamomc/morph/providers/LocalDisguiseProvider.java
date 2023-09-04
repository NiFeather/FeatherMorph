package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.libsdisg.LibsBackend;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;

public class LocalDisguiseProvider extends VanillaDisguiseProvider
{
    private final Bindable<Boolean> allowLD = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(allowLD, ConfigOption.ALLOW_LD_DISGUISES);
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        var list = new ObjectArrayList<String>();

        DisguiseConfig.getCustomDisguises().forEach((p, s) ->
        {
            list.add(DisguiseTypes.LD.toId(p.toReadable()));
        });

        return list;
    }

    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.LD.getNameSpace();
    }

    @Override
    public @NotNull DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        if (!allowLD.get())
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
            return DisguiseResult.fail();
        }

        var id = disguiseMeta.getIdentifier();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.LD)
            return DisguiseResult.fail();

        Disguise disguise = DisguiseAPI.getCustomDisguise(DisguiseTypes.LD.toStrippedId(id));

        if (disguise == null)
        {
            logger.error("未能找到叫" + id + "的伪装");
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.noSuchLocalDisguiseString().resolve("id", id)));
            return DisguiseResult.fail();
        }

        DisguiseAPI.disguiseEntity(player, disguise);

        var ldBackend = new LibsBackend();
        return DisguiseResult.success(ldBackend.createInstanceDirect(disguise));
    }

    @Override
    public boolean canConstruct(DisguiseMeta info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        return theirState != null && theirState.getDisguiseIdentifier().equals(info.getIdentifier());
    }

    @Override
    protected boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity,
                                       @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise)
    {
        if (theirState != null)
            return theirState.getDisguiseIdentifier().equals(info.getIdentifier());

        return false;
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        super.unMorph(player, state);

        return false;
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier, String locale)
    {
        var ldID = DisguiseTypes.LD.toStrippedId(disguiseIdentifier);

        var disg = DisguiseAPI.getCustomDisguise(ldID);

        if (disg != null)
            return Component.text(disg.getType().toReadable().equals(disg.getDisguiseName()) ? ldID : disg.getDisguiseName());

        return Component.text(ldID);
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return false;
    }

    @Override
    public String getSelfViewIdentifier(DisguiseState state)
    {
        return "";
    }
}
