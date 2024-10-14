package xyz.nifeather.morph.misc.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.papermc.paper.adventure.providers.MiniMessageProviderImpl;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Bindables.BindableList;
import xyz.nifeather.morph.messages.EmoteStrings;
import xyz.nifeather.morph.messages.GuiStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.MorphStrings;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.List;
import java.util.function.Consumer;

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

    private static final char CHAR_ENTRY = 'X';

    private List<String> getTemplate()
    {
        return pattern;
    }

    private final DisguiseState state;
    private final List<String> availableSequences;

    public AnimSelectScreenWrapper(DisguiseState state, List<String> availableSequences)
    {
        super(state.getPlayer());

        this.state = state;
        this.availableSequences = availableSequences;

        this.guiInstance = preparePage();
        this.initElements(this.guiInstance);
    }

    @Override
    public void show()
    {
        getBindingPlayer().playSound(openSound);

        super.show();
    }

    private int capacity = 0;

    private char getCurrentIndexChar(int index)
    {
        return (char) (10000 + index);
    }

    private InventoryGui preparePage()
    {
        var template = this.getTemplate();

        if (template.size() > 6)
        {
            capacity = 0;
            logger.error("May not have a inventory with more than 6 rows.");
            return new InventoryGui(plugin, "missingno", new String[]{"         "});
        }

        List<String> rows = new ObjectArrayList<>();

        for (String line : template)
        {
            StringBuilder builder = new StringBuilder();

            int lineCapacity = 0;
            for (char c : line.toCharArray())
            {
                switch (c)
                {
                    case CHAR_ENTRY ->
                    {
                        builder.append(getCurrentIndexChar(this.capacity + lineCapacity));

                        lineCapacity++;
                    }
                    default ->
                    {
                        builder.append(c);
                    }
                }
            }

            rows.add(builder.toString());
            capacity += lineCapacity;
        }

        var gui = new InventoryGui(plugin,
                GuiStrings.selectAnimation().toString(playerLocale),
                rows.toArray(new String[]{}));

        gui.setItemNameSetter((meta, string) -> meta.itemName(defaultMiniMessage.deserialize(string)));

        return gui;
    }

    private void initElements(InventoryGui gui)
    {
        var defaultIcon = IconLookup.instance().lookup(state.getDisguiseIdentifier()); //new ItemStack(Material.LIGHT);

        if (IconLookup.instance().lookup(state.getDisguiseIdentifier()).getType() == Material.PLAYER_HEAD)
            this.isDynamic.set(true);

        for (int i = 0; i < capacity; i++)
        {
            @Nullable
            var sequenceId = i >= availableSequences.size() ? null : availableSequences.get(i);

            var guiChar = this.getCurrentIndexChar(i);
            var icon = sequenceId == null ? new ItemStack(Material.GRAY_STAINED_GLASS_PANE) : defaultIcon.clone();
            int finalIndex = i;

            icon.editMeta(meta ->
            {
                meta.setRarity(ItemRarity.COMMON);

                if (meta instanceof BlockDataMeta blockDataMeta)
                {
                    var blockData = blockDataMeta.getBlockData(Material.LIGHT);
                    if (blockData instanceof Levelled levelled) levelled.setLevel(1 + finalIndex);

                    blockDataMeta.setBlockData(blockData);
                }
            });

            // String(Raw) -> Component(Parsed) -> String(Parsed MiniMessage format) -> Component(Parsed again)
            // So terrible, couldn't we just use Component instead String to set the item name?
            Component sequenceDisplayNameComponent = sequenceId == null
                    ? EmoteStrings.none().toComponent(playerLocale)
                    : EmoteStrings.get(sequenceId).toComponent(playerLocale);

            String sequenceDisplayName = defaultMiniMessage.serialize(sequenceDisplayNameComponent);

            var element = new StaticGuiElement(guiChar, icon, 1 + i, click ->
            {
                if (sequenceId == null) return true;

                var animationSet = state.getProvider()
                        .getAnimationProvider()
                        .getAnimationSetFor(state.getDisguiseIdentifier());

                var sequencePair = animationSet.sequenceOf(sequenceId);

                getBindingPlayer().playSound(clickSound);
                state.tryScheduleSequence(sequenceId, sequencePair.left(), sequencePair.right());
                guiInstance.close();

                return true;
            },
            "<italic:false>" + sequenceDisplayName);

            gui.addElement(element);
        }

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
                "<italic:false>" + GuiStrings.close().toString(playerLocale)));

        var disguiseElement = new StaticGuiElement('D',
                IconLookup.instance().lookup(state.getDisguiseIdentifier()),
                1,
                click -> true,
                "<italic:false>" + MorphStrings.disguisingAsString().resolve("what", state.getPlayerDisplay())
                        .toString(playerLocale));

        if (isDynamic.get())
            gui.addElement(new DynamicGuiElement('D', viewer -> disguiseElement));
        else
            gui.addElement(disguiseElement);
    }
}
