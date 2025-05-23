package xyz.nifeather.morph.events;

import de.themoep.inventorygui.InventoryGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.RGBLike;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.events.gameplay.PlayerCollectMagicBottleEvent;
import xyz.nifeather.morph.api.events.gameplay.PlayerConsumeMagicBottleEvent;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.gui.AnimSelectScreenWrapper;
import xyz.nifeather.morph.misc.gui.DisguiseSelectScreenWrapper;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.List;

public class CustomItemRelatedEvents extends MorphPluginObject implements Listener
{
    //region Disguise Tool

    @EventHandler
    public void onEntityHurtEntity(EntityDamageByEntityEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        if (event.getDamager() instanceof Player player
                && invokeDisguiseTool(player, Action.LEFT_CLICK_AIR, EquipmentSlot.HAND))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void disguiseToolOnPlayerInteractAtEntity(PlayerInteractAtEntityEvent e)
    {
        //workaround: 右键盔甲架不会触发事件、盔甲架是InteractAtEntityEvent
        if (e.getRightClicked() instanceof ArmorStand)
            e.setCancelled(invokeDisguiseTool(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void disguiseToolOnPlayerInteractEntity(PlayerInteractEntityEvent e)
    {
        //workaround: 右键继承了InventoryHolder的实体会打开他们的物品栏而不是使用技能
        if (e.getRightClicked() instanceof InventoryHolder && e.getRightClicked().getType() != EntityType.PLAYER)
            e.setCancelled(invokeDisguiseTool(e.getPlayer(), Action.RIGHT_CLICK_AIR, e.getHand()) || e.isCancelled());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (invokeDisguiseTool(e.getPlayer(), e.getAction(), e.getHand()))
            e.setCancelled(true);
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphs;

    /**
     * 尝试使用技能或快速伪装
     * @param player 目标玩家
     * @param action 动作
     * @return 是否应该取消Interact事件
     */
    private boolean invokeDisguiseTool(Player player, Action action, EquipmentSlot slot)
    {
        var mainHandItem = player.getEquipment().getItemInMainHand();
        if (mainHandItem.getType().isAir())
            return false;

        if (!action.isLeftClick() && !action.isRightClick()) return false;

        var disguiseState = morphs.getDisguiseStateFor(player);

        if (!ItemUtils.isDisguiseTool(mainHandItem))
            return false;

        // 因为快速伪装功能包含了玩家头颅，所以我们没有在上面检查物品是否为技能触发物品。
        if (player.isSneaking())
        {
            if (action.isRightClick()) // 下蹲+右键：快速伪装、打开伪装菜单
            {
                boolean isDisguiseTool = ItemUtils.isDisguiseTool(mainHandItem);
                boolean isValidItem = mainHandItem.getType() == Material.PLAYER_HEAD || isDisguiseTool;

                if (!isValidItem || action == Action.RIGHT_CLICK_BLOCK) return false;

                if (!morphs.tryQuickDisguise(player))
                {
                    if (isDisguiseTool && InventoryGui.getOpen(player) == null)
                    {
                        var guiScreen = new DisguiseSelectScreenWrapper(player);
                        guiScreen.show();
                    }

                    return true;
                }

                return false;
            }
            else // 下蹲+左键：取消伪装
            {
                if (disguiseState == null)
                    return false;

                morphs.unMorph(player);
            }
        }
        else
        {
            if (disguiseState == null)
                return false;

            if (action.isRightClick()) // 站立+右键：技能
            {
                if (disguiseState.getSkillCooldown() < 0)
                    morphs.executeDisguiseSkill(player);
            }
            else // 站立+左键：伪装动作
            {
                if (InventoryGui.getOpen(player) == null)
                {
                    var availableAnimations = disguiseState.getProvider()
                            .getAnimationProvider()
                            .getAnimationSetFor(disguiseState.getDisguiseIdentifier())
                            .getAvailableAnimationsForClient();

                    var guiScreen = new AnimSelectScreenWrapper(disguiseState, availableAnimations);
                    guiScreen.show();
                }
            }
        }

        return true;
    }

    //endregion Disguise Tool

    //region Magic Bottle

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event)
    {
        var consumedItem = event.getItem();
        if (!ItemUtils.isMagicBottle(consumedItem)) return;

        var id = ItemUtils.readMagicBottleData(consumedItem);
        if (id == null) return;

        var player = event.getPlayer();

        var consumeMagicBottleEvent = new PlayerConsumeMagicBottleEvent(player);
        var cancelled = !consumeMagicBottleEvent.callEvent();

        //               不允许玩家获取自己的形态，即使我们的框架允许这样做
        if (cancelled || (id.startsWith("player:") && DisguiseTypes.PLAYER.toStrippedId(id).equals(player.getName())))
        {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
            event.setCancelled(true);
            return;
        }

        morphs.grantMorphToPlayer(player, id);

        if (player.getGameMode() != GameMode.CREATIVE)
            event.setReplacement(ItemStack.of(Material.GLASS_BOTTLE));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bottleOnPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        var player = event.getPlayer();

        var mainhandItem = player.getEquipment().getItem(event.getHand());
        if (!ItemUtils.isMagicBottle(mainhandItem) || event.getHand() != EquipmentSlot.HAND || !player.isSneaking())
            return;

        if (ItemUtils.readMagicBottleData(mainhandItem) != null)
            return;

        var entityClicked = event.getRightClicked();

        var collectMagicBottleEvent = new PlayerCollectMagicBottleEvent(player, entityClicked);
        var cancelled = !collectMagicBottleEvent.callEvent();

        // 如果目标实体是怪物，或者物品数量大于1，或者事件被取消，拒绝获取
        if (entityClicked instanceof Monster || mainhandItem.getAmount() > 1 || cancelled)
        {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            player.swingHand(event.getHand());
            return;
        }

        // 根据点击的生物获取伪装ID
        String disguiseIdentifier = switch (entityClicked)
        {
            case Player playerEntity -> DisguiseTypes.PLAYER.toId(playerEntity.getName());
            case LivingEntity livingEntity -> livingEntity.getType().key().asString();
            default -> null;
        };

        if (disguiseIdentifier == null) return;

        // 获取显示名
        Component displayName = switch (entityClicked)
        {
            case Player playerEntity -> Component.text(playerEntity.getName());
            case Entity entity -> Component.translatable(entity.getType().translationKey());
        };

        // 设定物品
        var newItem = ItemUtils.writeMagicBottleData(mainhandItem, disguiseIdentifier);
        newItem.editMeta(PotionMeta.class, meta ->
        {
            var finalLoreDisplay = Component.text(disguiseIdentifier)
                    .style(
                            Style.style()
                                    .color(TextColor.color(0xAAAAAA))
                                    .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                    .build()
                    );

            meta.setColor(Color.fromARGB(disguiseIdentifier.hashCode()));
            meta.lore(List.of(finalLoreDisplay));

            // 装有xxx气息的瓶子
            var finalNameDisplay = Component.translatable("item.morphclient.bottle_with_disguise", "Magic Bottle of %s", displayName)
                    .style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build());
            meta.displayName(finalNameDisplay);
        });

        // 设置物品
        player.getEquipment().setItem(event.getHand(), newItem);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1, 1);
        player.swingHand(event.getHand());
        event.setCancelled(true);
    }

    //endregion Magic Bottle
}
