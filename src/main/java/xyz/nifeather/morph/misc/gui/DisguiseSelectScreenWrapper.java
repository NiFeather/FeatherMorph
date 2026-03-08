package xyz.nifeather.morph.misc.gui;

import de.themoep.inventorygui.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.messages.strings.GuiStrings;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.List;

public class DisguiseSelectScreenWrapper extends ScreenWrapper
{
    @Nullable
    private final DisguiseState bindingState;

    private final List<DisguiseMeta> disguises;

    private final boolean playOpenSound;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    public DisguiseSelectScreenWrapper(Player bindingPlayer)
    {
        this(bindingPlayer, true);
    }

    protected DisguiseSelectScreenWrapper(Player bindingPlayer, boolean playOpenSound)
    {
        super(bindingPlayer);

        this.disguises = manager.getAvailableDisguisesFor(bindingPlayer);
        this.bindingState = manager.getDisguiseStateFor(bindingPlayer);
        this.playOpenSound = playOpenSound;

        this.template.clear();
        this.template.addAll(config.getBindableList(String.class, ConfigOptions.GUI_PATTERN));

        this.guiInstance = this.preparePage();
        initElements(this.guiInstance);
    }

    private final List<String> template = ObjectArrayList.of(
            "CxDDDxPUN"
    );

    private List<String> getTemplate()
    {
        return template;
    }

    @Override
    public void show()
    {
        super.show();

        if (playOpenSound)
            getBindingPlayer().playSound(openSound);
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
            if (line.length() != 9)
            {
                logger.warn("A line cannot have more or less than 9 characters, ignoring '%s'".formatted(line));
                continue;
            }

            rows.add(line.toUpperCase());
        }

        // Build page
        var array = rows.toArray(new String[]{});

        var skel = new InventoryGui(plugin, GuiStrings.selectDisguise().createString(playerLocale), array);

        skel.setItemNameSetter(this::parseItemName);
        skel.setItemLoreSetter(this::parseItemLore);
        skel.setCloseAction(close -> false);

        return skel;
    }

    private GuiElement getPageFill(DisguiseMeta meta)
    {
        var identifier = meta.rawIdentifier;
        var bindingPlayer = getBindingPlayer();
        var item = IconLookup.instance().lookup(identifier);

        item.editMeta(m ->
        {
            m.customName(meta.asComponent(playerLocale).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        });

        GuiElement element = new StaticGuiElement('x',
                item,
                1,
                click ->
                {
                    bindingPlayer.playSound(clickSound);
                    manager.morph(bindingPlayer, bindingPlayer, identifier, bindingPlayer.getTargetEntity(5));
                    guiInstance.close();

                    return true;
                });

        if (meta.isPlayerDisguise())
            return new DynamicGuiElement('x', () -> element);
        else
            return element;
    }

    private void initElements(InventoryGui guiInstance)
    {
        var bindingPlayer = getBindingPlayer();

        var groupElement = new GuiElementGroup('D');

        var fallback = ItemStack.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        fallback.editMeta(meta -> meta.customName(Component.empty()));

        for (var disguiseMeta : this.disguises)
        {
            var element = getPageFill(disguiseMeta);

            if (disguiseMeta.isPlayerDisguise())
                this.isDynamic.set(true);

            groupElement.addElement(element);
        }

        guiInstance.addElement(groupElement);

        // Fill controls
        var nextPageElement = new PagerWithFallbackItem('N',
                ItemStack.of(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
                fallback,
                GuiPageElement.PageAction.NEXT,
                GuiStrings.nextPage().createString(playerLocale));

        guiInstance.addElement(nextPageElement);

        var lastPageElement = new PagerWithFallbackItem('P',
                ItemStack.of(Material.LIME_STAINED_GLASS_PANE),
                fallback,
                GuiPageElement.PageAction.PREVIOUS,
                GuiStrings.prevPage().createString(playerLocale));

        guiInstance.addElement(lastPageElement);

        guiInstance.setPageNumber(1);

        var borderElement = new StaticGuiElement('X',
                new ItemStack(Material.PINK_STAINED_GLASS_PANE),
                1,
                click -> true,
                "<i></i>");

        guiInstance.addElement(borderElement);

        var unDisguiseButton = new StaticGuiElement('U',
                new ItemStack(Material.RED_STAINED_GLASS_PANE),
                1,
                click ->
                {
                    bindingPlayer.playSound(clickSound);
                    manager.unMorph(bindingPlayer);
                    this.guiInstance.close();
                    return true;
                },
                "<italic:false>" + GuiStrings.unDisguise().createString(playerLocale));

        guiInstance.addElement(unDisguiseButton);

        // Current display
        if (bindingState != null)
        {
            var name = "<italic:false>" + MorphStrings.disguisingAsString().resolve("what", bindingState.getPlayerDisplay())
                    .createString(playerLocale);

            var currentDisguiseButton = new StaticGuiElement('C',
                    IconLookup.instance().lookup(bindingState.getDisguiseIdentifier()),
                    1,
                    click -> true,
                    name);

            guiInstance.addElement(currentDisguiseButton);
        }
        else
        {
            var disguisePlaceholderElement = new StaticGuiElement('C',
                    ItemStack.of(Material.PINK_STAINED_GLASS_PANE),
                    1,
                    click -> true,
                    "<i></i>");

            guiInstance.addElement(disguisePlaceholderElement);
        }
    }
}
