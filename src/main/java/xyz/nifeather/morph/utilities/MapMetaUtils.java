package xyz.nifeather.morph.utilities;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import xiamomc.morph.network.commands.S2C.clientrender.Equipment;

public class MapMetaUtils
{
    public static Equipment toPacketEquipment(EntityEquipment disguiseEquipment)
    {
        var items = new ItemStack[]
                {
                        disguiseEquipment.getHelmet(),
                        disguiseEquipment.getChestplate(),
                        disguiseEquipment.getLeggings(),
                        disguiseEquipment.getBoots(),

                        disguiseEquipment.getItemInMainHand(),
                        disguiseEquipment.getItemInOffHand()
                };

        var instance = new Equipment();
        for (int i = 0; i < items.length; i++)
        {
            var rec = bukkitToNMS(items[i]);
            switch (i)
            {
                case 0:
                {
                    instance.headId = rec.id;
                    instance.headNbt = rec.nbt;
                }
                case 1:
                {
                    instance.chestId = rec.id;
                    instance.chestNbt = rec.nbt;
                }
                case 2:
                {
                    instance.leggingId = rec.id;
                    instance.leggingNbt = rec.nbt;
                }
                case 3:
                {
                    instance.feetId = rec.id;
                    instance.feetNbt = rec.nbt;
                }
                case 4:
                {
                    instance.handId = rec.id;
                    instance.handNbt = rec.nbt;
                }
                case 5:
                {
                    instance.offhandId = rec.id;
                    instance.offhandNbt = rec.nbt;
                }
            }
        }

        return instance;
    }

    private static ItemRecord bukkitToNMS(ItemStack bukkitStack)
    {
        var snbt = ItemUtils.itemToStr(bukkitStack);
        String id = bukkitStack.getType().getKey().asString();

        return new ItemRecord(id, snbt);
    }

    private static record ItemRecord(String id, String nbt)
    {
    }
}
