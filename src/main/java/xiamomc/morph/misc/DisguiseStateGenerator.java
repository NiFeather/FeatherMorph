package xiamomc.morph.misc;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.network.PlayerOptions;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.offlinestore.OfflineDisguiseState;
import xiamomc.morph.storage.playerdata.PlayerMeta;
import xiamomc.morph.utilities.NbtUtils;

public class DisguiseStateGenerator
{
    public static OfflineDisguiseState toOfflineState(DisguiseState state)
    {
        var offlineState = new OfflineDisguiseState();

        offlineState.playerUUID = state.getPlayer().getUniqueId();
        offlineState.playerName = state.getPlayer().getName();

        offlineState.disguiseID = state.getDisguiseIdentifier();

        var newDisguise = state.getDisguiseWrapper().clone();
        var backend = newDisguise.getBackend();

        offlineState.disguiseData = "%s|%s".formatted(backend.getIdentifier(), backend.toOfflineSave(newDisguise));
        offlineState.displayingDisguisedItems = state.showingDisguisedItems();
        offlineState.snbt = state.getFullNbtString();
        offlineState.profileString = state.getProfileNbtString();

        if (state.entityCustomName != null)
            offlineState.customName = GsonComponentSerializer.gson().serialize(state.entityCustomName);

        return offlineState;
    }

    /**
     * 从离线存储转换为实例
     * @param offlineState 离线存储
     * @return DisguiseState的实例
     */
    public static DisguiseState fromOfflineState(OfflineDisguiseState offlineState,
                                                 PlayerOptions<Player> playerOptions, PlayerMeta playerMeta,
                                                 MorphSkillHandler skillHandler, DisguiseBackend<?, ?> backend)
    {
        if (!offlineState.isValid())
            throw new RuntimeException("Broken Offline State for UUID '%s'".formatted(offlineState.playerUUID));

        var player = Bukkit.getPlayer(offlineState.playerUUID);

        if (player == null) throw new RuntimeException("No matching player found for UUID '%s'".formatted(offlineState.playerUUID));

        //获取伪装ID和Provider
        var disguiseIdentifier = offlineState.disguiseID;
        var provider = MorphManager.getProvider(disguiseIdentifier);

        //获取技能ID
        //From MorphManager
        var rawIdentifierHasSkill = skillHandler.hasSkill(disguiseIdentifier) || skillHandler.hasSpeficSkill(disguiseIdentifier, SkillType.NONE);
        var targetSkillID = rawIdentifierHasSkill ? disguiseIdentifier : provider.getNameSpace() + ":" + MorphManager.disguiseFallbackName;

        var wrapper = backend.fromOfflineSave(offlineState.disguiseData);

        if (wrapper == null) return null;

        if (provider.getNameSpace().equals("player"))
        {
            var playerName = DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier);
            wrapper.setDisguiseName(playerName);
        }

        //构建State
        var state = new DisguiseState(player,
                disguiseIdentifier, targetSkillID,
                wrapper, provider,
                null, playerOptions, playerMeta);

        try
        {
            var rawProfile = NbtUtils.readGameProfile(offlineState.profileString);

            if (rawProfile != null)
            {
                var profile = new MorphGameProfile(rawProfile);
                profile.setName(wrapper.getDisguiseName());
                wrapper.applySkin(profile);
            }
        }
        catch (Throwable t)
        {
            var logger = MorphPlugin.getInstance().getSLF4JLogger();
            logger.error("Unable to parse profile data: " + t.getMessage());
        }

        var compound = NbtUtils.toCompoundTag(offlineState.snbt);
        if (compound != null)
            state.getDisguiseWrapper().mergeCompound(compound);

        //设置显示名称
        if (offlineState.customName != null)
        {
            var component = GsonComponentSerializer.gson().deserialize(offlineState.customName);

            state.entityCustomName = component;
            state.setDisplayName(component);
        }

        //设置伪装物品显示
        if (state.supportsShowingDefaultItems())
            state.setShowingDisguisedItems(offlineState.displayingDisguisedItems);

        return state;
    }
}
