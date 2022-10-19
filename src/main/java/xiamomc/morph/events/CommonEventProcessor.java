package xiamomc.morph.events;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.MorphSkillHandler;
import xiamomc.morph.abilities.AbilityFlag;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseUtils;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.misc.MorphChatRenderer;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CommonEventProcessor extends MorphPluginObject implements Listener
{
    @Resolved
    private MorphCommandHelper cmdHelper;

    @Resolved
    private MorphManager morphs;

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
        morphs.unMorph(e.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        //workaround: ChatManager与我们的聊天覆盖八字不和，只能用自己的Renderer
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state == null || !state.getDisguise().isPlayerDisguise()) return;

        if (morphs.allowChatOverride() && useCustomRenderer)
            e.renderer(new MorphChatRenderer());
        else
        {
            //noinspection OverrideOnly
            e.renderer().render(player, state.getDisplayName(), e.message(), player);
        }
    }

    private int cooldownOnDamage;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTookDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player player)
        {
            var state = morphs.getDisguiseStateFor(player);
            var cause = e.getCause();

            if (state != null)
            {
                switch (cause)
                {
                    case FALL ->
                    {
                        if (state.isAbilityFlagSet(AbilityFlag.NO_FALL_DAMAGE))
                            e.setDamage(0d);
                        else if (state.isAbilityFlagSet(AbilityFlag.REDUCES_FALL_DAMAGE))
                            e.setDamage(Math.max(0d, e.getDamage() - 10d));
                    }

                    case MAGIC ->
                    {
                        if (state.isAbilityFlagSet(AbilityFlag.REDUCES_MAGIC_DAMAGE))
                            e.setDamage(e.getDamage() * 0.15d);

                        //亡灵暂时无法实现，因为我们没法确定此魔法伤害是什么造成的
                    }

                    case FREEZE ->
                    {
                        if (state.isAbilityFlagSet(AbilityFlag.SNOWY))
                            e.setDamage(0d);
                    }
                }

                //如果伤害是0，那么取消事件
                if (e.getDamage() <= 0d)
                    e.setCancelled(true);
                else //否则，设置CD
                    state.setSkillCooldown(Math.max(state.getSkillCooldown(), cooldownOnDamage));
            }
        }
    }

    @Resolved
    private MorphConfigManager config;

    private boolean allowHeadMorph;
    private void setAllowHeadMorph(boolean val)
    {
        if (allowHeadMorph == val) return;

        allowHeadMorph = val;
        config.set(ConfigOption.ALLOW_HEAD_MORPH, val);
    }

    private boolean useCustomRenderer;

    @Initializer
    private void load()
    {
        config.onConfigRefresh(c -> onConfigRefresh(), true);
    }

    private void onConfigRefresh()
    {
        setAllowHeadMorph(config.getOrDefault(Boolean.class, ConfigOption.ALLOW_HEAD_MORPH, true));

        useCustomRenderer = config.getOrDefault(Boolean.class, ConfigOption.CHAT_OVERRIDE_USE_CUSTOM_RENDERER, true);
        cooldownOnDamage = config.getOrDefault(Integer.class, ConfigOption.SKILL_COOLDOWN_ON_DAMAGE);

        var actionItemId = config.getOrDefault(String.class, ConfigOption.ACTION_ITEM);
        var item = Material.matchMaterial(actionItemId);

        if (item == null && !actionItemId.equals("disabled"))
            Logger.warn("未能找到和" + actionItem + "对应的物品，相关功能将不会启用");

        actionItem = item;
    }

    @EventHandler
    public void onPlayerUnMorph(PlayerUnMorphEvent e)
    {
        uuidPlayerTexturesMap.remove(e.getPlayer().getUniqueId());
    }

    private final Map<UUID, PlayerTextures> uuidPlayerTexturesMap = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e)
    {
        if (e.getRightClicked() instanceof ArmorStand)
            e.setCancelled(tryInvokeSkillOrQuickDisguise(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent e)
    {
        if (e.getRightClicked() instanceof Allay)
            e.setCancelled(tryInvokeSkillOrQuickDisguise(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (tryInvokeSkillOrQuickDisguise(e.getPlayer(), e.getAction(), e.getHand()))
            e.setCancelled(true);
    }

    @Resolved
    private MorphSkillHandler skillHandler;

    private Material actionItem;

    /**
     * 尝试使用技能或快速伪装
     * @param player 目标玩家
     * @param action 动作
     * @return 是否应该取消Interact事件
     */
    private boolean tryInvokeSkillOrQuickDisguise(Player player, Action action, EquipmentSlot slot)
    {
        if (slot != EquipmentSlot.HAND || actionItem == null) return false;

        var state = morphs.getDisguiseStateFor(player);
        var mainHandItem = player.getEquipment().getItemInMainHand();
        var mainHandItemType = mainHandItem.getType();

        if (player.isSneaking())
        {
            if (action.isLeftClick())
            {
                if (state != null
                        && mainHandItemType == actionItem
                        && player.getEyeLocation().getDirection().getY() == -1)
                {
                    morphs.unMorph(player);
                    return true;
                }

                return false;
            }

            //右键玩家头颅：快速伪装
            if (mainHandItemType == Material.PLAYER_HEAD)
            {
                //忽略shift点地
                if (action.equals(Action.RIGHT_CLICK_BLOCK)) return false;

                if (!allowHeadMorph)
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.headDisguiseDisabledString()));

                    return true;
                }

                if (!morphs.canMorph(player))
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseCoolingDownString()));

                    return true;
                }

                var profile = ((SkullMeta) mainHandItem.getItemMeta()).getPlayerProfile();

                //忽略没有profile的玩家伪装
                if (profile == null)
                {
                    player.sendMessage(MessageUtils.prefixes(player, MorphStrings.invalidSkinString()));
                    return true;
                }

                var name = profile.getName();
                var profileTexture = profile.getTextures();
                var playerUniqueId = player.getUniqueId();

                //如果玩家有伪装，并且伪装的材质和Profile中的一样，那么取消伪装
                if (state != null)
                {
                    var disguise = state.getDisguise();

                    if (disguise instanceof PlayerDisguise playerDisguise
                            && playerDisguise.getName().equals(name)
                            && profileTexture.equals(uuidPlayerTexturesMap.get(playerUniqueId)))
                    {
                        morphs.unMorph(player);
                        return true;
                    }
                }

                //否则，更新或应用伪装
                if (morphs.morphEntityTypeAuto(player, "player:" + profile.getName(), player.getTargetEntity(5)))
                {
                    //成功伪装后设置皮肤为头颅的皮肤
                    var disguise = (PlayerDisguise) DisguiseAPI.getDisguise(player);
                    var wrappedProfile = WrappedGameProfile.fromHandle(new MorphGameProfile(profile));

                    var LDprofile = ReflectionManager.getGameProfileWithThisSkin(wrappedProfile.getUUID(), wrappedProfile.getName(), wrappedProfile);

                    //LD不支持直接用profile设置皮肤，只能先存到本地设置完再移除
                    DisguiseAPI.addGameProfile(LDprofile.toString(), LDprofile);
                    disguise.setSkin(LDprofile);
                    DisguiseUtilities.removeGameProfile(LDprofile.toString());

                    uuidPlayerTexturesMap.put(playerUniqueId, profileTexture);
                    return true;
                }

                morphs.updateLastPlayerMorphOperationTime(player);
            }
            else if (mainHandItemType == actionItem)
            {
                //主动技能或快速变形
                if (state != null)
                {
                    if (state.getSkillCooldown() <= 0)
                        morphs.executeDisguiseAbility(player);
                    else
                    {
                        //一段时间内内只接受一次右键触发
                        //传送前后会触发两次Interact，而且这两个Interact还不一定在同个Tick里
                        if (Plugin.getCurrentTick() - skillHandler.getLastInvoke(player) <= 1)
                            return true;

                        player.sendMessage(MessageUtils.prefixes(player,
                                SkillStrings.skillPreparing().resolve("time", state.getSkillCooldown() / 20 + "")));

                        player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                                Sound.Source.PLAYER, 1f, 1f));
                    }

                    return true;
                }
                else
                {
                    var targetedEntity = player.getTargetEntity(5);

                    if (targetedEntity != null)
                    {
                        var disg = DisguiseAPI.getDisguise(targetedEntity);

                        var targetKey = disg != null
                                ? (disg instanceof PlayerDisguise pd)
                                ? "player:" + pd.getName()
                                : disg.getType().getEntityType().getKey().asString()
                                : (targetedEntity instanceof Player targetPlayer)
                                ? "player:" + targetPlayer.getName()
                                : targetedEntity.getType().getKey().asString();

                        morphs.morphEntityTypeAuto(player, targetKey, targetedEntity);

                        return true;
                    }
                }
            }
        }

        return false;
    }

    //region LibsDisguises workaround

    //伪装时副手交换会desync背包
    @EventHandler
    public void onPlayerSwapHand(PlayerSwapHandItemsEvent e)
    {
        var player = e.getPlayer();
        if (DisguiseAPI.isDisguised(player))
        {
            //workaround: LibsDisguises在启用selfDisguiseVisible的情况下会导致副手切换异常
            this.addSchedule(c ->
            {
                if (DisguiseAPI.isDisguised(player) && DisguiseAPI.isSelfDisguised(player)) player.updateInventory();
            }, 2);
        }
    }

    //非Premium版本的LibsDisguises不会为玩家保存伪装
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (state != null)
        {
            //重新进入后player和info.player不属于同一个实例，需要重新disguise
            state.setPlayer(player);
            DisguiseAPI.disguiseEntity(player, state.getDisguise());

            var disguise = DisguiseAPI.getDisguise(player);
            DisguiseUtils.addTrace(disguise);

            //刷新Disguise
            state.setDisguise(DisguiseAPI.getDisguise(player), state.shouldHandlePose(), false);

            //更新飞行能力
            if (morphs.updateFlyingAbility(player) && player.getVelocity().getY() == 0)
                player.setFlying(true);

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
        };
    }

    @EventHandler
    public void onPlayerExit(PlayerQuitEvent e)
    {
        skillHandler.removeUnusedList(e.getPlayer());
    }

    //解决LibsDisguises中MonstersIgnoreDisguises会忽视PlayerDisguise的问题
    @EventHandler
    public void onEntityTarget(EntityTargetEvent e)
    {
        if (e.getTarget() == null) return;

        if (e.getTarget() instanceof Player player && !e.getEntity().getType().equals(EntityType.WARDEN))
        {
            //受到外力攻击或者其他原因时不要处理
            switch (e.getReason())
            {
                case TARGET_ATTACKED_ENTITY:
                case TARGET_ATTACKED_OWNER:
                case OWNER_ATTACKED_TARGET:
                case CUSTOM:
                    return;

                default:
                    break;
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
            morphs.grantPlayerMorphToPlayer(player, targetPlayer.getName());
        else
            morphs.grantMorphToPlayer(player, entity.getType());
    }
}
