package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.util.Vector3f;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;

public class ArmorStandWatcher extends InventoryLivingWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ARMOR_STAND);
    }

    public ArmorStandWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ARMOR_STAND);
    }


    public byte getArmorStandFlags(boolean small, boolean showArms, boolean noBasePlate)
    {
        var value = (byte)0x00;

        if (small)
            value |= (byte)0x01;

        if (showArms)
            value |= (byte)0x04;

        if (noBasePlate)
            value |= (byte)0x08;

        return value;
    }

    private boolean isSmall()
    {
        return (get(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x01) == 0x01;
    }

    private boolean noBasePlate()
    {
        return (get(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x08) == 0x08;
    }

    private boolean showArms()
    {
        return (get(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x04) == 0x04;
    }

    private ListTag saveVector(Vector3f vector3f)
    {
        var tag = new ListTag();

        tag.add(FloatTag.valueOf(vector3f.getX()));
        tag.add(FloatTag.valueOf(vector3f.getY()));
        tag.add(FloatTag.valueOf(vector3f.getZ()));

        return tag;
    }

    private Vector3f readVector(ListTag tag)
    {
        return new Vector3f(tag.getFloat(0), tag.getFloat(1), tag.getFloat(2));
    }

    private Vector3f getVec3IfEmpty(ListTag listTag, Vector3f defaultValue)
    {
        if (listTag.isEmpty())
        {
            logger.warn("Empty listTag! Using defaultValue...");
            listTag = saveVector(defaultValue);
        }

        return readVector(listTag);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);
        boolean small = isSmall();
        boolean noBasePlate = noBasePlate();
        boolean showArms = showArms();

        if (nbt.contains("Small"))
            small = nbt.getBoolean("Small");

        if (nbt.contains("NoBasePlate"))
            noBasePlate = nbt.getBoolean("NoBasePlate");

        if (nbt.contains("ShowArms"))
            showArms = nbt.getBoolean("ShowArms");

        //Tag "Invisible" is not supported as it's synced with the player

        write(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(small, showArms, noBasePlate));

        if (nbt.contains("Pose"))
        {
            var poseCompound = nbt.getCompound("Pose");

            if (poseCompound.contains("Body"))
            {
                write(ValueIndex.ARMOR_STAND.BODY_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("Body", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.BODY_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("Head"))
            {
                write(ValueIndex.ARMOR_STAND.HEAD_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("Head", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.HEAD_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("LeftArm"))
            {
                write(ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("LeftArm", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("RightArm"))
            {
                write(ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("RightArm", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("LeftLeg"))
            {
                write(ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("LeftLeg", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("RightLeg"))
            {
                write(ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION,
                        getVec3IfEmpty(poseCompound.getList("RightLeg", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION.defaultValue()));
            }
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("Small", this.isSmall());
        nbt.putBoolean("NoBasePlate", this.noBasePlate());
        nbt.putBoolean("ShowArms", this.showArms());

        var poseCompound = new CompoundTag();
        poseCompound.put("Head", saveVector(get(ValueIndex.ARMOR_STAND.HEAD_ROTATION)));
        poseCompound.put("Body", saveVector(get(ValueIndex.ARMOR_STAND.BODY_ROTATION)));
        poseCompound.put("LeftArm", saveVector(get(ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION)));
        poseCompound.put("RightArm", saveVector(get(ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION)));
        poseCompound.put("LeftLeg", saveVector(get(ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION)));
        poseCompound.put("RightLeg", saveVector(get(ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION)));

        nbt.put("Pose", poseCompound);
    }
}
