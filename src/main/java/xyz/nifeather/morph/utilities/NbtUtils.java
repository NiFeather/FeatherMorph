package xyz.nifeather.morph.utilities;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;

import java.util.List;
import java.util.UUID;

public class NbtUtils
{
    @Nullable
    public static UUID readUUID(@Nullable Tag element)
    {
        //var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

        if (element == null)
            return null;

        if (!element.getType().equals(IntArrayTag.TYPE))
        {
            //logger.warn("Given element is not a int array, can't convert to UUID");
            return null;
        }

        int[] is = ((IntArrayTag)element).getAsIntArray();

        if (is.length != 4)
        {
            //logger.warn("Given int array is not of length 4, can't convert to UUID");
            return null;
        }

        return UUIDUtil.uuidFromIntArray(is);
    }

    public static void putUUID(CompoundTag nbt, String key, UUID uuid)
    {
        int[] array = UUIDUtil.uuidToIntArray(uuid);

        var tag = new IntArrayTag(array);
        nbt.put(key, tag);
    }

    public static CompoundTag writeGameProfile(CompoundTag nbt, GameProfile profile)
    {
        if (!profile.name().isEmpty())
            nbt.putString("Name", profile.name());

        if (!profile.id().equals(Uuids.NIL_UUID))
            putUUID(nbt, "Id", profile.id());

        if (profile.properties().isEmpty())
            return nbt;

        var propertiesCompound = new CompoundTag();

        for (var key : profile.properties().keySet())
        {
            if (key == null) continue;
            var list = new ListTag();

            CompoundTag childCompound;
            for (var property : profile.properties().get(key))
            {
                if (property == null) continue;

                childCompound = new CompoundTag();
                childCompound.putString("Value", property.value());

                var sign = property.signature();
                if (sign != null)
                    childCompound.putString("Signature", sign);

                list.add(childCompound);
            }

            propertiesCompound.put(key, list);
        }

        nbt.put("Properties", propertiesCompound);

        return nbt;
    }

    @NotNull
    public static GameProfile readGameProfileOrThrow(String snbt) throws ParseErrorException
    {
        CompoundTag compound;

        try
        {
            compound = TagParser.parseCompoundFully(snbt);
        }
        catch (Throwable t)
        {
            throw ParseErrorException.forProperty("anyskin")
                    .byMethod("NbtUtils#readGameProfileOrThrow")
                    .withMessage("Failed to read skin from compound")
                    .causedBy(t)
                    .create();
        }

        String name = "NIL";
        if (compound.contains("Name"))
        {
            name = compound.getString("Name").orElseThrow(() ->
            {
                return ParseErrorException.forProperty("anyskin")
                        .byMethod("NbtUtils#readGameProfileOrThrow")
                        .withMessage("Profile doesn't containing a name!")
                        .create();
            });
        }

        UUID uuid = Uuids.NIL_UUID;
        if (compound.contains("Id"))
        {
            var tag = compound.get("Id");
            var readUUID = NbtUtils.readUUID(tag);

            if (readUUID != null)
                uuid = readUUID;
        }

        if (!compound.contains("Properties")) return new GameProfile(uuid, name);

        try
        {
            var propertiesCompound = compound.getCompound("Properties").orElseThrow();
            ImmutableMultimap.Builder<String, Property> propertiesBuilder = ImmutableMultimap.builder();

            propertiesCompound.forEach((key, tag) ->
            {
                var list = propertiesCompound.getListOrEmpty(key);

                for (int i = 0; i < list.size(); i++)
                {
                    var childCompound = list.getCompound(i).orElse(null);
                    if (childCompound == null) continue;

                    var value = childCompound.getString("Value").orElseThrow();

                    if (childCompound.contains("Signature"))
                        propertiesBuilder.put(key, new Property(key, value, childCompound.getString("Signature").orElseThrow()));
                    else
                        propertiesBuilder.put(key, new Property(key, value));
                }
            });

            return new GameProfile(uuid, name, new PropertyMap(propertiesBuilder.build()));
        }
        catch (Throwable t)
        {
            throw ParseErrorException.forProperty("anyskin")
                    .withMessage("Failed to read skin from compound")
                    .causedBy(t)
                    .create();
        }
    }

    @javax.annotation.Nullable
    public static GameProfile readGameProfile(String snbt)
    {
        try
        {
            return readGameProfileOrThrow(snbt);
        }
        catch (ParseErrorException e)
        {
            var logger = FeatherMorphMain.getInstance().getSLF4JLogger();

            logger.warn("Unable to parse GameProfile: " + e.getMessage());
            logger.warn("Raw profile: '%s'".formatted(snbt));

            return null;
        }
    }

