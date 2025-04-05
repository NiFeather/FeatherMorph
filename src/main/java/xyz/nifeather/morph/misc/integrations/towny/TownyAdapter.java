package xyz.nifeather.morph.misc.integrations.towny;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.*;
import com.palmergames.bukkit.towny.event.player.PlayerEntersIntoTownBorderEvent;
import com.palmergames.bukkit.towny.event.player.PlayerExitsFromTownBorderEvent;
import com.palmergames.bukkit.towny.event.town.TownUnclaimEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.abilities.impl.FlyAbility;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.events.api.gameplay.MorphTownBooleanFlagChangedEvent;
import xyz.nifeather.morph.events.api.gameplay.PlayerExecuteSkillEvent;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.MorphStrings;
import xyz.nifeather.morph.misc.integrations.towny.commands.TownyIntegrationCommand;

import java.util.Arrays;
import java.util.List;

public class TownyAdapter extends MorphPluginObject implements Listener
{
    private final TownyAPI townyAPI = TownyAPI.getInstance();

    private final Bindable<Boolean> allowFlyInWilderness = new Bindable<>(false);

    private final List<Player> blockedPlayers = ObjectLists.synchronize(new ObjectArrayList<>());

    public TownyAdapter()
    {
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                new TownyIntegrationCommand().register(event.registrar()));
    }

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(allowFlyInWilderness, ConfigOption.TOWNY_ALLOW_FLY_IN_WILDERNESS);

        allowFlyInWilderness.onValueChanged((o, n) ->
        {
            Bukkit.getOnlinePlayers().forEach(p ->
                    this.scheduleOn(p, () -> updatePlayer(p, null)));
        });
    }

    //region Towny events

    @EventHandler
    public void onTownCreate(NewTownEvent e)
    {
        //Init town default flags
        var town = e.getTown();
        for (BooleanDataField field : TownyFlags.FLAGS_FOR_INIT)
            MetaDataUtil.setBoolean(town, field, field.getValue(), true);
    }

    @EventHandler
    public void onEnterPlot(PlayerEntersIntoTownBorderEvent e)
    {
        var player = e.getPlayer();
        updatePlayer(player, e.getEnteredTown());
    }

    @EventHandler
    public void onChangePlot(PlayerChangePlotEvent e)
    {
    }

    @EventHandler
    public void onTownSpawn(TownSpawnEvent e)
    {
        this.updatePlayer(e.getPlayer(), e.getToTown());
    }

    @EventHandler
    public void onLeavePlot(PlayerExitsFromTownBorderEvent e)
    {
        updatePlayer(e.getPlayer(), null, true);
    }

    @EventHandler
    public void onTownUnClaim(TownUnclaimEvent e)
    {
        e.getWorldCoord()
                .getChunks()
                .forEach(task -> task.thenAccept(c -> this.updatePlayersInChunk(c, null)));
    }

    @EventHandler
    public void onTownClaim(TownClaimEvent e)
    {
        var currentTown = e.getTownBlock().getTownOrNull();

        e.getTownBlock()
                .getWorldCoord()
                .getChunks()
                .forEach(chunkTask -> chunkTask.thenAccept(chunk -> this.updatePlayersInChunk(chunk, currentTown)));
    }

    @EventHandler
    public void onTownRemoveResident(TownRemoveResidentEvent e)
    {
        var player = e.getResident().getPlayer();

        if (player != null)
            updatePlayer(e.getResident().getPlayer(), e.getTown());
    }

    @EventHandler
    public void onTownAddResident(TownAddResidentEvent e)
    {
        var player = e.getResident().getPlayer();

        if (player != null)
            updatePlayer(player, e.getTown());
    }

    // Folia没有提供监听玩家改变世界的事件，所以我们只能监听EntityAddToWorldEvent
    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent e)
    {
        if (!(e.getEntity() instanceof Player player)) return;

        updatePlayer(player, null);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        updatePlayer(e.getPlayer(), null);
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        this.unblockPlayer(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e)
    {
        if (e.getFrom().getWorld().equals(e.getTo().getWorld()))
            this.updatePlayer(e.getPlayer(), townyAPI.getTown(e.getTo()));
    }

    //endregion Towny events

    //region Our events

    @EventHandler
    public void onDisguiseSkill(PlayerExecuteSkillEvent event)
    {
        var player = event.getPlayer();
        var town = TownyAPI.getInstance().getTown(player.getLocation());

        if (town == null)
            return;

        if (playerTrustedByTown(town, player))
            return;

        boolean outsidersSkilAllowed = TownyFlags.ALLOW_OUTSIDERS_USE_SKILL.getValue();

        //  检查城镇是否允许外来者飞行
        if (MetaDataUtil.hasMeta(town, TownyFlags.ALLOW_OUTSIDERS_USE_SKILL))
            outsidersSkilAllowed = MetaDataUtil.getBoolean(town, TownyFlags.ALLOW_OUTSIDERS_FLIGHT);

        if (outsidersSkilAllowed)
            return;

        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.townyBlockedSkillString()));
        event.setCancelled(true);
    }

    @EventHandler
    public void onMorphFlagChanged(MorphTownBooleanFlagChangedEvent e)
    {
        this.refreshPlayersIn(e.getTown());
    }

    private void refreshPlayersIn(Town town)
    {
        // Towny没有API来告诉我们一个Town里进了多少玩家
        // 因此我们只能遍历所有玩家实例
        Bukkit.getOnlinePlayers().forEach(player ->
        {
            // 获取玩家爱所在的Town
            var currentTown = TownyAPI.getInstance().getTown(player.getLocation());

            // 在野外或者不是目标town
            if (currentTown == null || currentTown != town) return;

            this.updatePlayer(player, currentTown);
        });
    }

    //endregion Our events

    private boolean playerTrustedByTown(Town targetTown, Player player)
    {
        var resident = townyAPI.getResident(player);
        if (resident == null) return false;

        // 城镇是否信任此玩家？
        if (targetTown.getTrustedResidents().contains(resident))
            return true;

        // 玩家城镇
        var playerTown = resident.getTownOrNull();

        // 如果玩家没有城镇，返回false
        // 因为上面检查了野外和城镇的信任，这里应该没有问题。
        if (playerTown == null)
            return false;

        // 玩家就是城镇成员
        if (playerTown.getUUID() == targetTown.getUUID())
            return true;

        // 盟友
        if (CombatUtil.isAlly(targetTown, playerTown))
            return true;

        // 国家
        if (CombatUtil.isSameNation(targetTown, playerTown))
            return true;

        return false;
    }

    private boolean allowFlightAt(Player player, @Nullable Town town)
    {
        // Town == null -> Wilderness
        if (town == null)
            return allowFlyInWilderness.get();

        // 如果玩家被城镇信任，那么允许飞行
        if (playerTrustedByTown(town, player))
            return true;

        //  检查城镇是否允许外来者飞行
        if (MetaDataUtil.hasMeta(town, TownyFlags.ALLOW_OUTSIDERS_FLIGHT))
            return MetaDataUtil.getBoolean(town, TownyFlags.ALLOW_OUTSIDERS_FLIGHT);
        else
            return TownyFlags.ALLOW_OUTSIDERS_FLIGHT.getValue();
    }

    private void updatePlayersInChunk(Chunk chunk, Town currentTown)
    {
        var players = Arrays.stream(chunk.getEntities())
                .filter(entity -> entity.getType() == EntityType.PLAYER)
                .map(entity -> (Player)entity)
                .toList();

        if (players.isEmpty()) return;

        for (var player : players)
            updatePlayer(player, currentTown, true);
    }


    public void updatePlayer(@NotNull Player player, @Nullable Town currentTown)
    {
        this.updatePlayer(player, currentTown, false);
    }

    public void updatePlayer(@NotNull Player player, @Nullable Town currentTown, boolean noTownLookup)
    {
        if (!worldUsingTowny(player.getWorld()))
        {
            unblockPlayer(player);
            return;
        }

        if (currentTown == null && !noTownLookup)
            currentTown = townyAPI.getTown(player.getLocation());

        if (allowFlightAt(player, currentTown))
            unblockPlayer(player);
        else
            blockPlayer(player);
    }

    private boolean worldUsingTowny(World world)
    {
        var townyWorld = townyAPI.getTownyWorld(world);
        if (townyWorld == null) return false;

        return townyWorld.isUsingTowny();
    }

    private void blockPlayer(Player player)
    {
        var playerAlreadyBlocked = blockedPlayers.contains(player);
        if (playerAlreadyBlocked) return;

        blockedPlayers.add(player);
        FlyAbility.blockPlayer(player, this);
    }

    private void unblockPlayer(Player player)
    {
        FlyAbility.unBlockPlayer(player, this);
        blockedPlayers.remove(player);
    }
}
