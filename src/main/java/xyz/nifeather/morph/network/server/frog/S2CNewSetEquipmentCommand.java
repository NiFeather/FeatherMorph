package xyz.nifeather.morph.network.server.frog;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Registry;
import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.Damageable;
import xyz.nifeather.morph.network.BasicServerHandler;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetFakeEquipCommand;
import xyz.nifeather.morph.utilities.ItemUtils;

import java.util.HashMap;
import java.util.List;
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

        // Basic elements: slot, type, count, damage
        argumentMap.put("slot", this.slot.toString());
        argumentMap.put("type", this.item.getType().key().asString());
        argumentMap.put("count", "" + this.item.getAmount());

        int damage = 0;
        if (item.getItemMeta() instanceof Damageable damageable)
            damage = damageable.getDamage();

        argumentMap.put("damage", "" + damage);

        // Optional elements: name, enchantments, armor_trim, banner_pattern
        var nameString = ItemUtils.getItemJsonName(this.item);
        if (nameString != null)
            argumentMap.put("name", nameString);

        var itemEnchantments = item.getEnchantments();
        if (!itemEnchantments.isEmpty())
        {
            Map<String, String> enchantments = new HashMap<>();
            itemEnchantments.forEach((ench, lvl) -> enchantments.put(ench.key().asString(), "" + lvl));

            argumentMap.put("enchantments", gson().toJson(enchantments));
        }

        appendArmorTrimIfPossible(argumentMap, this.item);
        appendBannerIfPossible(argumentMap, this.item);

        return argumentMap;
    }

    private void appendBannerIfPossible(Map<String, String> argumentMap, ItemStack item)
    {
        if (!(item.getItemMeta() instanceof BannerMeta banner))
            return;

        List<String> patternList = new ObjectArrayList<>();

        for (Pattern pattern : banner.getPatterns())
        {
            var map = new HashMap<String, String>();

            var patternKey = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.BANNER_PATTERN)
                    .getKey(pattern.getPattern());

            if (patternKey == null)
                continue;

            map.put("pattern_type", patternKey.key().asString());
            map.put("color", pattern.getColor().toString());

            patternList.add(gson().toJson(map));
        }

        argumentMap.put("banner_pattern", gson().toJson(patternList));
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
