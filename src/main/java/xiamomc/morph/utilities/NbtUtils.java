package xiamomc.morph.utilities;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.properties.Property;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.players.GameProfileCache;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class NbtUtils
{
    //TODO: COPIED FROM DECOMPILED SOURCE
    //TODO: REPLACE THIS WITH OUR OWN CODE, OR FIND OUT HOW 1.20.5 CONVERTS GAMEPROFILE
    public static CompoundTag writeGameProfile(CompoundTag nbt, GameProfile profile) {
        if (!profile.getName().isEmpty()) {
            nbt.putString("Name", profile.getName());
        }

        if (!profile.getId().equals(Util.NIL_UUID)) {
            nbt.putUUID("Id", profile.getId());
        }

        if (!profile.getProperties().isEmpty()) {
            CompoundTag compoundTag = new CompoundTag();
            Iterator var3 = profile.getProperties().keySet().iterator();

            while(var3.hasNext()) {
                String string = (String)var3.next();
                ListTag listTag = new ListTag();

                CompoundTag compoundTag2;
                for(Iterator var6 = profile.getProperties().get(string).iterator(); var6.hasNext(); listTag.add(compoundTag2)) {
                    Property property = (Property)var6.next();
                    compoundTag2 = new CompoundTag();
                    compoundTag2.putString("Value", property.value());
                    String string2 = property.signature();
                    if (string2 != null) {
                        compoundTag2.putString("Signature", string2);
                    }
                }

                compoundTag.put(string, listTag);
            }

            nbt.put("Properties", compoundTag);
        }

        return nbt;
    }

    @javax.annotation.Nullable
    public static GameProfile readGameProfile(CompoundTag nbt) {
        UUID uUID = nbt.hasUUID("Id") ? nbt.getUUID("Id") : Util.NIL_UUID;
        if (nbt.contains("Id", 8)) {
            try {
                uUID = UUID.fromString(nbt.getString("Id"));
            } catch (IllegalArgumentException var11) {
            }
        }

        String string = nbt.getString("Name");

        try {
            GameProfile gameProfile = new GameProfile(uUID, string);
            if (nbt.contains("Properties", 10)) {
                CompoundTag compoundTag = nbt.getCompound("Properties");
                Iterator var5 = compoundTag.getAllKeys().iterator();

                while(var5.hasNext()) {
                    String string2 = (String)var5.next();
                    ListTag listTag = compoundTag.getList(string2, 10);

                    for(int i = 0; i < listTag.size(); ++i) {
                        CompoundTag compoundTag2 = listTag.getCompound(i);
                        String string3 = compoundTag2.getString("Value");
                        if (compoundTag2.contains("Signature", 8)) {
                            gameProfile.getProperties().put(string2, new Property(string2, string3, compoundTag2.getString("Signature")));
                        } else {
                            gameProfile.getProperties().put(string2, new Property(string2, string3));
                        }
                    }
                }
            }

            return gameProfile;
        } catch (Throwable var12) {
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

        //StringNbtWriter#apply(NbtElement)
        return visitor.visit(compound);
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
            return TagParser.parseTag(input);
        }
        catch (Throwable t)
        {
            return null;
        }
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
            return compoundTag.getBoolean("IsBaby");

        var val = compoundTag.getInt("Age");

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
