package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.util.Vector3f;
import io.papermc.paper.math.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ArmorStandPropertyCollection;

public class ArmorStandWatcher extends LivingEntityWatcher
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
        return (read(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x01) == 0x01;
    }

    private boolean noBasePlate()
    {
        return (read(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x08) == 0x08;
    }

    private boolean showArms()
    {
        return (read(ValueIndex.ARMOR_STAND.DATA_FLAGS) & 0x04) == 0x04;
    }

    private Vector3f getVec3(ListTag listTag, Vector3f defaultValue)
    {
        if (listTag.isEmpty() || listTag.size() < 3)
        {
            logger.warn("Not enough parameters in listTag! Using defaultValue...");
            return defaultValue;
        }

        return new Vector3f(listTag.getFloat(0).orElseThrow(), listTag.getFloat(1).orElseThrow(), listTag.getFloat(2).orElseThrow());
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(ArmorStandPropertyCollection.class);

        if (property.equals(properties.SHOW_ARMS))
        {
            var val = (Boolean) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(this.isSmall(), val, this.noBasePlate()));
            return;
        }

        if (property.equals(properties.HAS_BASE_PLATE))
        {
            var val = (Boolean) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(this.isSmall(), this.showArms(), !val));
            return;
        }

        if (property.equals(properties.SMALL))
        {
            var val = (Boolean) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(val, this.showArms(), this.noBasePlate()));
            return;
        }

        if (property.equals(properties.HEAD_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.HEAD_ROTATION, toVector(val));
        }

        if (property.equals(properties.BODY_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.BODY_ROTATION, toVector(val));
        }

        if (property.equals(properties.LEFT_ARM_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION, toVector(val));
        }

        if (property.equals(properties.RIGHT_ARM_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION, toVector(val));
        }

        if (property.equals(properties.LEFT_LEG_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION, toVector(val));
        }

        if (property.equals(properties.RIGHT_LEG_ROTATION))
        {
            var val = (Rotations) value;
            this.writePersistent(ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION, toVector(val));
        }

        super.onPropertyWrite(property, value);
    }

    private Vector3f toVector(Rotations rotations)
    {
        return new Vector3f((float) rotations.x(), (float) rotations.y(), (float) rotations.z());
    }

    private ListTag saveRotationOf(SingleValue<Vector3f> sv)
    {
        return saveRotations(read(sv));
    }

    private ListTag saveRotations(Vector3f rotations)
    {
        ListTag listTag = new ListTag();
        listTag.add(FloatTag.valueOf(rotations.getX()));
        listTag.add(FloatTag.valueOf(rotations.getY()));
        listTag.add(FloatTag.valueOf(rotations.getZ()));
        return listTag;
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        nbt.putBoolean("Small", this.isSmall());
        nbt.putBoolean("NoBasePlate", this.noBasePlate());
        nbt.putBoolean("ShowArms", this.showArms());

        var poseCompound = new CompoundTag();
        poseCompound.put("Head", saveRotationOf(ValueIndex.ARMOR_STAND.HEAD_ROTATION));
        poseCompound.put("Body", saveRotationOf(ValueIndex.ARMOR_STAND.BODY_ROTATION));
        poseCompound.put("LeftArm", saveRotationOf(ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION));
        poseCompound.put("RightArm", saveRotationOf(ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION));
        poseCompound.put("LeftLeg", saveRotationOf(ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION));
        poseCompound.put("RightLeg", saveRotationOf(ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION));

        nbt.put("Pose", poseCompound);
    }
}