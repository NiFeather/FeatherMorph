package xiamomc.morph.network.commands.S2C;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import xiamomc.morph.utilities.ItemUtils;

public class S2CSetEquipCommand extends S2CSetCommand<ItemStack>
{
    public S2CSetEquipCommand(ItemStack item, EquipmentSlot slot)
    {
        super(item);
        this.slot = slot;
    }

    private final EquipmentSlot slot;

    @Override
    public String getBaseName()
    {
        return "equip";
    }

    @Override
    public String buildCommand()
    {
        var slotName = switch (slot)
                {
                    case HAND -> "mainhand";
                    case HEAD -> "helmet";
                    case CHEST -> "chestplate";
                    case LEGS -> "leggings";
                    case FEET -> "boots";
                    default -> slot.name().toLowerCase();
                };

        var item = ItemUtils.itemOrAir(this.getArgumentAt(0));

        return super.buildCommand() + " " + slotName + " " + ItemUtils.itemToStr(item);
    }
}
