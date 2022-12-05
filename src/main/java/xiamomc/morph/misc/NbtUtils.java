package xiamomc.morph.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.server.commands.data.CommandDataAccessorEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class NbtUtils
{
    /**
     * 获取目标实体的 {@link NBTTagCompound}
     * @param entity 目标实体
     * @return 此实体的NBT数据，当实体为null或不为 {@link CraftEntity} 的实例时返回null
     */
    @Nullable
    public static NBTTagCompound getRawTagCompound(Entity entity)
    {
        if (entity instanceof CraftEntity craftEntity)
        {
            var nmsEntity = craftEntity.getHandle();

            var entityDataObject = new CommandDataAccessorEntity(nmsEntity);

            return entityDataObject.a();
        }

        return null;
    }

    /**
     * 将目标NBT序列化为字符串
     * @param compound 目标NBT
     * @return 由此NBT序列化的字符串，当compound为null时返回null
     */
    public static String getCompoundString(NBTTagCompound compound)
    {
        if (compound == null) return null;

        //StringNbtWriter
        var visitor = new StringTagVisitor();

        //StringNbtWriter#apply(NbtElement)
        return visitor.a((NBTBase) compound);
    }
}
