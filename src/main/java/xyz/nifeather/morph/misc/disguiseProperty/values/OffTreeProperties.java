package xyz.nifeather.morph.misc.disguiseProperty.values;

import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.OutputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.UUID;

public class OffTreeProperties
{
    public static final SingleProperty<UUID> VIRTUAL_ENTITY_UUID = SingleProperty.builder("offtree/virtual_entity_uuid", UUID.randomUUID())
            .withInputHandle(InputHandles::immediateException)
            .withOutputHandle(OutputHandles::writeUUID)
            .build();

}