    public static CompoundTag toCompoundTag(GameProfile profile)
    {
        var compound = new CompoundTag();
        return writeGameProfile(compound, profile);
    }

    /**
     * 获取目标实体的 {@link CompoundTag}
     * @param entity 目标实体
     * @return 此实体的NBT数据，当实体为null或不为 {@link CraftEntity} 的实例时返回null
     */
    @NotNull
    public static CompoundTag getRawTagCompound(Entity entity)
    {
        if (entity instanceof CraftEntity craftEntity)
        {
            var nmsEntity = craftEntity.getHandle();

            var entityDataObject = new EntityDataAccessor(nmsEntity);

            return entityDataObject.getData();
        }

        return new CompoundTag();
    }

    /**
     * 将目标NBT序列化为字符串
     * @param compound 目标NBT
     * @return 由此NBT序列化的字符串，当compound为null时返回null
     */
    public static String getCompoundString(CompoundTag compound)
    {
        if (compound == null) return null;

        //StringNbtWriter
        var visitor = new StringTagVisitor();

        visitor.visitCompound(compound);

        //StringNbtWriter#apply(NbtElement)
        return visitor.build();
    }

    /**
     *
     * @param input The input NBT string
     * @return Null if invalid of illegal
     */
    @Nullable
    public static CompoundTag toCompoundTag(@Nullable String input)
    {
        if (input == null || input.isEmpty()) return null;

        try
        {
            return TagParser.parseCompoundFully(input);
        }
        catch (Throwable t)
        {
            return null;
        }
    }

    public static ItemStack readItemStack(String compound, int dataVersion) throws ParseErrorException
    {
        var world = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle();
        if (world == null)
        {
            throw ParseErrorException.forProperty("anyItem")
                    .byMethod("NbtUtils#readItemStack")
                    .withMessage("Broken server implementation, no world is loaded")
                    .create();
        }

        var registry = world.registryAccess();

        CompoundTag tag = toCompoundTag(compound);

        if (tag == null)
        {
            throw ParseErrorException.forProperty("anyItem")
                    .byMethod("NbtUtils#readItemStack")
                    .withMessage("Invalid compound")
                    .create();
        }

        if (tag.getStringOr("id", "no").equals("minecraft:air"))
            return ItemStack.of(Material.AIR, 1);

        var ops = registry.createSerializationContext(NbtOps.INSTANCE);
        int currentDataVersion = SharedConstants.getCurrentVersion().dataVersion().version();

        if (dataVersion >= currentDataVersion)
            dataVersion = currentDataVersion;

        var fixer = DedicatedServer.getServer().getFixerUpper()
                .update(References.ITEM_STACK, new Dynamic<>(ops, tag), dataVersion, currentDataVersion);

        var item = net.minecraft.world.item.ItemStack.CODEC.decode(fixer);

        if (item.result().isPresent())
            return CraftItemStack.asBukkitCopy(item.result().get().getFirst());

        return null;
    }

    @Nullable
    @Contract("_, false -> !null; _, true -> _")
    public static CompoundTag toCompoundTag(@Nullable String input, boolean nullIfInvalid)
    {
        var result = toCompoundTag(input);

        if (result != null) return result;

        return nullIfInvalid ? null : new CompoundTag();
    }

    public static boolean isBabyForType(EntityType type, CompoundTag compoundTag)
    {
        var ageable = EntityTypeUtils.hasBabyVariant(type);

        if (!ageable) return false;

        if (EntityTypeUtils.isZombie(type) || type == EntityType.PIGLIN)
            return compoundTag.getBoolean("IsBaby").orElse(false);

        var val = compoundTag.getInt("Age").orElse(1);

        return val < 0;
    }

    public static List<String> defaultBlacklistedPatterns = List.of(
            //Common
            "[pP]urpur.*", "[pP]aper.*", "[sS]pigot.*", "[bB]ukkit.*",

            //Player
            "Xp.*", "food.*",

            //Misc
            "Death.*", "Spawn.*"
    );

    public static List<String> defaultBlacklistedTags = List.of(
            //Common
            "UUID", "data", "Brain", "Motion", "palette", "Attributes",
            "Invulnerable",

            //Armor stand
            "DisabledSlots", "ArmorItems", "HandItems",

            //Player
            "Tags", "recipes", "Inventory", "abilities", "recipeBook",
            "EnderItems", "warden_spawn_tracker", "previousPlayerGameType",
            "LastDeathLocation", "playerGameType", "seenCredits", "Score",

            //Villager
            "Offers", "LastRestock", "RestocksToday",

            //Misc
            "Pos", "Owner", "WorldUUIDLeast", "WorldUUIDMost",
            "Rotation", "listener", "ActiveEffects", "ArmorDropChances",
            "PersistenceRequired", "SelectedItem"
    );
}
