package xyz.nifeather.morph.providers.disguise;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.values.PlayerPropertyCollection;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.providers.animation.AnimationProvider;
import xyz.nifeather.morph.providers.animation.provider.PlayerAnimationProvider;
import xyz.nifeather.morph.utilities.GameProfileUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public @NotNull Optional<DisguiseWrapper<?>> makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        if (getMorphManager().getBannedDisguises().contains("minecraft:player"))
        {
            MessageUtils.send(player, MorphStrings.disguiseBannedOrNotSupportedString());
            return Optional.empty();
        }

        var id = disguiseMeta.getIdentifier();
        var backend = getPreferredBackend();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.PLAYER)
            return Optional.empty();

        return Optional.ofNullable(backend.createPlayerInstance(disguiseMeta.playerDisguiseTargetName));
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @Override
    public void finalizeProperties(DisguiseState state) throws ParseErrorException
    {
        super.finalizeProperties(state);
        setupSkinIfPossible(state);

        if (state.disguisePropertyHandler().contains(PropertyNames.ENTITY_CUSTOM_NAME))
        {
            throw ParseErrorException.forProperty(PropertyNames.ENTITY_CUSTOM_NAME)
                    .byMethod("PlayerDisguiseProvider#finalizeProperties")
                    .withLocalizableMessage(ExceptionStrings.unsupported())
                    .withMessage("Custom name is not available for player disguises")
                    .create();
        }
    }

    private void setupSkinIfPossible(DisguiseState state) throws ParseErrorException
    {
        var player = state.getPlayer();

        var mainHandItem = player.getEquipment().getItemInMainHand();
        String id = state.getDisguiseIdentifier();
        var propertyHandler = state.disguisePropertyHandler();
        var playerProperties = DisguiseProperties.INSTANCE.getCollectionOrThrow(PlayerPropertyCollection.class);

        if (propertyHandler.contains(playerProperties.SKIN))
            return;

        var playerDisguiseTargetName = DisguiseTypes.PLAYER.toStrippedId(id);

        var fallbackSkin = PlayerSkinProvider.getInstance().getCachedProfileOptional(DisguiseTypes.PLAYER.toStrippedId(id))
                .orElse(new GameProfile(UUID.randomUUID(), playerDisguiseTargetName));

        propertyHandler.set(playerProperties.SKIN, fallbackSkin);

        //存在玩家头颅，尝试通过头颅获取目标皮肤
        if (mainHandItem.getType() == Material.PLAYER_HEAD)
        {
            var gameProfile = getGameProfile(mainHandItem);

            if (gameProfile == null)
            {
                throw ParseErrorException.forProperty(PropertyNames.PLAYER_SKIN)
                        .withLocalizableMessage(MorphStrings.invalidSkinString())
                        .withMessage("Invalid GameProfile for the given player head")
                        .byMethod("PlayerDisguiseProvider#setupSkinIfPossible")
                        .create();
            }

            //如果玩家头和目标伪装ID一致，那么设置伪装皮肤
            if (gameProfile.name().equals(playerDisguiseTargetName))
                propertyHandler.set(playerProperties.SKIN, gameProfile);
        }

        PlayerSkinProvider.getInstance().fetchSkin(playerDisguiseTargetName)
                .thenAccept(optional ->
                {
                    if (state.disposed() || !fallbackSkin.equals(propertyHandler.get(playerProperties.SKIN)))
                        return;

                    GameProfile outcomingProfile = optional.orElse(fallbackSkin);
                    this.scheduleOn(player, () -> propertyHandler.set(playerProperties.SKIN, outcomingProfile));
                });
    }

    @Override
    public void onDisguiseApply(DisguiseState state)
    {
        mutePlayerWaypoint(state.getPlayer());
        enableDisguiseWaypoint(state);

        super.onDisguiseApply(state);
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        recoverPlayerWaypoint(player);
        disableDisguiseWaypoint(state);

        return super.unMorph(player, state);
    }

    @Override
    public void onPlayerJoinWithDisguise(DisguiseState state)
    {
        mutePlayerWaypoint(state.getPlayer());

        super.onPlayerJoinWithDisguise(state);
    }

    @Override
    public void onPlayerQuitWithDisguise(DisguiseState state)
    {
        recoverPlayerWaypoint(state.getPlayer());

        super.onPlayerQuitWithDisguise(state);
    }

    private GameProfile getGameProfile(ItemStack item)
    {
        if (item.getType() != Material.PLAYER_HEAD) return null;

        var profile = ((SkullMeta) item.getItemMeta()).getPlayerProfile();
        if (profile == null) return null;

        return GameProfileUtils.convertPlayerProfile(profile);
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        var onlinePlayers = featherMorph().getPlatform().onlinePlayersNative();

        var list = new ObjectArrayList<String>();
        onlinePlayers.forEach(p -> list.add(p.getName()));

        return list;
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
    public boolean validForClient(DisguiseState state)
    {
        return true;
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
