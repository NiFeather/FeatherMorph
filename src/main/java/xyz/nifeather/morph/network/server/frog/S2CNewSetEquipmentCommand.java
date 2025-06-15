package xyz.nifeather.morph.network.server.frog;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import xyz.nifeather.morph.network.BasicServerHandler;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class S2CNewSetEquipmentCommand extends AbstractS2CCommand<ItemStack>
{
    private final S2CSetFakeEquipCommand.ProtocolEquipmentSlot slot;
    private final ItemStack item;

    public S2CNewSetEquipmentCommand(EquipmentSlot slot, ItemStack item)
    {
        this.slot = toProtocolEquipment(slot);
        this.item = item;
    }

    @Override
    public String getBaseName()
    {
        return "v2_set_fake_equip";
    }

    @Override
    public void onCommand(BasicServerHandler<?> handler)
    {
    }

    @Override
    public Map<String, String> generateArgumentMap()
    {
        Map<String, String> argumentMap = new ConcurrentHashMap<>();
        argumentMap.put("slot", this.slot.toString());
        argumentMap.put("type", this.item.getType().key().asString());
        argumentMap.put("count", "" + this.item.getAmount());
        argumentMap.put("name", ItemUtils.getItemJsonName(this.item));

        Map<String, String> enchantments = new HashMap<>();
        item.getEnchantments().forEach((ench, lvl) ->
        {
            enchantments.put(ench.key().asString(), "" + lvl);
        });

        argumentMap.put("enchantments", gson().toJson(enchantments));

        appendArmorTrimIfPossible(argumentMap, this.item);

        return argumentMap;
    }

    private void appendArmorTrimIfPossible(Map<String, String> argumentMap, ItemStack item)
    {
        if (!(item.getItemMeta() instanceof ArmorMeta armorMeta))
            return;

        var trim = armorMeta.getTrim();

        if (trim == null)
            return;

        var material = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.TRIM_MATERIAL)
                .getKey(trim.getMaterial());

        var pattern = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.TRIM_PATTERN)
                .getKey(trim.getPattern());

        if (material == null || pattern == null)
            return;

        var stringMap = new HashMap<String, String>();
        stringMap.put("material", material.key().asString());
        stringMap.put("pattern", pattern.key().asString());

        argumentMap.put("armor_trim", gson().toJson(stringMap));
    }

    private static S2CSetFakeEquipCommand.ProtocolEquipmentSlot toProtocolEquipment(EquipmentSlot slot)
    {
        return switch (slot)
        {
            case HAND -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.MAINHAND;
            case OFF_HAND -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.OFF_HAND;
            case FEET -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.BOOTS;
            case LEGS -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.LEGGINGS;
            case CHEST -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.CHESTPLATE;
            case HEAD -> S2CSetFakeEquipCommand.ProtocolEquipmentSlot.HELMET;
            case BODY -> throw new IllegalArgumentException("BODY is not supported."); //生物BODY，和玩家无关？
            case SADDLE ->  throw new IllegalArgumentException("SADDLE is not supported.");
        };
    }
}
