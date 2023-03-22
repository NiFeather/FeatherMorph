package xiamomc.morph.providers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;

import java.util.List;

public class FallbackProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return "fallback";
    }

    @Override
    public boolean isValid(String rawIdentifier)
    {
        return false;
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        return List.of();
    }

    @Override
    public @NotNull DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        return DisguiseResult.fail();
    }

    @Override
    public boolean canConstruct(DisguiseInfo info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        return false;
    }

    @Override
    protected boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity, @Nullable DisguiseState theirState, @NotNull Disguise theirDisguise)
    {
        return false;
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier, String locale)
    {
        return Component.text("???");
    }
}
