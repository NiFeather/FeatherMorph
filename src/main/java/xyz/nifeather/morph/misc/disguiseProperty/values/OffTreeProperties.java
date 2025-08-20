package xyz.nifeather.morph.misc.disguiseProperty.values;

import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.UUID;

public class OffTreeProperties
{
    public static final SingleProperty<UUID> VIRTUAL_ENTITY_UUID = SingleProperty.of("offtree/virtual_entity_uuid", UUID.randomUUID());
    public static final SingleProperty<DisguiseEquipment> FAKE_EQUIPMENT = SingleProperty.of("offtree/fake_equip", new DisguiseEquipment());
    public static final SingleProperty<Boolean> DISPLAY_FAKE_EQUIPMENT = SingleProperty.of("offtree/display_fake_equipment", false);
}
