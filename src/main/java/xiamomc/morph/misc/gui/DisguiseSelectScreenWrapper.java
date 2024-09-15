package xiamomc.morph.misc.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.GuiStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DisguiseSelectScreenWrapper extends MorphPluginObject
{
    @NotNull
    private final InventoryGui gui;

    private final Player bindingPlayer;

    private final int pageOffset;

    private final List<DisguiseMeta> disguises;

    private final String playerLocale;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    /**
     * 获取此GUI的行数
     * @return
     */
    protected int getRowCount()
    {
        return 4;
    }

    /**
     * 获取此GUI的最大伪装显示物品承载量
     * @return
     */
    private int getElementCapacity()
    {
        // 行数 - 1(Footer)
        return ( getRowCount() - 1 ) * 9;
    }

    /**
     * 获取此页面在伪装列表中的起始Index
     * @return
     */
    private int getStartingIndex()
    {
        return this.pageOffset * (this.getRowCount() - 1) * 9;
    }

    public DisguiseSelectScreenWrapper(Player bindingPlayer, int pageOffset)
    {
        this.disguises = manager.getAvaliableDisguisesFor(bindingPlayer);
        this.bindingPlayer = bindingPlayer;
        this.pageOffset = pageOffset;
        this.playerLocale = MessageUtils.getLocale(bindingPlayer);

        this.gui = this.preparePage();
        initElements();
    }

    private final AtomicBoolean havePlayerHead = new AtomicBoolean(false);

    public void show()
    {
        this.gui.show(bindingPlayer);

        if (havePlayerHead.get())
            this.addSchedule(this::update);
    }

    private void update()
    {
        if (plugin.getCurrentTick() % 10 != 0)
        {
            this.addSchedule(this::update);
            return;
        }

        if (this.gui.equals(InventoryGui.getOpen(bindingPlayer)))
            this.addSchedule(this::update);

        this.gui.draw(bindingPlayer);
    }

    private InventoryGui preparePage()
    {
        var columns = 9;

        List<String> rows = new ObjectArrayList<>();
        var index = this.getStartingIndex();

        StringBuilder builder = new StringBuilder();

        // Build element slots
        for (int x = 1; x <= this.getRowCount() - 1; x++)
        {
            for (int y = 1; y <= columns; y++)
            {
                index++;
                builder.append(this.getElementCharAt(index));
            }

            rows.add(builder.toString());
            builder = new StringBuilder();
        }

        // Build footer slots
        var isLast = isLastPage();
        var isFirst = this.pageOffset == 0;

        builder.append(isFirst ? "x" : "P").append("xxxUxxx").append(isLast ? "x" : "N");
        rows.add(builder.toString());

        // Build page
        var array = rows.toArray(new String[]{});

        var page = new InventoryGui(plugin, GuiStrings.selectDisguise().toString(playerLocale), array);

        page.setCloseAction(close -> false);

        return page;
    }

    private char getElementCharAt(int index)
    {
        return (char)(1000 + index);
    }

    private void initElements()
    {
        // Fill disguise entries
        var endIndex = Math.min(disguises.size(), getStartingIndex() + getElementCapacity() + 1);
        for (int index = getStartingIndex(); index < endIndex; index++)
        {
            var meta = disguises.get(index);
            var item = IconLookup.instance().lookup(meta.rawIdentifier);

            var staticElement = new StaticGuiElement(this.getElementCharAt(index),
                    item,
                    1,
                    click ->
                    {
                        manager.morph(bindingPlayer, bindingPlayer, meta.rawIdentifier, bindingPlayer.getTargetEntity(5));
                        gui.close();

                        return true;
                    },
                    // They don't seem to support Components... Sad :(
                    "§r" + PlainTextComponentSerializer.plainText().serialize(meta.asComponent(playerLocale)));

            if (meta.isPlayerDisguise())
            {
                this.havePlayerHead.set(true);
                gui.addElement(new DynamicGuiElement(this.getElementCharAt(index), viewer -> staticElement));
            }
            else
            {
                gui.addElement(staticElement);
            }
        }

        // Fill controls
        var borderElement = new StaticGuiElement('x',
                new ItemStack(Material.PINK_STAINED_GLASS_PANE),
                1,
                click -> true,
                "§§");

        gui.addElement(borderElement);

        var prevButton = new StaticGuiElement('P',
                new ItemStack(Material.LIME_STAINED_GLASS_PANE),
                1,
                click ->
                {
                    schedulePrevPage();
                    return true;
                },
                "§r" + GuiStrings.prevPage().toString(playerLocale));

        gui.addElement(prevButton);

        var nextButton = new StaticGuiElement('N',
                new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE),
                1,
                click ->
                {
                    scheduleNextPage();
                    return true;
                },
                "§r" + GuiStrings.nextPage().toString(playerLocale));

        gui.addElement(nextButton);

        var unDisguiseButton = new StaticGuiElement('U',
                new ItemStack(Material.RED_STAINED_GLASS_PANE),
                1,
                click ->
                {
                    manager.unMorph(bindingPlayer);
                    this.gui.close();
                    return true;
                },
                "§r" + GuiStrings.unDisguise().toString(playerLocale));

        gui.addElement(unDisguiseButton);
    }

    private boolean isLastPage()
    {
        return (this.getStartingIndex() + this.getElementCapacity()) >= disguises.size();
    }

    @Nullable
    private Runnable scheduledAction;

    private void scheduleNextPage()
    {
        if (isLastPage()) return;

        if (scheduledAction != null) return;

        scheduledAction = () ->
        {
            var next = new DisguiseSelectScreenWrapper(bindingPlayer, this.pageOffset + 1);
            next.show();
        };

        this.addSchedule(scheduledAction);
    }

    private void schedulePrevPage()
    {
        if (this.pageOffset == 0) return;

        if (scheduledAction != null) return;

        scheduledAction = () ->
        {
            var next = new DisguiseSelectScreenWrapper(bindingPlayer, this.pageOffset - 1);
            next.show();
        };

        this.addSchedule(scheduledAction);
    }
}
