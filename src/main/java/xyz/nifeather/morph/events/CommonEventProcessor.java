package xyz.nifeather.morph.events;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Material;
import org.bukkit.block.data.type.CreakingHeart;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.RevealingHandler;
import xyz.nifeather.morph.api.events.gameplay.PlayerJoinedWithDisguiseEvent;
import xyz.nifeather.morph.api.networking.exceptions.PlayerDisconnectedException;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.messages.vanilla.MasterVanillaMessageStore;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.ModNetworkingHelper;
import xyz.nifeather.morph.misc.OfflineDisguiseResult;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.values.BaseLivingEntityPropertyCollection;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.Constants;
import xyz.nifeather.morph.network.commands.S2C.S2CSwapCommand;
import xyz.nifeather.morph.network.commands.S2C.admin.reveal.S2CRemoveAdminRevealCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static xyz.nifeather.morph.utilities.DisguiseUtils.itemOrAir;

public class CommonEventProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphs;

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    @Resolved(shouldSolveImmediately = true)
    private SkillManager skillHandler;

    @Resolved(shouldSolveImmediately = true)
    private MasterVanillaMessageStore masterVanillaMessageStore;

    @Resolved(shouldSolveImmediately = true)
    private RevealingHandler revealingHandler;

    private final Bindable<Boolean> unMorphOnDeath = new Bindable<>(true);

    private final Bindable<Boolean> doRevealing = new Bindable<>(true);

    private final Bindable<Boolean> allowAcquireMorphs = new Bindable<>(false);

    @Initializer
    private void load()
    {
        config.bind(cooldownOnDamage, ConfigOptions.SKILL_COOLDOWN_ON_DAMAGE);
        config.bind(bruteIgnoreDisguises, ConfigOptions.PIGLIN_BRUTE_IGNORE_DISGUISES);
        config.bind(doRevealing, ConfigOptions.REVEALING);
        config.bind(allowAcquireMorphs, ConfigOptions.ALLOW_ACQUIRE_MORPHS);
        config.bind(unMorphOnDeath, ConfigOptions.UNMORPH_ON_DEATH);

        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        if (plugin.getCurrentTick() % 8 == 0)
            playersMinedGoldBlocks.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChannelRegister(PlayerRegisterChannelEvent event)
    {
        if (featherMorph().debugOutputEnabled())
            logger.info("Player registered channel %s".formatted(event.getChannel()));

        if (event.getChannel().startsWith(FeatherMorphMain.getMorphNameSpace()))
            clientHandler.onPlayerChannelRegister(event.getPlayer());
    }

    @EventHandler
    public void onEntityAddToWorld(EntityAddToWorldEvent e)
    {
        if (!(e.getEntity() instanceof Player player))
            return;

        var world = e.getWorld();

        var state = morphs.getDisguiseStateFor(player);
        if (state != null && morphs.disguiseDisabledInWorld(world))
        {
            this.scheduleOn(player, () ->
            {
                if (!player.getWorld().equals(world)) return;

                MessageUtils.send(player, MorphStrings.disguiseDisabledInWorldString());
                morphs.unMorph(player);
            });
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e)
    {
        var entity = e.getEntity();
        var killer = entity.getKiller();

        //logger.warn(entity + "died by:" + killer);

        //盔甲架需要额外的一些东西
        if (entity.getType() == EntityType.ARMOR_STAND)
        {
            //logger.warn("IS armor stand");
            var lastCause = entity.getLastDamageCause();

            //logger.warn("cause: " + String.valueOf(lastCause));
            if (lastCause instanceof EntityDamageByEntityEvent damageEvent)
            {
                var cause = damageEvent.getDamager();

                //logger.warn("cause entity: " + cause);
                if (cause instanceof Player) killer = (Player) cause;
            }
        }

        //防止获得自己的伪装
        if (killer != null && !killer.equals(entity))
            this.onPlayerKillEntity(killer, e.getEntity());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        if (unMorphOnDeath.get())
            morphs.unMorph(e.getPlayer(), e.getPlayer(), true, true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getPlayer());
        if (state != null)
        {
            state.getAbilityUpdater().reApplyAbility();

            var skill = state.getSkill();
            skill.onInitialEquip(state);
        }
    }

    private final Bindable<Integer> cooldownOnDamage = new Bindable<>(0);
    private final Bindable<Boolean> bruteIgnoreDisguises = new Bindable<>(true);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTookDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof Player player))
            return;

        var state = morphs.getDisguiseStateFor(player);

        if (state != null)
        {
            state.getSoundHandler().resetSoundTime();

            //如果伤害是0，那么取消事件
            if (e.getDamage() > 0d)
                state.setSkillCooldown(Math.max(state.calculateRemainingCooldown(), cooldownOnDamage.get()), true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e)
    {
        //workaround: 对悦灵的伪装右键会导致物品栏失去同步
        if (e.getRightClicked() instanceof Player clickedPlayer)
        {
            var state = morphs.getDisguiseStateFor(clickedPlayer);

            if (state != null)
            {
                if (state.getEntityType() == EntityType.ALLAY)
                    e.setCancelled(true);

                if (EntityTypeUtils.saddleable(state.getDisguiseWrapper().getEntityType()))
                {
                    var slot = e.getHand();
                    var item = e.getPlayer().getEquipment().getItem(slot);

                    if (item.getType() == Material.SADDLE)
                        state.getDisguiseWrapper().setSaddled(true);
                    else if (item.getType() != Material.AIR)
                        e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state == null) return;

        if (!state.showingDisguisedItems()) return;

        var equip = state.getDisguiseEquipment();

        var mainHand = itemOrAir(equip.getItemInMainHand());
        var offHand = itemOrAir(equip.getItemInOffHand());

        if (clientHandler.isFutureClientProtocol(player, 3))
        {
            if (!clientHandler.isFutureClientProtocol(player, Constants.ApiLevel.EQUIPMENT_AND_SKIN_ARE_NOW_PROPERTY.protocolVersion))
                clientHandler.sendCommand(player, new S2CSwapCommand());
        }
        else
        {
            clientHandler.sendCommand(player, new ServerSetEquipCommand(mainHand, EquipmentSlot.HAND));
            clientHandler.sendCommand(player, new ServerSetEquipCommand(offHand, EquipmentSlot.OFF_HAND));
        }

        var propertyHandler = state.disguisePropertyHandler();
        var properties = DisguiseProperties.INSTANCE.getCollectionOrThrow(BaseLivingEntityPropertyCollection.class);

        var newEquipment = DisguiseEquipment.builder(equip)
                .offHand(mainHand)
                .mainHand(offHand)
                .build();

        propertyHandler.set(properties.EQUIPMENT, newEquipment);
    }

    @EventHandler
    public void onClientOptionChanged(PlayerClientOptionsChangeEvent e)
    {
        var locale = e.getLocale().toLowerCase(Locale.ROOT);
        masterVanillaMessageStore.getOrCreateSubStore(locale);

        if (e.hasLocaleChanged())
        {
            var player = e.getPlayer();
            var state = morphs.getDisguiseStateFor(player);

            if (state != null && state.entityCustomName == null)
            {
                var displayName = state.getProvider().getDisplayName(state.getDisguiseIdentifier(), locale);
                state.setPlayerDisplay(displayName);
            }
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private ModNetworkingHelper modNetworkingHelper;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        var effectivePermissions = new ObjectOpenHashSet<>(player.getEffectivePermissions());
        List<String> legacyPermissions = new ObjectArrayList<>();

        effectivePermissions.forEach(permInfo ->
        {
            var name = permInfo.getPermission();
            if (!name.startsWith("xiamomc.morph")) return;

            legacyPermissions.add(name);
        });

        if (!legacyPermissions.isEmpty())
        {
            logger.error("- x - x - x - x - x - x - x - x - x - x - x - x -");
            logger.error("MAY I HAVE YOUR ATTENTION PLEASE!");
            logger.error("");
            logger.error("Found legacy permission set for player '%s'!".formatted(player.getName()));
            logger.error("Please migrate to the new prefix 'feathermorph.XXX' instead of 'xiamomc.morph.XXX', as legacy permission support is now ENDED!");
            logger.error("Permissions found:");
            legacyPermissions.forEach(p -> logger.error("  --> %s".formatted(p)));
            logger.error("");
            logger.error("- x - x - x - x - x - x - x - x - x - x - x - x -");
        }

        if (state != null)
        {
            state.onPlayerJoin();

            modNetworkingHelper.sendCommandToRevealablePlayers(modNetworkingHelper.genPartialMapCommand(state));

            //调用Morph事件
            new PlayerJoinedWithDisguiseEvent(player, state).callEvent();

            return;
        }

        var offlineState = morphs.getOfflineState(player);

        if (offlineState != null)
        {
            MessageUtils.send(player, MorphStrings.stateRecoverReasonString());

            var result = morphs.disguiseFromOfflineState(player, offlineState);

            if (result == OfflineDisguiseResult.SUCCESS)
            {
                MessageUtils.send(player, MorphStrings.recoveringStateString());
            }
            else if (result == OfflineDisguiseResult.LIMITED)
            {
                MessageUtils.send(player, MorphStrings.recoveringStateLimitedString());
                MessageUtils.send(player, MorphStrings.recoveringStateLimitedHintString());
            }
            else
            {
                MessageUtils.send(player, MorphStrings.recoveringFailedString());
            }
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getPlayer());
        if (state == null)
            return;

        state.waypointUpdater().updateRealtimeConnections();
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        clientHandler.disconnect(e.getPlayer(), new PlayerDisconnectedException("Player disconnected"));
        skillHandler.trim();

        var state = morphs.getDisguiseStateFor(e.getPlayer());

        List<Player> players;
        synchronized (this)
        {
            players = new ObjectArrayList<>(featherMorph().getPlatform().onlinePlayersNative());
        }

        if (state != null)
        {
            var bossbar = state.getBossbar();

            if (bossbar != null)
                players.forEach(p -> p.hideBossBar(bossbar));

            state.onPlayerQuit();
        }

        var targets = players.stream()
                .filter(p -> p.hasPermission(CommonPermissions.DISGUISE_REVEALING))
                .toList();

        var cmd = new S2CRemoveAdminRevealCommand(e.getPlayer().getEntityId());
        targets.forEach(p -> clientHandler.sendCommand(p, cmd));
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state != null)
        {
            //刷新主动
            var skill = state.getSkill();
            skill.onInitialEquip(state);

            //刷新被动
            state.getAbilityUpdater().reApplyAbility();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getPlayer());
        if (state == null) return;

        if (e.getBlock().getType().equals(Material.GOLD_BLOCK))
            playersMinedGoldBlocks.add(e.getPlayer());
    }

    private final List<Player> playersMinedGoldBlocks = Collections.synchronizedList(new ObjectArrayList<>());

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e)
    {
        if (e.getTarget() == null) return;

        var sourceEntityType = e.getEntityType();

        if (sourceEntityType == EntityType.PIGLIN_BRUTE && bruteIgnoreDisguises.get())
            return;

        if (sourceEntityType == EntityType.WARDEN || !(e.getTarget() instanceof Player player))
            return;

        if (sourceEntityType == EntityType.PIGLIN && playersMinedGoldBlocks.contains(player))
            return;

        if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent edbee && edbee.getDamager().equals(player))
            return;

        //受到外力攻击或者其他原因时不要处理
        switch (e.getReason())
        {
            case TARGET_ATTACKED_ENTITY, TARGET_ATTACKED_NEARBY_ENTITY,
                    REINFORCEMENT_TARGET, FOLLOW_LEADER, DEFEND_VILLAGE,
                    TARGET_ATTACKED_OWNER, OWNER_ATTACKED_TARGET, CUSTOM, UNKNOWN ->
            {
                return;
            }

            default -> {}
        }

        var state = morphs.getDisguiseStateFor(player);

        //目标玩家没在伪装时不要处理
        if (state == null) return;

        var disguiseEntityType = state.getEntityType();

        //检查是否要取消Target
        boolean shouldTarget = switch (sourceEntityType)
                {
                    case ZOMBIE, ZOMBIE_VILLAGER, HUSK, DROWNED -> EntityTypeUtils.isZombiesHostile(disguiseEntityType);
                    case SKELETON, STRAY -> EntityTypeUtils.isGolem(disguiseEntityType) || state.getEntityType() == EntityType.PLAYER;
                    case PIGLIN -> EntityTypeUtils.isPiglinHostile(disguiseEntityType);
                    case PIGLIN_BRUTE -> EntityTypeUtils.isBruteHostile(disguiseEntityType);
                    case WITHER_SKELETON -> EntityTypeUtils.isWitherSkeletonHostile(disguiseEntityType);
                    case GUARDIAN, ELDER_GUARDIAN -> EntityTypeUtils.isGuardianHostile(disguiseEntityType);
                    case WITHER -> EntityTypeUtils.isWitherHostile(disguiseEntityType);
                    case PILLAGER, VEX, ILLUSIONER, VINDICATOR, EVOKER, RAVAGER -> EntityTypeUtils.isRaiderHostile(disguiseEntityType);
                    case ENDERMAN -> disguiseEntityType == EntityType.PLAYER || disguiseEntityType == EntityType.ENDERMITE;
                    case ZOGLIN -> EntityTypeUtils.isZoglinHostile(disguiseEntityType);
                    default -> state.getEntityType() == EntityType.PLAYER;
                };

        // 根据揭示值判定要不要允许生物攻击玩家
        var revealingState = revealingHandler.getRevealingState(player);

        // 每次被生物注意到，都会让揭示值上升
        if (doRevealing.get())
            revealingState.addBaseValue(RevealingHandler.RevealingDiffs.ON_MOB_TARGET);

        shouldTarget = shouldTarget || revealingState.shouldMobsAwareRevealed();

        e.setCancelled(!shouldTarget);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAdvancement(BlockBreakEvent event)
    {
        var block = event.getBlock();

        if (block.getType() != Material.CREAKING_HEART)
            return;

        if (!(block.getBlockData() instanceof CreakingHeart creakingHeart))
            return;

        if (creakingHeart.getCreakingHeartState() != CreakingHeart.State.AWAKE || !creakingHeart.isNatural())
            return;

        morphs.grantMorphToPlayer(event.getPlayer(), EntityType.CREAKING.getKey().asString());
    }

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        if (!(entity instanceof LivingEntity) && !(entity.getType() == EntityType.ARMOR_STAND))
            return;

        if (!allowAcquireMorphs.get())
            return;

        if (entity.getScoreboardTags().contains("feathermorph_nogrant"))
            return;

        if (entity instanceof Player targetPlayer)
            morphs.grantMorphToPlayer(player, DisguiseTypes.PLAYER.toId(targetPlayer.getName()));
        else
        {
            var type = entity.getType();

            if (type != EntityType.CREAKING)
                morphs.grantMorphToPlayer(player, type.getKey().asString());
        }
    }
}
