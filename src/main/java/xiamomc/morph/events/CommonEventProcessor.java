package xiamomc.morph.events;

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.RevealingHandler;
import xiamomc.morph.abilities.impl.AttributeModifyingAbility;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.api.gameplay.PlayerJoinedWithDisguiseEvent;
import xiamomc.morph.messages.HintStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.misc.NetworkingHelper;
import xiamomc.morph.misc.OfflineDisguiseResult;
import xiamomc.morph.misc.playerList.PlayerListHandler;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.network.commands.S2C.S2CSwapCommand;
import xiamomc.morph.network.commands.S2C.map.S2CMapRemoveCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.network.server.ServerSetEquipCommand;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.Random;

import static xiamomc.morph.utilities.DisguiseUtils.itemOrAir;

public class CommonEventProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphs;

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    @Resolved(shouldSolveImmediately = true)
    private MorphSkillHandler skillHandler;

    @Resolved(shouldSolveImmediately = true)
    private VanillaMessageStore vanillaMessageStore;

    @Resolved(shouldSolveImmediately = true)
    private RevealingHandler revealingHandler;

    @Resolved(shouldSolveImmediately = true)
    private NetworkingHelper networkingHelper;

    private Bindable<Boolean> unMorphOnDeath;

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
        if (killer != null && killer != entity)
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
        if (e.getEntity() instanceof Player player)
        {
            var state = morphs.getDisguiseStateFor(player);

            if (state != null)
            {
                state.getSoundHandler().resetSoundTime();

                //如果伤害是0，那么取消事件
                if (e.getDamage() > 0d)
                    state.setSkillCooldown(Math.max(state.getSkillCooldown(), cooldownOnDamage.get()), true);
            }
        }
    }

    private final Bindable<Boolean> doRevealing = new Bindable<>(true);

    private final Bindable<Boolean> allowAcquireMorphs = new Bindable<>(false);

    @Initializer
    private void load()
    {
        config.bind(cooldownOnDamage, ConfigOption.SKILL_COOLDOWN_ON_DAMAGE);
        config.bind(bruteIgnoreDisguises, ConfigOption.PIGLIN_BRUTE_IGNORE_DISGUISES);
        config.bind(doRevealing, ConfigOption.REVEALING);
        config.bind(allowAcquireMorphs, ConfigOption.ALLOW_ACQUIRE_MORPHS);

        unMorphOnDeath = config.getBindable(Boolean.class, ConfigOption.UNMORPH_ON_DEATH);
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        if (plugin.getCurrentTick() % 8 == 0)
        {
            playersMinedGoldBlocks.clear();
            susIncreasedPlayers.clear();
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

        //workaround: 右键盔甲架不会触发事件、盔甲架是InteractAtEntityEvent
        if (e.getRightClicked() instanceof ArmorStand)
            e.setCancelled(tryInvokeSkillOrQuickDisguise(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        //workaround: 右键继承了InventoryHolder的实体会打开他们的物品栏而不是使用技能
        if (e.getRightClicked() instanceof InventoryHolder && e.getRightClicked().getType() != EntityType.PLAYER)
            e.setCancelled(tryInvokeSkillOrQuickDisguise(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (tryInvokeSkillOrQuickDisguise(e.getPlayer(), e.getAction(), e.getHand()))
            e.setCancelled(true);
    }

    /**
     * 尝试使用技能或快速伪装
     * @param player 目标玩家
     * @param action 动作
     * @return 是否应该取消Interact事件
     */
    private boolean tryInvokeSkillOrQuickDisguise(Player player, Action action, EquipmentSlot slot)
    {
        var actionItem = morphs.getActionItem();
        if (slot != EquipmentSlot.HAND || actionItem == null) return false;

        var state = morphs.getDisguiseStateFor(player);
        var mainHandItem = player.getEquipment().getItemInMainHand();
        var mainHandItemType = mainHandItem.getType();

        if (mainHandItemType.isAir() || !player.isSneaking()) return false;

        //右键玩家头颅：快速伪装
        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.isLeftClick() && morphs.doQuickDisguise(player, false))
            return true;

        if (mainHandItemType != actionItem || state == null) return false;

        //激活技能或取消伪装
        if (action.isLeftClick())
        {
            if (player.getEyeLocation().getDirection().getY() <= -0.95)
                morphs.unMorph(player);
            else
                morphs.setSelfDisguiseVisible(player, !state.isSelfViewing(), true);

            return true;
        }

        if (state.getSkillCooldown() <= 0)
        {
            morphs.executeDisguiseSkill(player);
        }
        else
        {
            //一段时间内内只接受一次右键触发
            //传送前后会触发两次Interact，而且这两个Interact还不一定在同个Tick里
            if (plugin.getCurrentTick() - skillHandler.getLastInvoke(player) <= 1)
                return true;

            player.sendMessage(MessageUtils.prefixes(player,
                    SkillStrings.skillPreparing().resolve("time", state.getSkillCooldown() / 20 + "")));

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state == null) return;

        if (!state.showingDisguisedItems()) return;

        state.swapHands();

        var equip = state.getDisguisedItems();

        var mainHand = itemOrAir(equip.getItemInMainHand());
        var offHand = itemOrAir(equip.getItemInOffHand());

        if (clientHandler.isFutureClientProtocol(player, 3))
        {
            clientHandler.sendCommand(player, new S2CSwapCommand());
        }
        else
        {
            clientHandler.sendCommand(player, new ServerSetEquipCommand(mainHand, EquipmentSlot.HAND));
            clientHandler.sendCommand(player, new ServerSetEquipCommand(offHand, EquipmentSlot.OFF_HAND));
        }

        var wrapper = state.getDisguiseWrapper();
        var wrapperEquipments = wrapper.getFakeEquipments();
        wrapperEquipments.setItemInMainHand(mainHand);
        wrapperEquipments.setItemInOffHand(offHand);
        wrapper.setFakeEquipments(wrapperEquipments);
    }

    @EventHandler
    public void onClientOptionChanged(PlayerClientOptionsChangeEvent e)
    {
        var locale = e.getLocale();
        vanillaMessageStore.getOrCreateSubStore(locale);

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        clientHandler.markPlayerReady(player);

        //如果玩家是第一次用客户端连接，那么等待3秒向其发送提示
        if (clientHandler.clientConnected(player))
        {
            var config = morphs.getPlayerMeta(player);

            if (!config.shownMorphClientHint && !config.getUnlockedDisguiseIdentifiers().isEmpty())
                this.addSchedule(() ->
                {
                    if (player.isOnline() && !config.shownMorphClientHint)
                    {
                        player.sendMessage(MessageUtils.prefixes(player, HintStrings.firstGrantClientHintString()));

                        config.shownMorphClientHint = true;
                    }
                }, 20 * 3);
        }

        for (var attribute : Attribute.values())
        {
            var instance = player.getAttribute(attribute);

            if (instance == null) continue;

            instance.removeModifier(AttributeModifyingAbility.modifierKey);
        }

        this.addSchedule(() -> PlayerListHandler.instance().handle(player));

        if (state != null)
        {
            state.onPlayerJoin();

            //调用Morph事件
            new PlayerJoinedWithDisguiseEvent(player, state).callEvent();

            return;
        }

        var offlineState = morphs.getOfflineState(player);

        if (offlineState != null)
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.stateRecoverReasonString()));

            var result = morphs.disguiseFromOfflineState(player, offlineState);

            if (result == OfflineDisguiseResult.SUCCESS)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateString()));
            }
            else if (result == OfflineDisguiseResult.LIMITED)
            {
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateLimitedString()));
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateLimitedHintString()));
            }
            else
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringFailedString()));
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        clientHandler.unInitializePlayer(e.getPlayer());
        skillHandler.removeUnusedList(e.getPlayer());

        var state = morphs.getDisguiseStateFor(e.getPlayer());

        List<Player> players;
        synchronized (this)
        {
            players = new ObjectArrayList<>(Bukkit.getOnlinePlayers());
        }

        if (state != null)
        {
            var bossbar = state.getBossbar();

            if (bossbar != null)
                players.forEach(p -> p.hideBossBar(bossbar));

            state.onOffline();
        }

        var targets = players.stream()
                .filter(p -> p.hasPermission(CommonPermissions.DISGUISE_REVEALING))
                .toList();

        var cmd = new S2CMapRemoveCommand(e.getPlayer().getEntityId());
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

    private final Random random = new Random();

    private final List<Player> susIncreasedPlayers = new ObjectArrayList<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getPlayer());
        if (state == null) return;

        if (e.getBlock().getType().equals(Material.GOLD_BLOCK))
            playersMinedGoldBlocks.add(e.getPlayer());
    }

    private final List<Player> playersMinedGoldBlocks = new ObjectArrayList<>();

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

        if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent edbee && edbee.getDamager() == player)
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

        var disguise = state.getDisguiseWrapper();
        var disguiseEntityType = state.getEntityType();

        //检查是否要取消Target
        boolean shouldTarget = switch (sourceEntityType)
                {
                    case ZOMBIE, ZOMBIE_VILLAGER, HUSK, DROWNED -> EntityTypeUtils.isZombiesHostile(disguiseEntityType);
                    case SKELETON, STRAY -> EntityTypeUtils.isGolem(disguiseEntityType) || disguise.isPlayerDisguise();
                    case PIGLIN ->
                    {
                        yield EntityTypeUtils.isPiglinHostile(disguiseEntityType);
                    }
                    case PIGLIN_BRUTE -> EntityTypeUtils.isBruteHostile(disguiseEntityType);
                    case WITHER_SKELETON -> EntityTypeUtils.isWitherSkeletonHostile(disguiseEntityType);
                    case GUARDIAN, ELDER_GUARDIAN -> EntityTypeUtils.isGuardianHostile(disguiseEntityType);
                    case WITHER -> EntityTypeUtils.isWitherHostile(disguiseEntityType);
                    case PILLAGER, VEX, ILLUSIONER, VINDICATOR, EVOKER, RAVAGER -> EntityTypeUtils.isRaiderHostile(disguiseEntityType);
                    case ENDERMAN -> disguiseEntityType == EntityType.PLAYER || disguiseEntityType == EntityType.ENDERMITE;
                    case ZOGLIN -> EntityTypeUtils.isZoglinHostile(disguiseEntityType);
                    default -> disguise.isPlayerDisguise();
                };

        // 根据揭示值判定要不要允许生物攻击玩家
        var revealingState = revealingHandler.getRevealingState(player);
        var revealingLevel = revealingState.getRevealingLevel();

        if (!susIncreasedPlayers.contains(player) && doRevealing.get())
        {
            revealingState.addBaseValue(RevealingHandler.RevealingDiffs.ON_MOB_TARGET);
            susIncreasedPlayers.add(player);
        }

        // 如果伪装揭示值已满，则不要处理此事件
        // 否则，生物将有 ((val - 20) * 0.35)% 的概率target玩家，如果target失败，则加1点揭示值
        if (revealingLevel == RevealingHandler.RevealingLevel.REVEALED)
        {
            shouldTarget = true;
        }
        else if (revealingLevel == RevealingHandler.RevealingLevel.SUSPECT)
        {
            var rdv = random.nextInt(0, 100);

            //logger.info("RDV " + rdv + " <= " + (revealingState.getBaseValue() - 20) + "?");
            shouldTarget = shouldTarget || (rdv / 0.35) <= revealingState.getBaseValue() - 20;
        }

        e.setCancelled(e.isCancelled() || !shouldTarget);
    }

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        if (!(entity instanceof LivingEntity) && !(entity.getType() == EntityType.ARMOR_STAND))
            return;

        if (!allowAcquireMorphs.get())
            return;

        if (entity instanceof Player targetPlayer)
            morphs.grantMorphToPlayer(player, DisguiseTypes.PLAYER.toId(targetPlayer.getName()));
        else
            morphs.grantMorphToPlayer(player, entity.getType().getKey().asString());
    }
}
