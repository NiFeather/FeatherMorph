package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.*;

public class VanillaDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getIdentifier()
    {
        return DisguiseTypes.VANILLA.getNameSpace();
    }

    @Override
    @NotNull
    public DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var identifier = disguiseInfo.getIdentifier();

        Disguise constructedDisguise;

        var entityType = EntityTypeUtils.fromString(identifier, true);

        if (entityType == null || entityType == EntityType.PLAYER || !entityType.isAlive())
        {
            logger.error("无效的生物类型: " + identifier + "(" + entityType + ")");
            return DisguiseResult.fail();
        }

        var copyResult = getCopy(disguiseInfo, targetEntity);

        constructedDisguise = copyResult.success()
                ? copyResult.disguise()
                : new MobDisguise(DisguiseType.getType(entityType));

        DisguiseAPI.disguiseEntity(player, constructedDisguise);

        return DisguiseResult.success(constructedDisguise, copyResult.isCopy());
    }

    @Override
    protected boolean canConstruct(DisguiseInfo info, Entity targetEntity, DisguiseState state)
    {
        return state != null
                ? state.getDisguise().getType().getEntityType().equals(info.getEntityType())
                : targetEntity.getType().equals(info.getEntityType());
    }

    @Override
    protected boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                      @Nullable DisguiseState theirDisguiseState, @NotNull Disguise theirDisguise)
    {
        return theirDisguise.getType().getEntityType().equals(info.getEntityType());
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier)
    {
        var type = EntityTypeUtils.fromString(disguiseIdentifier, true);

        if (type == null)
            return Component.text("???");
        else
            return MinecraftLanguageHelper.getComponent(type.translationKey());
    }
}
