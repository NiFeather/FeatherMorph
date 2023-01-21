package xiamomc.morph.events;

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.watchers.AbstractHorseWatcher;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.*;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.utilities.DisguiseUtils;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetEquipCommand;
import xiamomc.morph.network.commands.S2C.S2CSwapCommand;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import static xiamomc.morph.utilities.DisguiseUtils.itemOrAir;

public class CommonEventProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphCommandHelper cmdHelper;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphs;

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    @Resolved(shouldSolveImmediately = true)
    private MorphSkillHandler skillHandler;

    @Resolved(shouldSolveImmediately = true)
    private VanillaMessageStore vanillaMessageStore;

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
    public void onTabComplete(TabCompleteEvent e)
    {
        if (e.isCancelled()) return;

        var result = cmdHelper.onTabComplete(e.getBuffer(), e.getSender());
        if (result != null) e.setCompletions(result);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        if (unMorphOnDeath.get())
            morphs.unMorph(e.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent e)
    {
        var player = e.getPlayer();

        var state = morphs.getDisguiseStateFor(e.getPlayer());
        if (state != null)
        {
            state.getAbilities().forEach(a -> a.applyToPlayer(player, state));

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
                //如果伤害是0，那么取消事件
                if (e.getDamage() > 0d)
                    state.setSkillCooldown(Math.max(state.getSkillCooldown(), cooldownOnDamage.get()));
            }
        }
    }

    @Initializer
    private void load()
    {
        config.bind(cooldownOnDamage, ConfigOption.SKILL_COOLDOWN_ON_DAMAGE);
        config.bind(bruteIgnoreDisguises, ConfigOption.PIGLIN_BRUTE_IGNORE_DISGUISES);

        unMorphOnDeath = config.getBindable(Boolean.class, ConfigOption.UNMORPH_ON_DEATH);
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

                if (state.getDisguise().getWatcher() instanceof AbstractHorseWatcher watcher)
                {
                    var slot = e.getHand();
                    var item = e.getPlayer().getEquipment().getItem(slot);

                    if (item.getType() == Material.SADDLE)
                        watcher.setSaddled(true);
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

        if (mainHandItemType.isAir()) return false;

        if (player.isSneaking())
        {
            //右键玩家头颅：快速伪装
            if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.isLeftClick() && morphs.doQuickDisguise(player, actionItem))
            {
                return true;
            }
            else if (mainHandItemType == actionItem)
            {
                //主动技能或快速变形
                if (state != null)
                {
                    if (action.isLeftClick())
                    {
                        if (player.getEyeLocation().getDirection().getY() <= -0.95)
                            morphs.unMorph(player);
                        else
                            morphs.setSelfDisguiseVisible(player, state.getServerSideSelfVisible(), true);

                        return true;
                    }

                    if (state.getSkillCooldown() <= 0)
                        morphs.executeDisguiseSkill(player);
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
            }
        }

        return false;
    }

    //region LibsDisguises workaround

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state != null)
        {
            //workaround: 启用服务端预览的情况下会造成物品栏desync
            if (state.getServerSideSelfVisible())
            {
                this.addSchedule(() ->
                {
                    if (DisguiseAPI.isDisguised(player) && DisguiseAPI.isSelfDisguised(player))
                        player.updateInventory();
                }, 2);
            }

            //workaround: 交换副手后伪装有概率在左右手显示同一个物品
            if (state.showingDisguisedItems())
            {
                var disguise = state.getDisguise();
                state.swapHands();
                var equip = state.getDisguisedItems();

                var mainHand = itemOrAir(equip.getItemInMainHand());
                var offHand = itemOrAir(equip.getItemInOffHand());

                if (clientHandler.clientVersionCheck(player, 3))
                {
                    clientHandler.sendClientCommand(player, new S2CSwapCommand());
                }
                else
                {
                    clientHandler.sendClientCommand(player, new S2CSetEquipCommand(mainHand, EquipmentSlot.HAND));
                    clientHandler.sendClientCommand(player, new S2CSetEquipCommand(offHand, EquipmentSlot.OFF_HAND));
                }

                this.addSchedule(() ->
                {
                    if (!state.showingDisguisedItems() || state.getDisguise() != disguise) return;

                    var watcher = state.getDisguise().getWatcher();

                    var air = itemOrAir(null);
                    watcher.setItemInMainHand(air);
                    watcher.setItemInOffHand(air);

                    watcher.setItemInMainHand(mainHand);
                    watcher.setItemInOffHand(offHand);
                }, 2);
            }
        }
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

    //非Premium版本的LibsDisguises不会为玩家保存伪装
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        clientHandler.markPlayerReady(player);

        if (clientHandler.clientConnected(player))
        {
            var config = morphs.getPlayerConfiguration(player);

            if (!config.shownMorphClientHint && config.getUnlockedDisguiseIdentifiers().size() > 0)
                this.addSchedule(() ->
                {
                    if (player.isOnline() && !config.shownMorphClientHint)
                    {
                        player.sendMessage(MessageUtils.prefixes(player, HintStrings.firstGrantClientHintString()));

                        config.shownMorphClientHint = true;
                    }
                }, 20 * 3);
        }

        if (state != null)
        {
            //重新进入后player和info.player不属于同一个实例，需要重新disguise
            state.setPlayer(player);
            DisguiseAPI.disguiseEntity(player, state.getDisguise());

            var disguise = DisguiseAPI.getDisguise(player);
            DisguiseUtils.addTrace(disguise);

            //刷新Disguise
            var nbt = state.getCachedNbtString();
            var profile = state.getProfileNbtString();

            var customName = state.entityCustomName;

            state.setDisguise(state.getDisguiseIdentifier(),
                    state.getSkillLookupIdentifier(), DisguiseAPI.getDisguise(player), state.shouldHandlePose(), false,
                    state.getDisguisedItems());

            state.setCachedNbtString(nbt);
            state.setCachedProfileNbtString(profile);

            if (customName != null)
            {
                state.entityCustomName = customName;
                state.setDisplayName(customName);
            }

            state.refreshSkills();

            //调用Morph事件
            Bukkit.getPluginManager().callEvent(new PlayerMorphEvent(player, state));

            return;
        }

        var offlineState = morphs.getOfflineState(player);

        if (offlineState == null && DisguiseAPI.isDisguised(player))
        {
            //移除未跟踪，未保存并且属于此插件的伪装
            var disguise = DisguiseAPI.getDisguise(player);

            if (DisguiseUtils.isTracing(disguise))
                disguise.removeDisguise(player);
        }
        else if (offlineState != null)
        {
            player.sendMessage(MessageUtils.prefixes(player, MorphStrings.stateRecoverReasonString()));

            if (morphs.disguiseFromOfflineState(player, offlineState))
            {
                if (offlineState.disguise != null)
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateString()));
                }
                else
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateLimitedString()));
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringStateLimitedHintString()));
                }
            }
            else
                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.recoveringFailedString()));
        }
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        clientHandler.unInitializePlayer(e.getPlayer());
        skillHandler.removeUnusedList(e.getPlayer());

        var state = morphs.getDisguiseStateFor(e.getPlayer());

        if (state != null)
        {
            var bossbar = state.getBossbar();

            if (bossbar != null)
                Bukkit.getOnlinePlayers().forEach(p -> p.hideBossBar(bossbar));
        }
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
            var abilities = state.getAbilities();

            if (abilities != null)
                abilities.forEach(a -> a.applyToPlayer(player, state));
        }
    }

    //解决LibsDisguises中MonstersIgnoreDisguises会忽视PlayerDisguise的问题
    @EventHandler
    public void onEntityTarget(EntityTargetEvent e)
    {
        if (e.getTarget() == null) return;

        if (e.getEntity().getType() == EntityType.PIGLIN_BRUTE && bruteIgnoreDisguises.get())
            return;

        if (e.getTarget() instanceof Player player && !e.getEntity().getType().equals(EntityType.WARDEN))
        {
            //受到外力攻击或者其他原因时不要处理
            switch (e.getReason())
            {
                case TARGET_ATTACKED_ENTITY, TARGET_ATTACKED_NEARBY_ENTITY,
                        REINFORCEMENT_TARGET, FOLLOW_LEADER,
                        TARGET_ATTACKED_OWNER, OWNER_ATTACKED_TARGET, CUSTOM ->
                {
                    return;
                }

                default -> {}
            }

            //目标玩家没在伪装时不要处理
            if (!DisguiseAPI.isDisguised(player)) return;

            var disguise = DisguiseAPI.getDisguise(player);

            var sourceEntityType = e.getEntity().getType();
            var disguiseEntityType = disguise.getType().getEntityType();

            //检查是否要取消Target
            boolean shouldTarget = switch (sourceEntityType)
                    {
                        case ZOMBIE, ZOMBIE_VILLAGER, HUSK, DROWNED -> EntityTypeUtils.isZombiesHostile(disguiseEntityType);
                        case SKELETON, STRAY -> EntityTypeUtils.isGolem(disguiseEntityType) || disguise.isPlayerDisguise();
                        case PIGLIN -> EntityTypeUtils.isPiglinHostile(disguiseEntityType);
                        case PIGLIN_BRUTE -> EntityTypeUtils.isBruteHostile(disguiseEntityType);
                        case WITHER_SKELETON -> EntityTypeUtils.isWitherSkeletonHostile(disguiseEntityType);
                        case GUARDIAN, ELDER_GUARDIAN -> EntityTypeUtils.isGuardianHostile(disguiseEntityType);
                        case WITHER -> EntityTypeUtils.isWitherHostile(disguiseEntityType);
                        case PILLAGER, VEX, ILLUSIONER, VINDICATOR, EVOKER, RAVAGER -> EntityTypeUtils.isRaiderHostile(disguiseEntityType);
                        case ENDERMAN -> disguiseEntityType == EntityType.PLAYER || disguiseEntityType == EntityType.ENDERMITE;
                        case ZOGLIN -> EntityTypeUtils.isZoglinHostile(disguiseEntityType);
                        default -> disguise.isPlayerDisguise();
                    };

            e.setCancelled(e.isCancelled() || !shouldTarget);
        }
    }

    //endregion LibsDisguises workaround

    private void onPlayerKillEntity(Player player, Entity entity)
    {
        if (!(entity instanceof LivingEntity) && !(entity.getType() == EntityType.ARMOR_STAND))
            return;

        if (entity instanceof Player targetPlayer)
            morphs.grantMorphToPlayer(player, DisguiseTypes.PLAYER.toId(targetPlayer.getName()));
        else
            morphs.grantMorphToPlayer(player, entity.getType().getKey().asString());
    }
}
