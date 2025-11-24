package xyz.nifeather.morph.misc.disguiseProperty.struct;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record MorphEquipmentStruct(int dataVersion, @Nullable Map<String, String> equipmentData)
{
}
