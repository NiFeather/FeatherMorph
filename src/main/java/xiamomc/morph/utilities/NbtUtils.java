package xiamomc.morph.utilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NbtUtils
{
    /**
     * 获取目标实体的 {@link CompoundTag}
     * @param entity 目标实体
     * @return 此实体的NBT数据，当实体为null或不为 {@link CraftEntity} 的实例时返回null
     */
    @Nullable
    public static CompoundTag getRawTagCompound(Entity entity)
    {
        if (entity instanceof CraftEntity craftEntity)
        {
            var nmsEntity = craftEntity.getHandle();

            var entityDataObject = new EntityDataAccessor(nmsEntity);

            return entityDataObject.getData();
        }

        return null;
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
