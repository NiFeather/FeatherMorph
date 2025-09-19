package xyz.nifeather.morph.events;

import de.themoep.inventorygui.InventoryGui;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DyedItemColor;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.events.gameplay.PlayerCollectMagicBottleEvent;
import xyz.nifeather.morph.api.events.gameplay.PlayerConsumeMagicBottleEvent;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.gui.AnimSelectScreenWrapper;
import xyz.nifeather.morph.misc.gui.DisguiseSelectScreenWrapper;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.utilities.ItemUtils;
import xyz.nifeather.morph.utilities.PermissionUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

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
                if (!disguiseState.skillInCooldown())
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

        var id = ItemUtils.readMagicItemData(consumedItem);
        if (id == null) return;

        var player = event.getPlayer();

        var consumeMagicBottleEvent = new PlayerConsumeMagicBottleEvent(player, consumedItem);
        var cancelled = !consumeMagicBottleEvent.callEvent();
        var hasPermission = PermissionUtils.hasPermission(player, CommonPermissions.MAGIC_BOTTLE_USE, true);

        //               不允许玩家获取自己的形态，即使我们的框架允许这样做
        if (cancelled || !hasPermission || (id.startsWith("player:") && DisguiseTypes.PLAYER.toStrippedId(id).equals(player.getName())))
        {
            player.playSound(player, Sound.ENTITY_VILLAGER_NO, 1, 1);
            event.setCancelled(true);
            return;
        }

        morphs.grantMorphToPlayer(player, id);
    }

    public final TagKey<@NotNull EntityType> tagMagicBottleCollectable = TagKey.create(RegistryKey.ENTITY_TYPE,
            Objects.requireNonNull(NamespacedKey.fromString("feathermorph:magic_bottle_collectable")));

    // Collect

    @NotNull
    public final NamespacedKey collectedMagicBottleLootKey = Objects.requireNonNull(NamespacedKey.fromString("feathermorph:magic_bottle_template"));

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void bottleOnPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        var player = event.getPlayer();

        var mainhandItem = player.getEquipment().getItem(event.getHand());
        if (!ItemUtils.isMagicItem(mainhandItem) || event.getHand() != EquipmentSlot.HAND || !player.isSneaking())
            return;

        if (ItemUtils.readMagicItemData(mainhandItem) != null)
            return;

        var entityClicked = event.getRightClicked();

        var collectMagicBottleEvent = new PlayerCollectMagicBottleEvent(player, entityClicked);
        var cancelled = !collectMagicBottleEvent.callEvent();
        var collectorHasPermission = PermissionUtils.hasPermission(player, CommonPermissions.MAGIC_BOTTLE_USE, true);

        Runnable failEffect = () ->
        {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            player.swingHand(event.getHand());
        };

        // 如果事件被取消，或者没有权限，拒绝获取
        if (cancelled || !collectorHasPermission)
        {
            failEffect.run();
            return;
        }

        var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE);
        if (!registry.hasTag(tagMagicBottleCollectable))
        {
            failEffect.run();
            return;
        }

        var allowedTypes = registry.getTagValues(tagMagicBottleCollectable);
        if (!allowedTypes.contains(entityClicked.getType()))
        {
            failEffect.run();
            return;
        }

        // 根据点击的生物获取伪装ID
        String disguiseIdentifier = switch (entityClicked)
        {
            case Player playerEntity ->
            {
                if (PermissionUtils.hasPermission(playerEntity, CommonPermissions.MAGIC_BOTTLE_EXCLUDE, false))
                    yield null;

                yield DisguiseTypes.PLAYER.toId(playerEntity.getName());
            }
            case LivingEntity livingEntity -> livingEntity.getType().key().asString();
            default -> null;
        };

        if (disguiseIdentifier == null)
        {
            failEffect.run();
            return;
        }

        // 获取显示名
        Component displayName = switch (entityClicked)
        {
            case Player playerEntity -> Component.text(playerEntity.getName());
            case Entity entity -> Component.translatable(entity.getType().translationKey());
        };

        // 设置物品
        if (player.getGameMode() != GameMode.CREATIVE)
            mainhandItem.setAmount(mainhandItem.getAmount() - 1);

        LootTable lootTable = Bukkit.getLootTable(collectedMagicBottleLootKey);

        if (lootTable == null)
        {
            logger.error("Loot table '%s' not found! Failed to setup magic bottle!".formatted(collectedMagicBottleLootKey));
            failEffect.run();
            return;
        }

        LootContext lootContext = new LootContext.Builder(player.getLocation())
                .killer(player)
                .lootedEntity(entityClicked)
                .build();

        Collection<ItemStack> items = lootTable
                .populateLoot(ThreadLocalRandom.current(), lootContext)
                .stream().map(stack ->
                {
                    // If skipped, don't process
                    if (ItemUtils.skipMagicBottleSetup(stack))
                        return stack;

                    var itemColor = Color.fromARGB(disguiseIdentifier.hashCode());

                    // Override the potion color if it hasn't been set
                    //region Potion Data
                    PotionContents.Builder contentBuilder = PotionContents.potionContents().customColor(itemColor);
                    PotionContents potionContents = stack.getData(DataComponentTypes.POTION_CONTENTS);

                    if (potionContents != null)
                    {
                        var color = potionContents.customColor();
                        color = color == null ? itemColor : color;

                        contentBuilder.customColor(color)
                                .addCustomEffects(potionContents.customEffects())
                                .potion(potionContents.potion())
                                .customName(potionContents.customName());
                    }

                    stack.setData(DataComponentTypes.POTION_CONTENTS, contentBuilder);
                    //endregion Potion Data

                    // Set the `dyed_color` property is it hasn't been set
                    if (!stack.hasData(DataComponentTypes.DYED_COLOR))
                        stack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor().color(itemColor));

                    // Add lore to the item if no lore has been set
                    // The lore is used to tell the player which disguise this bottle would unlock
                    var currentLore = stack.getData(DataComponentTypes.LORE);

                    var referenceLore = Component.text(disguiseIdentifier)
                            .style(
                                    Style.style()
                                            .color(TextColor.color(0xAAAAAA))
                                            .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                                            .build()
                            );

                    if (currentLore != null && ItemUtils.alwaysAppendReferenceTooltip(stack))
                    {
                        var newLore = ItemLore.lore().addLines(currentLore.lines()).addLine(referenceLore);
                        stack.setData(DataComponentTypes.LORE, newLore);
                    }
                    else if (currentLore == null || currentLore.lines().isEmpty())
                    {
                        stack.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(referenceLore));
                    }

                    // We already have name setup in the loot table, do we really need this?
                    /*
                    // translate: 装有xxx气息的瓶子
                    var finalNameDisplay = Component.translatable("item.morphclient.bottle_with_disguise", "Magic Bottle of %s", displayName)
                            .style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build());

                    if (!stack.hasData(DataComponentTypes.CUSTOM_NAME))
                        stack.setData(DataComponentTypes.CUSTOM_NAME, finalNameDisplay);
                    */

                    return ItemUtils.writeMagicItemData(stack, disguiseIdentifier);
                }).toList();

        items.forEach(player::give);

        player.getWorld().playSound(player.getLocation(), Sound.ITEM_BOTTLE_FILL, 1, 1);
        player.swingHand(event.getHand());
        event.setCancelled(true);
    }

    //endregion Magic Bottle
}
