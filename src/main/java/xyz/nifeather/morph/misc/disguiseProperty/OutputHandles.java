package xyz.nifeather.morph.misc.disguiseProperty;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Keyed;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphEquipmentStruct;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphProfileProperty;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphResolvableProfileStruct;
import xyz.nifeather.morph.network.server.ServerSetEquipCommand;
import xyz.nifeather.morph.utilities.ItemUtils;
import xyz.nifeather.morph.utilities.NbtUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class OutputHandles
{
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static GsonComponentSerializer createComponentSerializer()
    {
        return GsonComponentSerializer.builder()
                .editOptions(builder ->
                {
                    builder.value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, false);
                }).build();
    }

    private static final GsonComponentSerializer COMPONENT_SERIALIZER = createComponentSerializer();

    public static <X> String immediateException(String propertyName, X val) throws ParseErrorException
    {
        throw ParseErrorException.forProperty(propertyName)
                .withMessage("This property is not configured, or doesn't support output handles")
                .create();
    }

    public static String writeBoolean(String propertyName, Boolean val)
    {
        return val.toString().toLowerCase();
    }

    public static String writeString(String propertyName, String val)
    {
        return val;
    }

    public static String writeFloat(String propertyName, Float fl)
    {
        return Float.toString(fl);
    }

    public static String writeDouble(String propertyName, Double d)
    {
        return Double.toString(d);
    }

    public static String writeInteger(String propertyName, Integer i)
    {
        return Integer.toString(i);
    }

    public static String writeRotations(String propertyName, Rotations rotations)
    {
        List<Double> array = new ArrayList<>();
        array.add(rotations.x());
        array.add(rotations.y());
        array.add(rotations.z());

        return gson.toJson(array);
    }

    public static <E extends Enum<E>> String writeEnum(String propertyName, Enum<E> eEnum)
    {
        return eEnum.name().toLowerCase();
    }

    public static String writeAdventureComponentJSON(String propertyName, Component component)
    {
        return COMPONENT_SERIALIZER.serialize(component);
    }

    public static String writeUUID(String propertyName, UUID uuid)
    {
        return uuid.toString();
    }

    public static String writeKeyed(String propertyName, Keyed keyed)
    {
        return keyed.key().asString();
    }

    public static String writeResolvableProfileAny(String propertyName, ResolvableProfile resolvableProfile) throws ParseErrorException
    {
        ImmutableList.Builder<String> properties = ImmutableList.builder();

        for (ProfileProperty property : resolvableProfile.properties())
        {
            try
            {
                properties.add(gson.toJson(MorphProfileProperty.fromPaperProperty(property)));
            }
            catch (JsonParseException e)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .causedBy(e)
                        .withMessage("GSON error, see details")
                        .withLocalizableMessage(ExceptionStrings.malformedInput())
                        .create();
            }
        }

        var skinPatch = resolvableProfile.skinPatch();
        var skinPatchModel = skinPatch.model();

        var record = new MorphResolvableProfileStruct(resolvableProfile.uuid(), resolvableProfile.name(), properties.build(),
                nullableKeyToString(skinPatch.cape()),
                nullableKeyToString(skinPatch.elytra()),
                skinPatchModel == null ? null : skinPatchModel.toString().toLowerCase(),
                nullableKeyToString(skinPatch.body()),
                resolvableProfile.dynamic());

        return gson.toJson(record);
    }

    public static @Nullable String nullableKeyToString(@Nullable Key key)
    {
        if (key == null) return null;

        return key.asString();
    }

    public static String writeGameProfile(String propertyName, GameProfile profile)
    {
        return writeCompound(propertyName, NbtUtils.toCompoundTag(profile));
    }

    public static String writeCompound(String propertyName, CompoundTag compoundTag)
    {
        return NbtUtils.getCompoundString(compoundTag);
    }

    public static String writeEquipment(String propertyName, DisguiseEquipment equipment)
    {
        Map<String, String> stringMap = new ConcurrentHashMap<>();
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (slot == EquipmentSlot.BODY || slot == EquipmentSlot.SADDLE) continue;

            ItemStack item = equipment.getItemOrNull(slot);

            if (item != null)
                stringMap.put(ServerSetEquipCommand.toProtocolEquipment(slot).toString(), ItemUtils.itemToStr(item));
        }

        var record = new MorphEquipmentStruct(SharedConstants.getCurrentVersion().dataVersion().version(), stringMap);
        return gson.toJson(record);
    }
}
