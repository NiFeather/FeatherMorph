package xyz.nifeather.morph.misc.gui;

import de.themoep.inventorygui.GuiPageElement;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class PagerWithFallbackItem extends GuiPageElement
{
    private final ItemStack fallbackItem;

    public PagerWithFallbackItem(char slotChar, ItemStack item, ItemStack fallback, PageAction pageAction, String... text)
    {
        super(slotChar, item, pageAction, text);

        this.fallbackItem = fallback;
    }

    @Override
    public ItemStack getItem(HumanEntity who, int slot)
    {
        var result = super.getItem(who, slot);
        if (result != null)
        {
            result.setAmount(1);
            return result;
        }

        return fallbackItem;
    }
}