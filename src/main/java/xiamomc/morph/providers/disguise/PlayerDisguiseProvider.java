package xiamomc.morph.providers.disguise;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.WrapperEvent;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.misc.skins.PlayerSkinProvider;
import xiamomc.morph.network.commands.S2C.set.S2CSetProfileCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.providers.animation.AnimationProvider;
import xiamomc.morph.providers.animation.provider.PlayerAnimationProvider;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.Objects;

public class PlayerDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.PLAYER.getNameSpace();
    }

    private final PlayerAnimationProvider animationProvider = new PlayerAnimationProvider();

    /**
     * 获取此DisguiseProvider的动画提供器
     */
    @Override
    public AnimationProvider getAnimationProvider()
    {
        return animationProvider;
    }

    @Override
    public boolean isValid(String rawIdentifier)
    {
        return DisguiseTypes.fromId(rawIdentifier) == DisguiseTypes.PLAYER;
    }

    @Override
    public boolean allowSwitchingWithoutUndisguise(DisguiseProvider other, DisguiseMeta meta)
    {
        return other.getPreferredBackend() == this.getPreferredBackend()
                && (meta.getDisguiseType() == DisguiseTypes.VANILLA || meta.getDisguiseType() == DisguiseTypes.PLAYER);
    }

    @Override
    public @NotNull DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        if (getMorphManager().getBannedDisguises().contains("minecraft:player"))
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseBannedOrNotSupportedString()));
            return DisguiseResult.fail();
        }

        var id = disguiseMeta.getIdentifier();
        var backend = getPreferredBackend();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.PLAYER)
            return DisguiseResult.fail();

        var result = constructFromEntity(disguiseMeta, targetEntity);
        var wrapper = result.success()
                ? result.wrapperInstance()
                : backend.createPlayerInstance(disguiseMeta.playerDisguiseTargetName);

        Objects.requireNonNull(wrapper, "Null wrapper at where it shouldn't be?!");

        // Ensure profile is always present
        var skin = PlayerSkinProvider.getInstance().getCachedProfile(DisguiseTypes.PLAYER.toStrippedId(id));
        wrapper.applySkin(skin == null ? new GameProfile(Util.NIL_UUID, DisguiseTypes.PLAYER.toStrippedId(id)) : skin);

        var mainHandItem = player.getEquipment().getItemInMainHand();

        //存在玩家头颅，尝试通过头颅获取目标皮肤
        if (mainHandItem.getType() == Material.PLAYER_HEAD)
        {
            var gameProfile = getGameProfile(mainHandItem);

            if (gameProfile == null)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidSkinString()));
                return DisguiseResult.fail();
            }

            //如果玩家头和目标伪装ID一致，那么设置伪装皮肤
            if (gameProfile.getName().equals(DisguiseTypes.PLAYER.toStrippedId(id)))
                wrapper.applySkin(gameProfile);
        }

        return DisguiseResult.success(wrapper, result.isCopy());
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @Override
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
        super.postConstructDisguise(state, targetEntity);

        var wrapper = state.getDisguiseWrapper();
        var player = state.getPlayer();

        wrapper.subscribeEvent(this, WrapperEvent.SKIN_SET, skin ->
                clientHandler.sendCommand(player, new S2CSetProfileCommand(state.getProfileNbtString())));

        var wrapperSkin = wrapper.getSkin();
        if (wrapperSkin == null || wrapperSkin.getId().equals(Util.NIL_UUID))
        {
            var playerDisguiseTargetName = DisguiseTypes.PLAYER.toStrippedId(state.getDisguiseIdentifier());

            PlayerSkinProvider.getInstance().fetchSkin(playerDisguiseTargetName)
                    .thenAccept(optional ->
                    {
                        if (wrapper.disposed()) return;

                        GameProfile outcomingProfile = new GameProfile(Util.NIL_UUID, playerDisguiseTargetName);
                        if (optional.isPresent()) outcomingProfile = optional.get();

                        GameProfile finalOutcomingProfile = outcomingProfile;
                        this.scheduleOn(player, () -> wrapper.applySkin(finalOutcomingProfile));
                    });
        }

    }

    private MorphGameProfile getGameProfile(ItemStack item)
    {
        if (item.getType() != Material.PLAYER_HEAD) return null;

        var profile = ((SkullMeta) item.getItemMeta()).getPlayerProfile();
        if (profile == null) return null;

        return new MorphGameProfile(profile);
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        var onlinePlayers = Bukkit.getOnlinePlayers();

        var list = new ObjectArrayList<String>();
        onlinePlayers.forEach(p -> list.add(p.getName()));

        return list;
    }

    @Override
    public boolean canConstruct(DisguiseMeta info, Entity targetEntity, @Nullable DisguiseState theirState)
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

    /**
     * 我们是否可以克隆目标实体/玩家的伪装？
     *
     * @param info         {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState   他们的{@link DisguiseState}，如果有
     * @return 是否允许克隆他们的装备进行显示
     */
    @Override
    public boolean canCloneEquipment(DisguiseMeta info, Entity targetEntity, DisguiseState theirState)
    {
        return canConstruct(info, targetEntity, theirState);
    }

    @Override
    protected boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity,
                                       @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise)
    {
        return theirDisguise.getDisguiseName().equals(info.playerDisguiseTargetName) && theirDisguise.isPlayerDisguise();
    }

    @Override
    public boolean validForClient(DisguiseState state)
    {
        return true;
    }

    @Override
    public @Nullable CompoundTag getInitialNbtCompound(DisguiseState state, Entity targetEntity, boolean enableCulling)
    {
        if (!(targetEntity instanceof Player targetPlayer)) return null;

        if (!targetPlayer.getName().equals(DisguiseTypes.PLAYER.toStrippedId(state.getDisguiseIdentifier()))) return null;

        return super.getInitialNbtCompound(state, targetEntity, enableCulling);
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        return super.unMorph(player, state);
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier, String locale)
    {
        //尝试获取玩家的显示名称
        Component finalName;
        var playerName = DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier);
        var player = Bukkit.getPlayerExact(playerName);

        if (player != null)
            finalName = player.displayName();
        else
            finalName = Component.text(playerName);

        return finalName;
    }
}
