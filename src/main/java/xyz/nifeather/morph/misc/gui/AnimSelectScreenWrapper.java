package xyz.nifeather.morph.misc.gui;

import de.themoep.inventorygui.DynamicGuiElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Material;
import org.bukkit.block.data.Levelled;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import xiamomc.pluginbase.Bindables.BindableList;
import xyz.nifeather.morph.messages.EmoteStrings;
import xyz.nifeather.morph.messages.GuiStrings;
import xyz.nifeather.morph.messages.MorphStrings;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.List;

public class AnimSelectScreenWrapper extends ScreenWrapper
{
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

        return new InventoryGui(plugin, GuiStrings.selectAnimation().toString(playerLocale), rows.toArray(new String[]{}));
    }

    private void initElements(InventoryGui gui)
    {
        var actionItemBase = IconLookup.instance().lookup(state.getDisguiseIdentifier()); //new ItemStack(Material.LIGHT);

        if (IconLookup.instance().lookup(state.getDisguiseIdentifier()).getType() == Material.PLAYER_HEAD)
            this.isDynamic.set(true);

        for (int i = 0; i < Math.min(capacity, availableSequences.size()); i++)
        {
            var guiChar = this.getCurrentIndexChar(i);
            var itemClone = actionItemBase.clone();
            int finalIndex = i;
            itemClone.editMeta(meta ->
            {
                meta.setRarity(ItemRarity.COMMON);

                if (meta instanceof BlockDataMeta blockDataMeta)
                {
                    var blockData = blockDataMeta.getBlockData(Material.LIGHT);
                    if (blockData instanceof Levelled levelled) levelled.setLevel(1 + finalIndex);

                    blockDataMeta.setBlockData(blockData);
                }
            });

            var sequenceId = availableSequences.get(i);

            var sequenceDisplayName = EmoteStrings.get(sequenceId).withLocale(playerLocale).toString();

            var element = new StaticGuiElement(guiChar, itemClone, 1 + i, click ->
            {
                var animationSet = state.getProvider()
                        .getAnimationProvider()
                        .getAnimationSetFor(state.getDisguiseIdentifier());

                var sequencePair = animationSet.sequenceOf(sequenceId);

                getBindingPlayer().playSound(clickSound);
                state.tryScheduleSequence(sequenceId, sequencePair.left(), sequencePair.right());
                guiInstance.close();
                return true;
            },
            "§r" + sequenceDisplayName);

            gui.addElement(element);
        }

        gui.addElement(new StaticGuiElement('!',
                new ItemStack(Material.PINK_STAINED_GLASS_PANE),
                1,
                click -> true,
                "§r"));

        var closeElementItem = new ItemStack(Material.BARRIER);
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
                "§r" + GuiStrings.close().toString(playerLocale)));

        var disguiseElement = new StaticGuiElement('D',
                IconLookup.instance().lookup(state.getDisguiseIdentifier()),
                1,
                click -> true,
                "§r" + MorphStrings.disguisingAsString().resolve("what", state.getPlayerDisplay())
                        .toString(playerLocale));

        if (isDynamic.get())
            gui.addElement(new DynamicGuiElement('D', viewer -> disguiseElement));
        else
            gui.addElement(disguiseElement);
    }
}
