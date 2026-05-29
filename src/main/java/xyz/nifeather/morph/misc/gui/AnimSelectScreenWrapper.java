package xyz.nifeather.morph.misc.gui;

import de.themoep.inventorygui.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xiamomc.pluginbase.Bindables.BindableList;
import xyz.nifeather.morph.messages.strings.EmoteStrings;
import xyz.nifeather.morph.messages.strings.GuiStrings;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.List;

public class AnimSelectScreenWrapper extends ScreenWrapper
{
    /*
    // 如果要启用这个，记得手动设置 capacity
    private final BindableList<String> pattern = new BindableList<>(
            List.of(
                    " " + getCurrentIndexChar(0) + " ",
                    getCurrentIndexChar(3) + "E" + getCurrentIndexChar(1),
                    " " + getCurrentIndexChar(2) + " "
    ));*/

    private final BindableList<String> pattern = new BindableList<>(List.of(
            "XXXXE"
    ));

    private List<String> getTemplate()
    {
        return pattern;
    }

    private final DisguiseState state;
    private final List<String> availableActions;

    public AnimSelectScreenWrapper(DisguiseState state, List<String> availableActions)
    {
        super(state.getPlayer());

        this.state = state;
        this.availableActions = availableActions;

        this.guiInstance = preparePage();
        this.initElements(this.guiInstance);
    }

    @Override
    public void show()
    {
        getBindingPlayer().playSound(openSound);

        super.show();
    }

    protected void parseItemLore(ItemMeta itemMeta, List<String> strings)
    {
        List<Component> loreList = new ObjectArrayList<>();

        strings.forEach(lore ->
        {
            Component component = Component.text("???");

            try
            {
                component = MiniMessage.miniMessage().deserialize(lore);
            }
            catch (Throwable t)
            {
                logger.error("Can't deserialize lore string '%s': %s".formatted(lore, t.getMessage()));
            }

            loreList.add(component);
        });

        itemMeta.lore(loreList);
    }

    protected void parseItemName(ItemMeta itemMeta, String s)
    {
        Component component = Component.text("???");

        try
        {
            component = MiniMessage.miniMessage().deserialize(s);
        }
        catch (Throwable t)
        {
            logger.error("Can't deserialize string '%s': %s".formatted(s, t.getMessage()));
        }

        itemMeta.itemName(component);
    }

    private InventoryGui preparePage()
    {
        //var columns = 9;
        var template = this.getTemplate();

        if (template.size() > 6)
        {
            logger.error("May not have a inventory with more than 6 rows.");
            return new InventoryGui(plugin, "missingno", new String[]{"         "});
        }

        List<String> rows = new ObjectArrayList<>();

        for (String line : template)
        {
            rows.add(line.toUpperCase());
        }

        // Build page
        var array = rows.toArray(new String[]{});

        var skel = new InventoryGui(plugin, GuiStrings.selectAnimation().createString(playerLocale), array);

        skel.setItemNameSetter(this::parseItemName);
        skel.setItemLoreSetter(this::parseItemLore);
        skel.setCloseAction(close -> false);

        return skel;
    }

    private void initElements(InventoryGui gui)
    {
        var defaultIcon = IconLookup.instance().lookup(state.getDisguiseIdentifier()); //new ItemStack(Material.LIGHT);

        if (IconLookup.instance().lookup(state.getDisguiseIdentifier()).getType() == Material.PLAYER_HEAD)
            this.isDynamic.set(true);

        var groupElement = new GuiElementGroup('X');

        var filler = ItemStack.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        filler.editMeta(meta ->
        {
            var name = EmoteStrings.none()
                    .createComponent(playerLocale)
                    .style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

            meta.customName(name);
        });

        groupElement.setFiller(filler);

        var animationSet = state.getProvider()
                .getAnimationProvider()
                .getAnimationSetFor(state.getDisguiseIdentifier());

        for (String actionName : availableActions)
        {
            var action = animationSet.getAction(actionName);
            if (action == null) continue;

            var icon = defaultIcon.clone();

            icon.editMeta(meta ->
            {
                var name = EmoteStrings.get(actionName)
                        .createComponent()
                        .style(Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));

                meta.setRarity(ItemRarity.COMMON);
                meta.customName(name);
            });

            var element = new StaticGuiElement('_', icon, 1 + availableActions.indexOf(actionName), click ->
            {
                getBindingPlayer().playSound(clickSound);
                state.tryScheduleAction(actionName, action);
                guiInstance.close();

                return true;
            });

            groupElement.addElement(element);
        }

        gui.addElement(groupElement);

        gui.addElement(new StaticGuiElement('!',
                new ItemStack(Material.PINK_STAINED_GLASS_PANE),
                1,
                click -> true,
                "<italic:false>"));

        var closeElementItem = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
        closeElementItem.editMeta(meta -> meta.setRarity(ItemRarity.COMMON));

        gui.addElement(new StaticGuiElement('E',
                closeElementItem,
                1,
                click ->
                {
                    getBindingPlayer().playSound(clickSound);
                    guiInstance.close();
                    return true;
                },
                "<italic:false>" + GuiStrings.close().createString(playerLocale)));
    }
}
