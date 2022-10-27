package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.pluginbase.Annotations.Initializer;

public class LibsDisguisesDisguiseProvider extends VanillaDisguiseProvider
{
    private boolean allowLD;

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.onConfigRefresh(c ->
        {
            allowLD = configManager.get(Boolean.class, ConfigOption.ALLOW_LD_DISGUISES);
        }, true);
    }

    @Override
    public @NotNull String getIdentifier()
    {
        return DisguiseTypes.LD.getNameSpace();
    }

    @Override
    public @NotNull DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        if (!allowLD)
        {
            return DisguiseResult.fail();
        }

        var id = disguiseInfo.getIdentifier();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.LD)
            return DisguiseResult.fail();

        Disguise disguise = DisguiseAPI.getCustomDisguise(DisguiseTypes.LD.toStrippedId(id));

        if (disguise == null)
        {
            logger.error("未能找到叫" + id + "的伪装");
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.parseErrorString().resolve("id", id)));
            return DisguiseResult.fail();
        }

        DisguiseAPI.disguiseEntity(player, disguise);

        return DisguiseResult.success(disguise);
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        super.unMorph(player, state);

        return false;
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier)
    {
        return Component.text(DisguiseTypes.LD.toStrippedId(disguiseIdentifier));
    }
}
