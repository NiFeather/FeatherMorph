package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.UUID;

public class OffTreeProperties
{
    public static final SingleProperty<Boolean> IS_BABY = SingleProperty.of("is_baby", false).withValidInput("true", "false");
    public static final SingleProperty<UUID> VIRTUAL_ENTITY_UUID = SingleProperty.of("virtual_entity_uuid", UUID.randomUUID());
    public static final SingleProperty<DisguiseEquipment> FAKE_EQUIPMENT = SingleProperty.of("fake_equip", new DisguiseEquipment());
    public static final SingleProperty<Boolean> DISPLAY_FAKE_EQUIPMENT = SingleProperty.of("display_fake_equipment", false);
}
