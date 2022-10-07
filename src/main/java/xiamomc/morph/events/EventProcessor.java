package xiamomc.morph.events;

import dev.geco.gsit.api.event.*;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.EquipmentSlot;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.misc.DisguiseUtils;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.List;

public class EventProcessor extends MorphPluginObject implements Listener
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
    public void onLeave(PlayerQuitEvent e)
    {
        gSitHandlingPlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e)
    {
        morphs.unMorph(e.getPlayer());
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent e)
    {
        var player = e.getPlayer();
        var state = morphs.getDisguiseStateFor(player);

        if (player.getEquipment().getItemInMainHand().getType().equals(Material.CARROT_ON_A_STICK)
                && player.isSneaking()
                && state != null
                && e.getHand() == EquipmentSlot.HAND
                && e.getAction().isRightClick())
        {
            if (state.getAbilityCooldown() <= 0)
                morphs.executeDisguiseAbility(player);
            else
                player.sendMessage(MessageUtils.prefixes(player, Component.translatable("技能正在冷却中")
                        .append(Component.text("(" + state.getAbilityCooldown() / 20 + "秒)"))
                        .color(NamedTextColor.RED)));
        }
    }

    //region GSit <-> LibsDisguises workaround

    @EventHandler
    public void onEntityGetUp(EntityGetUpSitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            showDisguiseFor(player);
    }

    private final List<Player> gSitHandlingPlayers = new ArrayList<>();

    @EventHandler
    public void onEntitySit(EntitySitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            hideDisguiseFor(player);
    }

    @EventHandler
    public void onEarlyPlayerPlayerSit(PrePlayerPlayerSitEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getTarget());

        if (!state.getDisguise().isPlayerDisguise())
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPlayerSit(PlayerPlayerSitEvent e)
    {
        gSitHandlingPlayers.add(e.getPlayer());
        hideDisguiseFor(e.getPlayer());
    }

    @EventHandler
    public void onPlayerGetUpPlayerSit(PlayerGetUpPlayerSitEvent e)
    {
        if (gSitHandlingPlayers.contains(e.getPlayer()))
        {
            showDisguiseFor(e.getPlayer());
            gSitHandlingPlayers.remove(e.getPlayer());
        }
    }

    //endregion  GSit <-> LibsDisguises workaround

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
            state.setDisguise(DisguiseAPI.getDisguise(player), state.isShouldHandlePose());

            //更新飞行能力
            if (morphs.updateFlyingAbility(player) && player.getVelocity().getY() == 0)
                player.setFlying(true);

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
            player.sendMessage(MessageUtils.prefixes(player, Component.text("自上次离线后相关功能已被重置")));

            if (morphs.disguiseFromOfflineState(player, offlineState))
            {
                if (offlineState.disguise != null)
                {
                    player.sendMessage(MessageUtils.prefixes(player, Component.text("我们正在恢复您的伪装")));
                }
                else
                {
                    player.sendMessage(MessageUtils.prefixes(player, Component.text("我们正从有限副本中恢复您的伪装")));
                    player.sendMessage(MessageUtils.prefixes(player, Component.text("恢复后的伪装可能和之前的不一样")
                            .decorate(TextDecoration.ITALIC)));
                }
            }
            else
                player.sendMessage(MessageUtils.prefixes(player, Component.text("我们无法恢复您的伪装 :(")
                        .color(NamedTextColor.RED)));
        };
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
                        case ZOGLIN -> true;
                        default -> disguise.isPlayerDisguise();
                    };

            e.setCancelled(!shouldTarget);
        }
    }

    //endregion LibsDisguises workaround

    private void hideDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
            DisguiseUtilities.removeSelfDisguise(DisguiseAPI.getDisguise(player));
    }

    private void showDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
            this.addSchedule(c ->
            {
                if (DisguiseAPI.isDisguised(player))
                    DisguiseUtilities.setupFakeDisguise(DisguiseAPI.getDisguise(player));
            });
    }

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
