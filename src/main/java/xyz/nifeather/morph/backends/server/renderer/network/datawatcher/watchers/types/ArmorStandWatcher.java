package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.Rotations;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.ArmorStandProperties;
import xyz.nifeather.morph.utilities.NbtUtils;

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
        return this.readEntryOrDefault(CustomEntries.ARMOR_STAND_SMALL, false);
    }

    private boolean noBasePlate()
    {
        return this.readEntryOrDefault(CustomEntries.ARMOR_STAND_NO_BASE_PLATE, false);
    }

    private boolean showArms()
    {
        return this.readEntryOrDefault(CustomEntries.ARMOR_STAND_SHOW_ARMS, false);
    }

    private Rotations getVec3(ListTag listTag, Rotations defaultValue)
    {
        if (listTag.isEmpty())
            listTag = defaultValue.save();

        return new Rotations(listTag);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(ArmorStandProperties.class);

        if (property.equals(properties.SHOW_ARMS))
        {
            var val = (Boolean) value;
            this.writeEntry(CustomEntries.ARMOR_STAND_SHOW_ARMS, val);
            this.writePersistent(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(this.isSmall(), val, this.noBasePlate()));
        }

        super.onPropertyWrite(property, value);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);
        boolean small = isSmall();
        boolean noBasePlate = noBasePlate();
        boolean showArms = showArms();

        if (nbt.contains("Small"))
        {
            small = nbt.getBoolean("Small");
            this.writeEntry(CustomEntries.ARMOR_STAND_SMALL, small);
        }

        if (nbt.contains("NoBasePlate"))
        {
            noBasePlate = nbt.getBoolean("NoBasePlate");
            this.writeEntry(CustomEntries.ARMOR_STAND_NO_BASE_PLATE, noBasePlate);
        }

        if (nbt.contains("ShowArms"))
        {
            showArms = nbt.getBoolean("ShowArms");
            this.writeEntry(CustomEntries.ARMOR_STAND_SHOW_ARMS, showArms);
        }

        //Tag "Invisible" is not supported as it's synced with the player

        writePersistent(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(small, showArms, noBasePlate));

        if (nbt.contains("Pose"))
        {
            var poseCompound = nbt.getCompound("Pose");

            if (poseCompound.contains("Body"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.BODY_ROTATION,
                        getVec3(poseCompound.getList("Body", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.BODY_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("Head"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.HEAD_ROTATION,
                        getVec3(poseCompound.getList("Head", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.HEAD_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("LeftArm"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION,
                        getVec3(poseCompound.getList("LeftArm", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("RightArm"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION,
                        getVec3(poseCompound.getList("RightArm", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("LeftLeg"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION,
                        getVec3(poseCompound.getList("LeftLeg", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION.defaultValue()));
            }

            if (poseCompound.contains("RightLeg"))
            {
                writePersistent(ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION,
                        getVec3(poseCompound.getList("RightLeg", CompoundTag.TAG_FLOAT),
                                ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION.defaultValue()));
            }
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var small = this.readEntry(CustomEntries.ARMOR_STAND_SMALL);
        if (small != null)
            nbt.putBoolean("Small", small);

        var noBasePlate = this.readEntry(CustomEntries.ARMOR_STAND_NO_BASE_PLATE);
        if (noBasePlate != null)
            nbt.putBoolean("NoBasePlate", noBasePlate);

        var showArms = this.readEntry(CustomEntries.ARMOR_STAND_SHOW_ARMS);
        if (showArms != null)
            nbt.putBoolean("ShowArms", showArms);

        var poseCompound = new CompoundTag();
        saveCompoundIfSet(poseCompound, "Head", ValueIndex.ARMOR_STAND.HEAD_ROTATION);
        saveCompoundIfSet(poseCompound, "Body", ValueIndex.ARMOR_STAND.BODY_ROTATION);
        saveCompoundIfSet(poseCompound, "LeftArm", ValueIndex.ARMOR_STAND.LEFT_ARM_ROTATION);
        saveCompoundIfSet(poseCompound, "RightArm", ValueIndex.ARMOR_STAND.RIGHT_ARM_ROTATION);
        saveCompoundIfSet(poseCompound, "LeftLeg", ValueIndex.ARMOR_STAND.LEFT_LEG_ROTATION);
        saveCompoundIfSet(poseCompound, "RightLeg", ValueIndex.ARMOR_STAND.RIGHT_LEG_ROTATION);

        nbt.put("Pose", poseCompound);
    }

    private void saveCompoundIfSet(CompoundTag compoundTag, String name, SingleValue<Rotations> sv)
    {
        var value = readOr(sv, null);
        if (value == null) return;

        compoundTag.put(name, value.save());
    }
}
