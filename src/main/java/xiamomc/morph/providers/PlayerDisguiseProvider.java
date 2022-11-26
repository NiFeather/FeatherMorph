package xiamomc.morph.providers;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.MorphGameProfile;

import java.util.List;

public class PlayerDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getIdentifier()
    {
        return DisguiseTypes.PLAYER.getNameSpace();
    }

    @Override
    public @NotNull DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var id = disguiseInfo.getIdentifier();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.PLAYER)
            return DisguiseResult.fail();

        var result = getCopy(disguiseInfo, targetEntity);
        var disguise = result.success() ? result.disguise() : new PlayerDisguise(disguiseInfo.playerDisguiseTargetName);

        DisguiseAPI.disguiseEntity(player, disguise);

        return DisguiseResult.success(disguise, result.isCopy());
    }

    @Override
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        super.postConstructDisguise(state, targetEntity);

        var mainHandItem = state.getPlayer().getItemInHand();

        if (mainHandItem.getType() != Material.PLAYER_HEAD) return;

        var profile = ((SkullMeta) mainHandItem.getItemMeta()).getPlayerProfile();
        var player = state.getPlayer();

        if (profile == null)
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidSkinString()));
            return;
        }

        //成功伪装后设置皮肤为头颅的皮肤
        var disguise = (PlayerDisguise) state.getDisguise();
        var wrappedProfile = WrappedGameProfile.fromHandle(new MorphGameProfile(profile));

        var LDprofile = ReflectionManager.getGameProfileWithThisSkin(wrappedProfile.getUUID(), wrappedProfile.getName(), wrappedProfile);

        //LD不支持直接用profile设置皮肤，只能先存到本地设置完再移除
        DisguiseAPI.addGameProfile(LDprofile.toString(), LDprofile);
        disguise.setSkin(LDprofile);
        DisguiseUtilities.removeGameProfile(LDprofile.toString());
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        var onlinePlayers = Bukkit.getOnlinePlayers();

        var list = new ObjectArrayList<String>();
        onlinePlayers.forEach(p -> list.add(DisguiseTypes.PLAYER.toId(p.getName())));

        return list;
    }

    @Override
    public boolean canConstruct(DisguiseInfo info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        if (theirState != null)
        {
            var type = DisguiseTypes.fromId(theirState.getDisguiseIdentifier());

            return type == DisguiseTypes.PLAYER
                    && type.toStrippedId(theirState.getDisguiseIdentifier()).equals(info.playerDisguiseTargetName);
        }

        if (!(targetEntity instanceof Player targetPlayer))
            return false;

        return targetPlayer.getName().equals(info.playerDisguiseTargetName);
    }

    @Override
    protected boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                      @Nullable DisguiseState theirState, @NotNull Disguise theirDisguise)
    {
        if (theirDisguise instanceof PlayerDisguise playerDisguise)
            return playerDisguise.getName().equals(info.playerDisguiseTargetName);

        return false;
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return true;
    }

    @Override
    public @Nullable String getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        if (!(targetEntity instanceof Player targetPlayer)) return null;

        if (!targetPlayer.getName().equals(DisguiseTypes.PLAYER.toStrippedId(state.getDisguiseIdentifier()))) return null;

        return super.getNbtCompound(state, targetEntity);
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        return super.unMorph(player, state);
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier)
    {
        //尝试获取玩家的显示名称
        Component finalName;
        var playerName = DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier);
        var player = Bukkit.getPlayer(playerName);

        if (player != null)
            finalName = player.displayName();
        else
            finalName = Component.text(playerName);

        return finalName;
    }
}
